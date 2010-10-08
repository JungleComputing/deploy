package go.daccripple.comm;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import go.daccripple.Go;
import go.daccripple.Job;
import go.daccripple.Move;
import go.daccripple.Timer;
import ibis.ipl.ConnectionFailedException;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;
import ibis.util.ThreadPool;
import ibis.util.TypedProperties;

public class Communication implements MessageUpcall, RegistryEventHandler,
        Runnable {

    public static final int STEAL_CONNECTION_TIMEOUT = 10000;
    public static final int CONNECTION_TIMEOUT = 60000;

    public static final int STEAL_INTERVAL = 100000;

    private static final Logger logger = LoggerFactory
            .getLogger(Communication.class);

    PortType normalPortType = new PortType(PortType.COMMUNICATION_RELIABLE,
            PortType.SERIALIZATION_OBJECT_SUN, PortType.RECEIVE_AUTO_UPCALLS,
            PortType.CONNECTION_MANY_TO_ONE);

    PortType lightPortType = new PortType(PortType.COMMUNICATION_RELIABLE,
            PortType.SERIALIZATION_OBJECT_SUN, PortType.RECEIVE_AUTO_UPCALLS,
            PortType.CONNECTION_MANY_TO_ONE, PortType.CONNECTION_LIGHT);

    PortType ultralightPortType = new PortType(
            PortType.SERIALIZATION_OBJECT_SUN, PortType.RECEIVE_AUTO_UPCALLS,
            PortType.CONNECTION_MANY_TO_ONE, PortType.CONNECTION_ULTRALIGHT);

    IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.ELECTIONS_STRICT,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED,
            IbisCapabilities.TERMINATION);

    enum Opcode {
        STEAL_REQUEST, STEAL_REPLY, RESULT_RETURN;
    };

    private final Go go;

    private final Ibis myIbis;

    private final IbisIdentifier myIdentifier;

    private final ReceivePort normalReceivePort;

    private final ReceivePort lightReceivePort;

    private final ReceivePort ultralightReceivePort;

    private final IbisIdentifier master;

    private final boolean isMaster;

    private final boolean light;

    private final boolean ultralight;

    private final Random random;

    // local steal stuff

    private final ArrayList<IbisIdentifier> lanIbises;

    private final Timer lanStealTimer;

    private StealRequest lanSteal;

    private long lanStealsSucceeded;

    // wide-area steal stuff

    private final ArrayList<IbisIdentifier> wanIbises;

    private final Timer wanStealTimer;

    private StealRequest wanSteal;

    private long wanStealsSucceeded;

    private static String cluster(IbisIdentifier ibis) {
        return ibis.location().getLevel(ibis.location().numberOfLevels() - 1);
    }

    public Communication(Go go) throws IbisCreationFailedException, IOException {
        this.go = go;

        random = new Random();

        Timer joinTimer = go.getTimer("Join");
        
        joinTimer.start();
        myIbis = IbisFactory.createIbis(ibisCapabilities, this, normalPortType,
            lightPortType, ultralightPortType);
        joinTimer.stop();
        myIdentifier = myIbis.identifier();

        master = myIbis.registry().elect("Master");

        if (myIbis.identifier().equals(master)) {
            isMaster = true;
        } else {
            isMaster = false;
        }

        normalReceivePort = myIbis.createReceivePort(normalPortType, "normal",
            this);

        lightReceivePort = myIbis.createReceivePort(lightPortType, "light",
            this);

        ultralightReceivePort = myIbis.createReceivePort(ultralightPortType,
            "ultralight", this);

        TypedProperties properties = new TypedProperties(System.getProperties());

        light = properties.getBooleanProperty("go.light");
        ultralight = properties.getBooleanProperty("go.ultralight");

        if (isMaster) {
            logger.info("light = " + light + ", ultralight = " + ultralight);
        }

        lanIbises = new ArrayList<IbisIdentifier>();
        lanSteal = null;
        lanStealTimer = go.getTimer("LAN Steals");
        lanStealsSucceeded = 0;

        wanIbises = new ArrayList<IbisIdentifier>();
        wanSteal = null;
        wanStealTimer = go.getTimer("WAN Steals");
        wanStealsSucceeded = 0;

        normalReceivePort.enableConnections();
        normalReceivePort.enableMessageUpcalls();

        lightReceivePort.enableConnections();
        lightReceivePort.enableMessageUpcalls();

        ultralightReceivePort.enableConnections();
        ultralightReceivePort.enableMessageUpcalls();

        myIbis.registry().enableEvents();

        ThreadPool.createNew(this, "Steal request manager");
    }

    public IbisIdentifier getIdentifier() {
        return myIbis.identifier();
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void end() {
        logger.info("Ending communication");
        try {
            if (normalReceivePort != null) {
                normalReceivePort.close(1);
            }
            if (lightReceivePort != null) {
                lightReceivePort.close(1);
            }
            if (ultralightReceivePort != null) {
                ultralightReceivePort.close(1);
            }

            Timer leaveTimer = go.getTimer("Leave");
            
            leaveTimer.start();
            logger.info("Leaving...");
            myIbis.end();
            leaveTimer.stop();
        } catch (IOException e) {
            logger.error("error on ending communication", e);
        }
    }

    public void upcall(ReadMessage message) {
        Opcode opcode = null;
        try {

            opcode = (Opcode) message.readObject();

            if (opcode == Opcode.STEAL_REQUEST) {

                IbisIdentifier thief = message.origin().ibisIdentifier();

                UUID stealID = (UUID) message.readObject();

                // also causes next upcall
                message.finish();

                sendStealReply(thief, stealID);

            } else if (opcode == Opcode.STEAL_REPLY) {
                UUID id = (UUID) message.readObject();
                Job job = (Job) message.readObject();
                message.finish();

                logger.debug("Received steal reply for " + id + " result = "
                        + job);

                receivedJob(id, job);

            } else if (opcode == Opcode.RESULT_RETURN) {
                IbisIdentifier origin = message.origin().ibisIdentifier();

                UUID jobID = (UUID) message.readObject();
                UUID parentID = (UUID) message.readObject();
                Move result = (Move) message.readObject();

                // also causes next upcall
                message.finish();

                logger.debug("Received job result from  " + origin + " for "
                        + jobID + ",  result = " + result);

                go.returnStolenJob(origin, jobID, parentID, result);
            } else {
                logger.error("Unknown opcode " + opcode);
            }
        } catch (Exception e) {
            logger.warn("error on handling " + opcode + " message from "
                    + message.origin().ibisIdentifier(), e);
        }
    }

    private synchronized void receivedJob(UUID id, Job job) {
        // add to local Q
        if (job != null) {
            logger.debug("received job " + job);
            go.addToPriorityJobQ(job);
        }

        if (lanSteal != null && lanSteal.getID().equals(id)) {
            lanSteal.gotReply();
            if (job != null) {
                lanStealsSucceeded++;
            }
        }

        if (wanSteal != null && wanSteal.getID().equals(id)) {
            wanSteal.gotReply();
            if (job != null) {
                wanStealsSucceeded++;
            }
        }
    }

    public synchronized void startStealing() {
        // wake up stealing thread
        notifyAll();
    }

    private void sendStealReply(IbisIdentifier thief, UUID stealID) {
        SendPort replyPort = null;

        Job reply = go.steal(thief);
        try {

            if (reply == null) {
                replyPort = lightConnect(thief);
            } else {
                replyPort = myIbis.createSendPort(normalPortType);
                replyPort.connect(thief, "normal", CONNECTION_TIMEOUT, false);
            }

            // connect to the requestor's receive port
            logger.debug("Received steal request " + stealID + " from " + thief
                    + ", returning " + reply);

            // create a reply message
            WriteMessage replyMessage = replyPort.newMessage();

            replyMessage.writeObject(Opcode.STEAL_REPLY);
            replyMessage.writeObject(stealID);
            replyMessage.writeObject(reply);

            replyMessage.finish();

        } catch (ConnectionFailedException e) {
            if (reply == null) {
                logger.warn("error on connecting to " + thief
                        + ", for sending steal reply (NACK)", e);
            } else {
                logger.warn("error on connecting to " + thief
                        + ", for sending steal reply (Job)", e);
            }
            if (reply != null) {
                go.unsteal(thief, reply);
            }
        } catch (Exception e) {
            logger.warn("error on sending steal reply", e);
            if (reply != null) {
                go.unsteal(thief, reply);
            }
        } finally {
            if (replyPort != null) {
                try {
                    replyPort.close();
                } catch (IOException e) {
                    // IGNORE
                }
            }
        }

    }

    // return result to original Ibis.
    public void returnResult(Job job, Move result) {
        SendPort sendPort = null;

        logger.debug("returning result for job " + job);

        while (true) {
            try {
                sendPort = myIbis.createSendPort(normalPortType);
                sendPort.connect(job.getSource(), "normal", CONNECTION_TIMEOUT,
                    true);

                // Send the request message. This message contains the
                // identifier of
                // our receive port so the server knows where to send the reply
                WriteMessage request = sendPort.newMessage();
                request.writeObject(Opcode.RESULT_RETURN);
                request.writeObject(job.getID());
                request.writeObject(job.getParentID());
                request.writeObject(result);
                request.finish();
                return;
            } catch (IOException e) {
                logger.error("Could not return job " + "result to "
                        + job.getSource(), e);
                try {
                    myIbis.registry().maybeDead(job.getSource());
                } catch (IOException e1) {
                    logger.error("Error in maybeDead", e);
                }

                if (!isMember(job.getSource())) {
                    logger
                            .warn("tried to return result to ibis which left/died");
                    return;
                }

            } finally {
                if (sendPort != null) {
                    // Close port
                    try {
                        sendPort.close();
                    } catch (IOException e) {
                        logger.error("Error on closing send port", e);
                    }
                }
            }
        }
    }

    private synchronized boolean isMember(IbisIdentifier ibis) {
        for (IbisIdentifier member : lanIbises) {
            if (member.equals(ibis)) {
                return true;
            }
        }
        for (IbisIdentifier member : wanIbises) {
            if (member.equals(ibis)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a sendport, and connects to the given target. May use light or
     * ultralight connections, if these are enabled
     * 
     * @param target
     *            target Ibis.
     * @return sendport connected to the target
     * @throws IOException
     *             if creating the sendport or connection failed
     */
    private SendPort lightConnect(IbisIdentifier target) throws IOException {
        SendPort result = null;

        try {
            if (light) {
                result = myIbis.createSendPort(lightPortType);
                result.connect(target, "light", STEAL_CONNECTION_TIMEOUT, false);
                return result;
            } else if (ultralight) {
                result = myIbis.createSendPort(ultralightPortType);
                result.connect(target, "ultralight", STEAL_CONNECTION_TIMEOUT, false);
                return result;
            } else {
                result = myIbis.createSendPort(normalPortType);
                result.connect(target, "normal", STEAL_CONNECTION_TIMEOUT, false);
                return result;
            }
        } catch (ConnectionFailedException e) {
            result.close();
            throw e;
        }
    }

    public boolean sendStealRequest(IbisIdentifier target, UUID id) {
        SendPort sendPort = null;
        try {
            sendPort = lightConnect(target);

            // Send the request message. This message contains the identifier of
            // our receive port so the server knows where to send the reply
            WriteMessage request = sendPort.newMessage();
            Thread.sleep(1000);
                       request.writeObject(Opcode.STEAL_REQUEST);
            Thread.sleep(1000);
                       request.writeObject(id);
            Thread.sleep(1000);
                       request.finish();
            Thread.sleep(1000);

            return true;
        } catch (ConnectionFailedException e) {
            logger.warn("Error on stealing from " + target, e);
            return false;
        } catch (Exception e) {
            logger.error("Error on stealing from " + target, e);
            return false;
        } finally {
            if (sendPort != null) {
                try {
                    sendPort.close();
                } catch (IOException e) {
                    // IGNORE
                }
            }
        }
    }

    private synchronized IbisIdentifier getVictim(boolean local) {
        if (local) {
            if (lanIbises.size() == 0) {
                return null;
            }
            return lanIbises.get(random.nextInt(lanIbises.size()));
        } else {
            if (wanIbises.size() == 0) {
                return null;
            }
            return wanIbises.get(random.nextInt(wanIbises.size()));
        }
    }

    // Registry Events

    public synchronized void joined(IbisIdentifier joinedIbis) {
        logger.debug(joinedIbis + " joined");

        if (joinedIbis.equals(myIdentifier)) {
            logger.debug("not adding self");
            return;
        }

        if (cluster(joinedIbis).equals(cluster(myIdentifier))) {
            lanIbises.add(joinedIbis);
        } else {
            wanIbises.add(joinedIbis);
        }
    }

    public void left(IbisIdentifier ibis) {
        logger.debug(ibis + " left");
        remove(ibis);
    }

    public void died(IbisIdentifier ibis) {
        logger.debug(ibis + " died");
        remove(ibis);

        if (ibis.equals(myIdentifier)) {
            logger.error("Apparantly, I died. Exiting JVM");
            System.exit(1);
        }
    }

    private void remove(IbisIdentifier ibis) {
        synchronized (this) {
            if (cluster(ibis).equals(cluster(myIdentifier))) {
                lanIbises.remove(ibis);
            } else {
                wanIbises.remove(ibis);
            }
        }

        go.clearJobsFrom(ibis);

        if (ibis.equals(master)) {
            logger.debug("master left, time to go");
            go.terminate();
        }
    }

    public void gotSignal(String signal, IbisIdentifier source) {
        // IGNORED
    }

    public void poolClosed() {
        // IGNORED
    }

    public void electionResult(String electionName, IbisIdentifier winner) {
        // IGNORED
    }

    public void printStatistics(PrintStream out) {
        synchronized (this) {
            out.println("LAN Steals succeeded: " + lanStealsSucceeded);
            out.println("WAN Steals succeeded: " + wanStealsSucceeded);
        }
        myIbis.printManagementProperties(out);
    }

    public void poolTerminated(IbisIdentifier source) {
        // IGNORE

    }

    private synchronized void updateSteals() {
        if (lanSteal != null && lanSteal.isDone()) {
            lanSteal = null;
        }

        if (lanSteal == null) {
            IbisIdentifier victim = getVictim(true);
            if (victim != null) {
                lanSteal = new StealRequest(victim, this, lanStealTimer);
            }
        }

        if (wanSteal != null && wanSteal.isDone()) {
            wanSteal = null;
        }

        if (wanSteal == null) {
            IbisIdentifier victim = getVictim(false);
            if (victim != null) {
                wanSteal = new StealRequest(victim, this, wanStealTimer);
            }
        }

        try {
            wait(STEAL_INTERVAL);
        } catch (InterruptedException e) {
            // IGNORE
        }
    }

    public void run() {
        while (!go.hasTerminated()) {
            if (go.workersWaiting()) {
                updateSteals();
            }
        }
    }

}

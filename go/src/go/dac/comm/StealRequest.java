package go.dac.comm;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import go.dac.Timer;
import ibis.ipl.IbisIdentifier;
import ibis.util.ThreadPool;

public class StealRequest implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(StealRequest.class);

    public static final int STEAL_WAIT_TIME = 10000;

    private final IbisIdentifier target;

    private final Communication communication;

    private final Timer timer;

    private final UUID id;

    private boolean done;

    StealRequest(IbisIdentifier target, Communication communication, Timer timer) {
        this.target = target;
        this.communication = communication;
        this.timer = timer;

        id = UUID.randomUUID();

        ThreadPool.createNew(this, "Steal request to " + target);
        
        logger.debug("new steal to " + target);
    }

    private synchronized void setDone() {
        done = true;
    }

    public synchronized boolean isDone() {
        return done;
    }

    private synchronized void waitForReply() {
        try {
            wait(STEAL_WAIT_TIME);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    synchronized void gotReply() {
        // stop waiting for a reply
        notifyAll();
    }

    synchronized UUID getID() {
        return id;
    }

    public void run() {
        timer.start();
        if (communication.sendStealRequest(target, id)) {
            // if we successfully send the request, wait for a reply
            waitForReply();
        }
        timer.stop();
        setDone();
    }

}

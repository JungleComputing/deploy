package go.ipl;

import go.threads.Board;
import go.threads.Move;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.ReceivePortIdentifier;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public final class Go implements Runnable, MessageUpcall {

    public static boolean DEBUG = false;

    /**
     * Port type used for sending a request to the server
     */
    PortType requestPortType =
        new PortType(PortType.COMMUNICATION_RELIABLE,
                PortType.SERIALIZATION_OBJECT, PortType.RECEIVE_AUTO_UPCALLS,
                PortType.CONNECTION_MANY_TO_ONE);

    /**
     * Port type used for sending a reply back
     */
    PortType replyPortType =
        new PortType(PortType.COMMUNICATION_RELIABLE,
                PortType.SERIALIZATION_OBJECT, PortType.RECEIVE_EXPLICIT,
                PortType.CONNECTION_ONE_TO_ONE);

    IbisCapabilities ibisCapabilities =
        new IbisCapabilities(IbisCapabilities.ELECTIONS_STRICT,
                IbisCapabilities.CLOSED_WORLD);

    private final Ibis myIbis;

    private final Queue<Board> workQ;

    private int workSize;

    private final Map<String, Move> results;

    private int nrOfResults;

    private final IbisIdentifier server;

    // server receive port
    private final ReceivePort receiver;

    private final boolean isServer;

    public Go() throws IbisCreationFailedException, IOException {
        workQ = new LinkedList<Board>();
        results = new HashMap<String, Move>();

        myIbis =
            IbisFactory.createIbis(ibisCapabilities, null, requestPortType,
                replyPortType);

        server = myIbis.registry().elect("Server");
        
        if (myIbis.identifier().equals(server)) {
            isServer = true;
            receiver =
                myIbis.createReceivePort(requestPortType, "server", this);

        } else {
            isServer = false;
            receiver = null;
        }
    }

    private static void printIndented(int level, String message) {
        if (DEBUG) {
            for (int i = 0; i < level; i++) {
                message = "\t" + message;
            }
            System.err.println(message);
        }
    }

    /**
     * Returns if the first or the second move is better as far as the given
     * player is concerned
     * 
     * @param first
     *            First possible move
     * @param second
     *            Second possible move
     * @param blackPlayer
     *            if true black is deciding, if false white is
     * @return true if the first move is better, false otherwise
     */
    private static boolean isBetter(Move first, Move second, boolean blackPlayer) {
        if (first == null) {
            return false;
        }
        if (second == null) {
            return true;
        }

        boolean blacksAnswer;

        // determine the best move FOR BLACK
        if (first.blackWins() && !second.blackWins()) {
            // first is better, we win! :)
            blacksAnswer = true;
        } else if (!first.blackWins() && second.blackWins()) {
            // second is better, we win! :)
            blacksAnswer = false;
        } else if (first.blackWins() && second.blackWins()) {
            // both moves make us win, return the quickest win, or the
            // move with the lowest coordinate new stone
            if (first.getMaxTurns() < second.getMaxTurns()) {
                blacksAnswer = true;
            } else if (first.getMaxTurns() > second.getMaxTurns()) {
                blacksAnswer = false;
            } else {
                // both turns are equally good, return the lowest
                // coordinate move (makes answer deterministic)
                if (first.getX() != second.getX()) {
                    blacksAnswer = first.getX() < second.getX();
                } else {
                    blacksAnswer = first.getY() < second.getY();
                }
            }
        } else {
            // neither moves make us win :( delay loosing as long as
            // possible, if equal return the
            // move with the lowest coordinate new stone
            if (first.getMaxTurns() > second.getMaxTurns()) {
                blacksAnswer = true;
            } else if (first.getMaxTurns() < second.getMaxTurns()) {
                blacksAnswer = false;
            } else {
                // both turns are equally bad, return the lowest
                // coordinate move (makes answer deterministic)
                if (first.getX() != second.getX()) {
                    blacksAnswer = first.getX() < second.getX();
                } else {
                    blacksAnswer = first.getY() < second.getY();
                }
            }
        }

        if (blackPlayer) {
            return blacksAnswer;
        } else {
            // white player decides, and always wants the opposite of black :)
            return !blacksAnswer;
        }
    }

    /**
     * Returns the best move possible from the current players perspective
     * 
     * @param board
     *            the board to investigate.
     * 
     * @return the optimal move for the current player.
     */
    public static Move bestMove(Board board) {
        if (DEBUG) {
            System.err.println(board);
        }

        if (board.getWinner() == Board.BLACK) {
            // black wins in 0 moves
            return new Move(-1, -1, true, 0);
        } else if (board.getWinner() == Board.WHITE) {
            // white wins in 0 moves
            return new Move(-1, -1, false, 0);
        }

        // result variables
        int bestX = -1;
        int bestY = -1;
        Move bestMove = null;

        int size = board.size();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.isEmpty(x, y)) {
                    // place a stone
                    Board child = null;
                    try {
                        child = new Board(board, x, y);
                    } catch (Exception e) {
                        System.err.println("could not place stone,"
                                + " but space should be empty");
                        System.exit(1);
                    }

                    Move childBestMove = bestMove(child);

                    if (isBetter(childBestMove, bestMove,
                        board.currentPlayerIsBlack())) {
                        bestMove = childBestMove;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        Move result =
            new Move(bestX, bestY, bestMove.blackWins(),
                    bestMove.getMaxTurns() + 1);
        if (DEBUG) {
            printIndented(board.turn(), result.toString());
        }
        return result;
    }

    private static void printUsage(PrintStream out) {
        out.println();
        out.println("Go: Calculate the best move for a Go board. A board can be"
                + " either randomly generated or given as a file");
        out.println();
        out.println("USAGE: Go [options]");
        out.println();
        out.println("--size SIZE\t\tsize of the board");
        out.println("--stones STONES\t\tnumber of stones to place on the board");
        out.println("--seed SEED\t\tseed to use for the random generator used ");
        out.println("\t\t\tto create the board");
        out.println();
        out.println("--file FILENAME\t\tDo not randomly create a board.");
        out.println("\t\t\tInstead load it from the given file");
        out.println();
        out.println("--help\t\t\tThis message");
        out.println();

    }

    private synchronized void generateJobs(int numberOfJobs, Board initialBoard) {
        int depth = 0;
        int prevWorkSize = -1;
        while (workQ.size() < numberOfJobs) {
            workQ.clear();
            depth++;
            generateBoards(initialBoard, depth);
            workSize = workQ.size();

            System.err.println("generated " + workQ.size() + " jobs of depth "
                    + depth);

            if (workQ.size() < prevWorkSize || workQ.size() == 0) {
                // next depth will only generate less jobs, not more.
                // Re-calculate jobq one depth back
                workQ.clear();
                depth = depth - 1;
                generateBoards(initialBoard, depth);
                workSize = workQ.size();
                System.err.println("generation depth is: " + depth);
                return;
            }
        }
        System.err.println("generation depth is: " + depth);
    }

    private synchronized void generateBoards(Board board, int depth) {

        // check if board is an end board
        if (board.getWinner() == Board.BLACK
                || board.getWinner() == Board.WHITE) {
            return;
        }

        // check if we reached the required depth, and add board to work q
        if (depth <= 0) {
            workQ.add((board));
            return;
        }

        int size = board.size();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.isEmpty(x, y)) {
                    // place a stone
                    Board child = null;
                    try {
                        child = new Board(board, x, y);
                    } catch (Exception e) {
                        System.err.println("could not place stone,"
                                + " but space should be empty");
                        System.exit(1);
                    }

                    generateBoards(child, depth - 1);
                }
            }
        }
    }

    public synchronized Move calculateResult(Board board) {
        if (DEBUG) {
            System.err.println(board);
        }

        if (board.getWinner() == Board.BLACK) {
            // black wins in 0 moves
            return new Move(-1, -1, true, 0);
        } else if (board.getWinner() == Board.WHITE) {
            // white wins in 0 moves
            return new Move(-1, -1, false, 0);
        }

        // result variables
        int bestX = -1;
        int bestY = -1;
        Move bestMove = null;

        int size = board.size();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.isEmpty(x, y)) {
                    // place a stone
                    Board child = null;
                    try {
                        child = new Board(board, x, y);
                    } catch (Exception e) {
                        System.err.println("could not place stone,"
                                + " but space should be empty");
                        System.exit(1);
                    }

                    Move childBestMove = results.get(child.ID());

                    if (childBestMove == null) {
                        // System.err.println("calculating result ourselves");
                        // calculate it ourselves
                        childBestMove = calculateResult(child);
                    } else {
                        // System.err.println("fetched result from result
                        // map!");
                    }

                    if (isBetter(childBestMove, bestMove,
                        board.currentPlayerIsBlack())) {
                        bestMove = childBestMove;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        Move result =
            new Move(bestX, bestY, bestMove.blackWins(),
                    bestMove.getMaxTurns() + 1);
        if (DEBUG) {
            printIndented(board.turn(), result.toString());
        }
        return result;
    }

    private void runWorkers(int nrOfWorkers) {
        Thread[] workers = new Thread[nrOfWorkers];

        // create and start workers
        for (int i = 0; i < nrOfWorkers; i++) {
            workers[i] = new Thread(this);
            workers[i].start();
        }

        // wait until workers are finished
        for (int i = 0; i < nrOfWorkers; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

    private void startIbis() {
        receiver.enableConnections();
        receiver.enableMessageUpcalls();
    }

    private synchronized Board getLocalWork() {
        // System.err.println("workQ now of size: " + workQ.size());
        Board result = workQ.poll();

        return result;
    }

    private synchronized void addLocalResult(String boardID, Move result) {
        results.put(boardID, result);

        nrOfResults++;

        if (nrOfResults >= workSize) {
            notifyAll();
        }
    }

    // wait for workers to be done (or 10 seconds)
    private synchronized void waitUntilDone() {
        if (nrOfResults < workSize) {
            try {
                wait(10000);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

    private Board getWork(String previousBoardID, Move previousResult)
            throws IOException, ClassNotFoundException {
        if (isServer) {
            addLocalResult(previousBoardID, previousResult);
            return getLocalWork();
        }

        SendPort sendPort = myIbis.createSendPort(requestPortType);
        sendPort.connect(server, "server", 60000, true);

        // Create a receive port for receiving the reply from the server
        // this receive port does not need a name, as we will send the
        // ReceivePortIdentifier to the server directly
        ReceivePort receivePort = myIbis.createReceivePort(replyPortType, null);
        receivePort.enableConnections();

        // Send the request message. This message contains the identifier of
        // our receive port so the server knows where to send the reply
        WriteMessage request = sendPort.newMessage();
        request.writeObject(receivePort.identifier());
        request.writeObject(previousBoardID);
        request.writeObject(previousResult);
        request.finish();

        ReadMessage reply = receivePort.receive();
        Board result = (Board) reply.readObject();
        reply.finish();

        // Close ports.
        sendPort.close();
        receivePort.close();

        return result;
    }

    public void run() {
        int jobsDone = 0;

        try {
            Board work = getWork(null, null);
            while (true) {

                if (work == null) {
                    // workQ empty, we are done
                    System.err.println("did " + jobsDone + " jobs");
                    return;
                }

                Move result = bestMove(work);

                work = getWork(work.ID(), result);
                jobsDone++;
            }
        } catch (Exception e) {
            System.err.println("eep! error on getting work");
            e.printStackTrace(System.err);
        }
    }

    public void upcall(ReadMessage message) throws IOException,
            ClassNotFoundException {
        ReceivePortIdentifier requestor =
            (ReceivePortIdentifier) message.readObject();
        String previousBoardID = (String) message.readObject();
        Move previousResult = (Move) message.readObject();

        // System.err.println("received request from: " + requestor);

        // finish the request message. This MUST be done before sending
        // the reply message. It ALSO means Ibis may now call this upcall
        // method agian with the next request message
        message.finish();

        if (previousBoardID != null) {
            addLocalResult(previousBoardID, previousResult);
        }

        // create a sendport for the reply
        SendPort replyPort = myIbis.createSendPort(replyPortType);

        // connect to the requestor's receive port
        replyPort.connect(requestor);

        // create a reply message
        WriteMessage reply = replyPort.newMessage();
        reply.writeObject(getLocalWork());
        reply.finish();

        replyPort.close();

    }

    /**
     * Main function
     * 
     * @param args
     *            command line arguments
     * @throws Exception
     *             in case of trouble
     */
    public static void main(String[] args) throws Exception {
        Board board;
        int size = 3;
        int stones = 1;
        int seed = 0;
        int jobsPerCPU = 25;
        int threads = Runtime.getRuntime().availableProcessors();
        String fileName = null;

        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].equalsIgnoreCase("--size")) {
                    i++;
                    size = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("--stones")) {
                    i++;
                    stones = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("--seed")) {
                    i++;
                    seed = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("--threads")) {
                    i++;
                    threads = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("--jobs-per-cpu")) {
                    i++;
                    jobsPerCPU = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("--file")) {
                    i++;
                    fileName = args[i];
                } else if (args[i].equalsIgnoreCase("--help")) {
                    printUsage(System.out);
                    // System.exit upsets Satin :(
                    return;
                } else {
                    System.err.println("unknown command line option: "
                            + args[i]);
                    printUsage(System.err);
                    // System.exit upsets Satin :(
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("error on parsing argument, expecting integer: "
                        + args[i]);
                // System.exit upsets Satin :(
                return;
            }
        }

        Go go = new Go();
        
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}

        if (go.isServer) {

            if (fileName == null) {
                board = new Board(size, stones, seed);
            } else {
                board = new Board(fileName);
            }

            System.out.println(board);

            int numberOfJobs = threads * jobsPerCPU * go.poolSize();

            go.generateJobs(numberOfJobs, board);

            synchronized (go) {
                System.err.println("workQ now of length: " + go.workQ.size());
            }

            long start = System.currentTimeMillis();

            go.startIbis();

            go.runWorkers(threads);

            go.waitUntilDone();

            synchronized (go) {
                System.err.println("result map size = " + go.results.size());
            }

            Move bestMove = go.calculateResult(board);

            System.out.println("best move = " + bestMove);

            long end = System.currentTimeMillis();
            System.err.println("app took " + (end - start) + "ms");

        } else {
            // just create workers
            go.runWorkers(threads);
        }

        go.end();
    }

    private int poolSize() {
        return myIbis.registry().getPoolSize();
    }

    private void end() throws IOException {
        if (receiver != null) {
            // give workers a change to get the "null" job and finish
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // IGNORE
            }
            receiver.close();
        }
        myIbis.end();
    }

}

package go.dac;

import java.io.PrintStream;

public class Main {

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
                } else if (args[i].equalsIgnoreCase("--file")) {
                    i++;
                    fileName = args[i];
                } else if (args[i].equalsIgnoreCase("--help")) {
                    Main.printUsage(System.out);
                    return;
                } else {
                    System.err.println("unknown command line option: "
                            + args[i]);
                    Main.printUsage(System.err);
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("error on parsing argument, expecting integer: "
                        + args[i]);
                return;
            }
        }
    
        Thread.sleep(5000);
        
        Go go = new Go();
        
        // register shutdown hook
        try {
            Runtime.getRuntime().addShutdownHook(new Shutdown(go));
        } catch (Exception e) {
            System.err.println("warning: could not registry shutdown hook");
        }
        
        
        if (go.isMaster()) {
            if (fileName == null) {
                board = new Board(size, stones, seed);
            } else {
                board = new Board(fileName);
            }
    
            System.out.println("Master @ " + go.getIbisIdentifier());
            System.out.println(board.print());

            //add initial job to job Q
            go.addToJobQ(new Job(go.getIbisIdentifier(), null, board, null));
             
            long start = System.currentTimeMillis();
    
            runWorkers(threads, go);
    
            Move bestMove = go.getResult();
    
            System.out.println("GO[" + size + "," + stones + "," + seed + "] = " + bestMove);

            long end = System.currentTimeMillis();
            System.err.println("app took " + (end - start) + "ms");
        } else {
            // just create workers
            runWorkers(threads, go);
        }

        go.end();
        
        go.printStatistics();

    }
    
    static void runWorkers(int nrOfWorkers, Go go) {
        Worker[] workers = new Worker[nrOfWorkers];

        // create and start workers
        for (int i = 0; i < nrOfWorkers; i++) {
            workers[i] = new Worker(go, i);
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

    static void printUsage(PrintStream out) {
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
    
    
    private static class Shutdown extends Thread {
        private final Go go;

        Shutdown(Go go) {
            this.go = go;
        }

        public void run() {
            go.terminate();
        }
    }


}

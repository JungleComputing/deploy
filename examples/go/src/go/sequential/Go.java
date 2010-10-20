package go.sequential;

import java.io.PrintStream;

public final class Go {

    public static boolean DEBUG = false;

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

        if (fileName == null) {
            board = new Board(size, stones, seed);
        } else {
            board = new Board(fileName);
        }

        System.out.println(board);

        long start = System.currentTimeMillis();

        Move bestMove = bestMove(board);

        System.out.println("best move = " + bestMove);

        long end = System.currentTimeMillis();
        System.err.println("app took " + (end - start) + "ms");

    }
}

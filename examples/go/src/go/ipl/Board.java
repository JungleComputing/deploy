package go.ipl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Random;

/**
 * Class representing a Go board with one or more stones on it.
 */
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    // turns debugging prints on or off
    private static final boolean DEBUG = false;

    // pretty print boards using unicode characters
    private static final boolean UNICODE = true;

    // constants representing states or results

    /**
     * Constant representing a undetermined result
     */
    public static final byte UNDETERMINED = 0;

    /**
     * Constant representing an empty space on the board
     */
    private static final byte EMPTY = 0;

    /**
     * Constant representing the black player
     */
    public static final byte BLACK = 1;

    /**
     * Constant representing the white player
     */
    public static final byte WHITE = 2;

    // array of board positions containing if this position is EMPTY, a BLACK
    // stone or a WHITE stone
    private final byte[] data;

    // size of the board (a board is a "size x size" square)
    private final int size;

    // if true black has to place the next stone, if falls white.
    private final boolean currentPlayerIsBlack;

    // turn number. Starts at 1, incremented each time a stone is placed.
    private final int turn;

    private final byte winner;

    /**
     * Creates an empty board of the given size
     * 
     * @param size
     *            the size of the new board.
     */
    public Board(int size) {
        this.size = size;
        data = new byte[size * size];
        currentPlayerIsBlack = true;
        turn = 1;

        winner = determineWinner();
    }

    /**
     * Generate a "random" board. Not really random, as this function will give
     * the same result when called with the same parameters
     * 
     * @param size
     *            size of the board.
     * @param initialStones
     *            number of stones randomly placed on the board.
     * @param seed
     *            seed of the random number generator used by this function.
     */
    public Board(int size, int initialStones, long seed) {
        this.size = size;
        this.turn = 1;

        data = new byte[size * size];

        if ((initialStones) > (size * size)) {
            System.err.println("creating a more than full board");
            System.exit(1);
        }

        Random random = new Random(seed);

        for (int i = 0; i < initialStones
                && (determineWinner() == UNDETERMINED); i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            boolean black = (i % 2 == 0);
            while (!isEmpty(x, y)) {
                x = random.nextInt(size);
                y = random.nextInt(size);
            }

            if (DEBUG) {
                System.err.println(x + "," + y + " = black");
            }

            if (black) {
                // place black
                data[(size * x) + y] = BLACK;
            } else {
                // place white
                data[(size * x) + y] = WHITE;
            }
        }

        int removed = 0;
        while (determineWinner() != UNDETERMINED) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);

            if (!isEmpty(x, y)) {
                data[(size * x) + y] = EMPTY;
                removed++;
            }
        }
        if (DEBUG && removed > 0) {
            System.err.println("removed " + removed + " stones");
        }

        winner = UNDETERMINED;
        currentPlayerIsBlack = random.nextBoolean();
    }

    private char getChar(FileReader reader) throws IOException {
        char nextChar;

        do {
            int value = reader.read();

            if (value < 0) {
                throw new IOException("could not board from file");
            }

            nextChar = (char) value;
        } while (Character.isWhitespace(nextChar));

        return nextChar;
    }

    /**
     * Read a board from a file
     * 
     * @param fileName
     *            name of the file to read the board from.
     * 
     * @throws FileNotFoundException
     *             when the file was not found.
     * @throws IOException
     *             when the board cannot succesfully be read from the file.
     */
    public Board(String fileName) throws IOException {
        FileReader reader = new FileReader(fileName);
        String sizeString = "";

        char nextChar;

        do {
            nextChar = getChar(reader);

            sizeString += nextChar;

        } while (Character.isDigit(nextChar));

        try {
            size = Integer.parseInt(sizeString);
        } catch (NumberFormatException e) {
            throw new IOException("cannot read size from file, got: "
                    + sizeString);
        }

        char currentPlayer = getChar(reader);

        if (currentPlayer == 'B' || currentPlayer == 'b') {
            currentPlayerIsBlack = true;
        } else if (currentPlayer == 'W' || currentPlayer == 'w') {
            currentPlayerIsBlack = false;
        } else {
            throw new IOException(
                    "could not determine current player, expected 'B' or 'W', got: "
                            + currentPlayer);
        }

        this.data = new byte[size * size];

        for (int i = 0; i < data.length; i++) {
            nextChar = getChar(reader);

            if (nextChar == 'B' || nextChar == 'b' || nextChar == '\u25cf') {
                data[i] = BLACK;
            } else if (nextChar == 'W' || nextChar == 'w'
                    || nextChar == '\u25cb') {
                data[i] = WHITE;
            } else {
                data[i] = EMPTY;
            }
        }
        reader.close();

        turn = 1;

        winner = determineWinner();

    }

    /**
     * Creates a new board by placing a stone at the given position on the given
     * board.
     * 
     * @param original
     *            the original board.
     * @param x
     *            the x coordinate of the new stone.
     * @param y
     *            the y coordinate of the new stone.
     * 
     * @throws Exception
     *             when the new stone could not be placed
     */
    public Board(Board original, int x, int y) throws Exception {
        if (!original.isEmpty(x, y)) {
            throw new Exception("cannot place stone at " + x + "," + y);
        }

        this.size = original.size;
        this.data = new byte[original.data.length];

        // invert current player, add one to the current turn
        this.currentPlayerIsBlack = !original.currentPlayerIsBlack;
        this.turn = original.turn + 1;

        // copy data
        System.arraycopy(original.data, 0, this.data, 0, original.data.length);

        // place new stone
        if (original.currentPlayerIsBlack) {
            this.data[(size * x) + y] = BLACK;
        } else {
            this.data[(size * x) + y] = WHITE;
        }

        winner = determineWinner();
    }

    /**
     * Returns if the board is empty at the given position.
     * 
     * @param x
     *            the x coordinate of the checked position.
     * @param y
     *            the y coordinate of the checked position.
     * 
     * @return if the board is empty at the given position.
     */
    public boolean isEmpty(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return false;
        }

        return data[(size * x) + y] == EMPTY;
    }

    /**
     * Returns if there is a black stone at the given position.
     * 
     * @param x
     *            the x coordinate of the checked position.
     * @param y
     *            the y coordinate of the checked position.
     * 
     * @return if there is a black stone at the given position.
     */
    public boolean isBlack(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return false;
        }

        return data[(size * x) + y] == BLACK;
    }

    /**
     * Returns if there is a white stone at the given position.
     * 
     * @param x
     *            the x coordinate of the checked position.
     * @param y
     *            the y coordinate of the checked position.
     * 
     * @return if there is a white stone at the given position.
     */
    public boolean isWhite(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return false;
        }

        return data[(size * x) + y] == WHITE;
    }

    /**
     * Returns the size of this board.
     * 
     * @return the size of this board.
     */
    public int size() {
        return size;
    }

    /**
     * Returns if black is to place the next stone.
     * 
     * @return true is black is to place the next stone, false if white is to
     *         place the next stone.
     */
    public boolean currentPlayerIsBlack() {
        return currentPlayerIsBlack;
    }

    /**
     * Returns the number of stones placed since this board was created.
     * 
     * @return the number of stones placed since this board was created.
     */
    public int turn() {
        return turn;
    }

    /**
     * Check if the stone at the given position has any freedoms.
     * 
     * @param x
     *            the x coordinate of the stone to check,
     * @param y
     *            the y coordinate of the stone to check.
     * @param type
     *            the type of stone we are checking.
     * @param checked
     *            a set of booleans representing already checked stones.
     * 
     * @return true if the stone at the current position has freedoms or one of
     *         its neightbours has, or the space is empty and false otherwise.
     */
    private boolean hasFreedom(int x, int y, byte type, BitSet checked) {
        if (DEBUG) {
            System.err.println("hasFreedom(" + x + ", " + y + ", " + type
                    + ", " + checked);
        }

        // outside of board, no freedom here
        if (x < 0 || x >= size || y < 0 || y >= size) {
            if (DEBUG) {
                System.err.println("outside of board");
            }
            return false;
        }

        // type of this stone
        byte thisType = data[(size * x) + y];

        if (thisType == EMPTY) {
            // this is a freedom
            if (DEBUG) {
                System.err.println("empty!");
            }
            return true;
        } else if (type == WHITE && thisType == BLACK) {
            // opposite stone type
            if (DEBUG) {
                System.err.println("!white");
            }
            return false;
        } else if (type == BLACK && thisType == WHITE) {
            // opposite stone type
            if (DEBUG) {
                System.err.println("!black");
            }
            return false;
        } else if (checked.get((size * x) + y)) {
            // this stone has already been checked
            if (DEBUG) {
                System.err.println("already checked");
            }
            return false;
        }
        checked.set((size * x) + y);

        if (DEBUG) {
            System.err.println("checking neighbours:");
        }

        if (DEBUG) {
            System.err.println(x + "," + y + " UP");
        }
        boolean leftHasFreedom = hasFreedom(x - 1, y, type, checked);
        if (DEBUG) {
            System.err.println(x + "," + y + " DOWN");
        }
        boolean rightHasFreedom = hasFreedom(x + 1, y, type, checked);
        if (DEBUG) {
            System.err.println(x + "," + y + " LEFT");
        }
        boolean upHasFreedom = hasFreedom(x, y - 1, type, checked);
        if (DEBUG) {
            System.err.println(x + "," + y + " RIGHT");
        }
        boolean downHasFreedom = hasFreedom(x, y + 1, type, checked);

        return upHasFreedom || downHasFreedom || leftHasFreedom
                || rightHasFreedom;
    }

    /**
     * Determines if any of the two player won. Looks at each stone and checks
     * if it or any of its neighbours has any freedoms (neighbouring empty
     * space). Remebers which stones are already checked for efficiency and to
     * prevent endless loops
     * 
     * 
     * @return UNDETERMINED if there is no winner, BLACK if black won and WHITE
     *         if white won.
     */
    private byte determineWinner() {
        BitSet checked = new BitSet();
        boolean blackWon = false;
        boolean whiteWon = false;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (DEBUG) {
                    System.err.println("checking " + x + "," + y);
                }
                if (!checked.get((size * x) + y)) {
                    byte type = data[(size * x) + y];

                    if (type == EMPTY) {
                        if (DEBUG) {
                            System.err.println(x + "," + y + " empty");
                        }
                    } else if (!hasFreedom(x, y, type, checked)) {
                        if (DEBUG) {
                            System.err.println(x + "," + y + " dead");
                        }

                        if (type == WHITE) {
                            blackWon = true;
                        } else if (type == BLACK) {
                            whiteWon = true;
                        }
                    }
                } else {
                    if (DEBUG) {
                        System.err.println(x + "," + y + " already checked");
                    }
                }
            }

        }

        if (blackWon && whiteWon) {
            // last player to put a stone wins
            if (currentPlayerIsBlack) {
                return WHITE;
            } else {
                return BLACK;
            }
        } else if (blackWon) {
            return BLACK;
        } else if (whiteWon) {
            return WHITE;
        }
        return UNDETERMINED;
    }

    /**
     * Returns if any of the two player won.
     * 
     * @return UNDETERMINED if there is no winner, BLACK if black won and WHITE
     *         if white won.
     */
    public byte getWinner() {
        return winner;
    }

    /**
     * Returns the right symbol for printing a "blank" at the given postion.
     * 
     * @param x
     *            the x coordinate of the empty space.
     * @param y
     *            the y coordinate of the empty space.
     * 
     * 
     * @return a character suitable for denoting the current position
     */
    private char emptyChar(int x, int y) {
        if (!UNICODE) {
            return '.';
        }
        if (x == 0 && y == 0) {
            return '\u250c';
        } else if (x == 0 && y == (size - 1)) {
            return '\u2510';
        } else if (x == 0) {
            return '\u252c';
        } else if (x == (size - 1) && y == 0) {
            return '\u2514';
        } else if (x == (size - 1) && y == (size - 1)) {
            return '\u2518';
        } else if (x == (size - 1)) {
            return '\u2534';
        } else if (y == 0) {
            return '\u251c';
        } else if (y == (size - 1)) {
            return '\u2524';
        } else {
            return '\u253C';
        }
    }

    /**
     * Returns a string representation of this board, indended to the "turn"
     * number of this board. Warning: includes Unicode ASCII Art :)
     * 
     * @return a string possibly indented representation of this board.
     */
    public String toString() {
        String result = "";
        for (int i = 0; i < turn; i++) {
            result += "\t";
        }

        byte winner = determineWinner();
        if (winner == BLACK) {
            result += "BLACK wins";
        } else if (winner == WHITE) {
            result += "WHITE wins";
        } else if (currentPlayerIsBlack) {
            result += "BLACK to go next";
        } else {
            result += "WHITE to go next";
        }

        for (int x = 0; x < size; x++) {
            result += "\n";
            for (int i = 0; i < turn; i++) {
                result += "\t";
            }
            for (int y = 0; y < size; y++) {
                if (isEmpty(x, y)) {
                    result += emptyChar(x, y);
                } else if (isBlack(x, y)) {
                    if (UNICODE) {
                        result += '\u25cf';
                    } else {
                        result += 'B';
                    }
                } else if (isWhite(x, y)) {
                    if (UNICODE) {
                        result += '\u25cb';
                    } else {
                        result += 'W';
                    }

                }
            }
        }
        return result;
    }

    public String ID() {
        String result = "";

        if (currentPlayerIsBlack) {
            result += "B";
        } else {
            result += "W";
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (isEmpty(x, y)) {
                    result += ".";
                } else if (isBlack(x, y)) {
                    result += 'B';
                } else if (isWhite(x, y)) {
                    result += 'W';
                }
            }
        }
        return result;
    }

    /**
     * Writes this board to the given file as text.
     * 
     * @param fileName
     *            the name of the file.
     * @throws IOException
     *             in case the file cannot be written.
     */
    public void tofile(String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName, false);
        writer.append(Integer.toString(size) + "\n");
        if (currentPlayerIsBlack) {
            writer.append("B");
        } else {
            writer.append("W");
        }
        for (int x = 0; x < size; x++) {
            writer.append("\n");
            for (int y = 0; y < size; y++) {
                if (isEmpty(x, y)) {
                    writer.append(".");
                } else if (isBlack(x, y)) {
                    writer.append("B");
                } else if (isWhite(x, y)) {
                    writer.append("W");
                }

            }
        }
        writer.flush();
        writer.close();
    }

    @Override
    public boolean equals(Object object) {
        System.err.println("comparing " + this + " to " + object);

        if (!(object instanceof Board)) {
            return false;
        }

        Board other = (Board) object;

        if (size != other.size
                || currentPlayerIsBlack != other.currentPlayerIsBlack) {
            return false;
        }

        for (int i = 0; i < data.length; i++) {
            if (data[i] != other.data[i]) {
                return false;
            }
        }

        return true;
    }

}

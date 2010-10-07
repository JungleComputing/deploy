package go.dac;

import java.io.Serializable;

/**
 * Represents a move, and the result this move has on the result of the game
 */
public final class Move implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int x;

    private final int y;

    private final boolean blackWins;

    private final int maxTurns;

    /**
     * @param x
     *            x coordinate of the new stone
     * @param y
     *            y coordinate of the new stone
     * @param blackWins
     *            does this move result in black or white winning
     * @param maxTurns
     *            how long does the winner have to "wait" until he/she wins
     */
    public Move(int x, int y, boolean blackWins, int maxTurns) {
        this.x = x;
        this.y = y;
        this.blackWins = blackWins;
        this.maxTurns = maxTurns;

    }

    /**
     * @return the blackWins
     */
    public boolean blackWins() {
        return blackWins;
    }

    /**
     * @return the maxTurns
     */
    public int getMaxTurns() {
        return maxTurns;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    public String toString() {
        if (blackWins) {
            return "BLACK wins with move " + x + "," + y + " in at most "
                    + maxTurns + " turns";
        } else {
            return "WHITE wins with move " + x + "," + y + " in at most "
                    + maxTurns + " turns";
        }

    }

    /**
     * Returns if the first or the second move is better as far as the given
     * player is concerned
     * 
     * @param second
     *            Second possible move
     * @param blackPlayer
     *            if true black is deciding, if false white is
     * @return true if the first move is better, false otherwise
     */
    boolean isBetter(Move second, boolean blackPlayer) {
        if (second == null) {
            return true;
        }

        boolean blacksAnswer;

        // determine the best move FOR BLACK
        if (this.blackWins() && !second.blackWins()) {
            // this is better, we win! :)
            blacksAnswer = true;
        } else if (!this.blackWins() && second.blackWins()) {
            // second is better, we win! :)
            blacksAnswer = false;
        } else if (this.blackWins() && second.blackWins()) {
            // both moves make us win, return the quickest win, or the
            // move with the lowest coordinate new stone
            if (this.getMaxTurns() < second.getMaxTurns()) {
                blacksAnswer = true;
            } else if (this.getMaxTurns() > second.getMaxTurns()) {
                blacksAnswer = false;
            } else {
                // both turns are equally good, return the lowest
                // coordinate move (makes answer deterministic)
                if (this.getX() != second.getX()) {
                    blacksAnswer = this.getX() < second.getX();
                } else {
                    blacksAnswer = this.getY() < second.getY();
                }
            }
        } else {
            // neither moves make us win :( delay loosing as long as
            // possible, if equal return the
            // move with the lowest coordinate new stone
            if (this.getMaxTurns() > second.getMaxTurns()) {
                blacksAnswer = true;
            } else if (this.getMaxTurns() < second.getMaxTurns()) {
                blacksAnswer = false;
            } else {
                // both turns are equally bad, return the lowest
                // coordinate move (makes answer deterministic)
                if (this.getX() != second.getX()) {
                    blacksAnswer = this.getX() < second.getX();
                } else {
                    blacksAnswer = this.getY() < second.getY();
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
}

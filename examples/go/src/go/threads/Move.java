package go.threads;

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

}

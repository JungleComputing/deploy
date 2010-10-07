package go.daccripple;

import java.io.Serializable;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.ipl.IbisIdentifier;

public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(Job.class);
    
    private final IbisIdentifier[] dependantNodes;

    private final IbisIdentifier source;

    private final UUID id;

    private final UUID parentID;

    private final Board board;

    private UUID[] childrenIDs;

    private Board[] children;

    private Move[] results;
    
    private static IbisIdentifier[] createDependantList(IbisIdentifier[] originalNodes, IbisIdentifier newNode) {
        if (originalNodes == null) {
            return new IbisIdentifier[] { newNode };
        }
        
        for(IbisIdentifier identifier: originalNodes) {
            if (identifier.equals(newNode)) {
                //list already contains new node
                return originalNodes;
            }
        }
        IbisIdentifier[] result = new IbisIdentifier[originalNodes.length + 1];

        System.arraycopy(originalNodes, 0, result, 0, originalNodes.length);
        result[result.length - 1] = newNode;
        
        return result;
    }

    Job(IbisIdentifier source, UUID parentID, Board board, IbisIdentifier[] dependantNodes) {
        this.source = source;
        this.parentID = parentID;
        this.board = board;

        id = UUID.randomUUID();

        if (source == null) {
            throw new Error("Source cannot be null!");
        }

        if (board == null) {
            throw new Error("Board cannot be null!");
        }
        
        this.dependantNodes = createDependantList(dependantNodes, source);

        childrenIDs = null;
        children = null;
        results = null;
    }

    public Board getBoard() {
        return board;
    }

    public UUID getID() {
        return id;
    }

    public UUID getParentID() {
        return parentID;
    }

    public IbisIdentifier getSource() {
        return source;
    }

    public synchronized boolean addResult(UUID childID, Move move) {
        boolean allResults = true;
        for (int i = 0; i < childrenIDs.length; i++) {
            if (childrenIDs[i].equals(childID)) {
                if (results[i] != null) {
                    logger.error("EEP! Already got result for " + childID,
                        new Exception());
                } else {
                    results[i] = move;
                }
            }
            if (results[i] == null) {
                allResults = false;
            }
        }
        return allResults;
    }

    /**
     * Returns the best possible move of this job.
     * 
     * @return the best move, or null if this job has just spawned children.
     */
    public synchronized Move result() {
        if (board.isLeaf()) {
            // do calculation now
            return board.bestMove();
        } else if (children == null) {
            return null;
        } else if (missing() > 0) {
            logger.error("tried to get result for waiting job: " + this);
            System.exit(1);
            return null;
        } else {
            // use result of children to calculate result
            return board.bestMove(children, results);
        }
    }

    public synchronized Job[] getChildJobs(IbisIdentifier identifier) {
        children = board.getChildren();
        results = new Move[children.length];
        childrenIDs = new UUID[children.length];

        Job[] result = new Job[children.length];
        for (int i = 0; i < children.length; i++) {
            result[i] = new Job(identifier, id, children[i], dependantNodes);
            childrenIDs[i] = result[i].getID();
        }

        return result;
    }

    public String toString() {
        return "Job " + id + " for: " + board + " with " + nrOfChildren()
                + " children missing " + missing() + " results";
    }

    private synchronized int missing() {
        if (results == null) {
            return -1;
        }
        int result = 0;

        for (Move move : results) {
            if (move == null) {
                result++;
            }
        }
        return result;
    }

    private synchronized int nrOfChildren() {
        if (children == null) {
            return -1;
        }
        return children.length;
    }

    public boolean dependsOn(IbisIdentifier ibis) {
        for(IbisIdentifier dependancy: dependantNodes) {
            if (dependancy.equals(ibis)) {
                return true;
            }
        }
        return false;
    }
}

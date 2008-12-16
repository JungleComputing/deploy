package ibis.deploy;

/**
 * Thrown if a job/cluster/application of the given name already exists in the
 * experiment/grid/applicationSet.
 * 
 */
public class AlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

    public AlreadyExistsException(String message) {
        super(message);
    }

}

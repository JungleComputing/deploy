package ibis.deploy;

/**
 * Thrown if a job/resource/application of the given name already exists in the
 * experiment/jungle/applicationSet.
 * 
 */
public class AlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

    public AlreadyExistsException(String message) {
        super(message);
    }

}

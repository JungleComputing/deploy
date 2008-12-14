package ibis.deploy;

public enum State {

    /**
     * The {@link Job} state is unknown for some reason. It might be a network
     * problem.
     */
    UNKNOWN,

    /**
     * Initial state indicator.
     * 
     * The {@link Job} has been constructed.
     */
    INITIAL,

    /**
     * The {@link Job} is waiting for an event (usually the hub has not been
     * started yet).
     */
    WAITING,

    /**
     * The input files of the {@link Job} are being pre staged using Rsync.
     */
    PRE_STAGE_RSYNC,

    /**
     * The {@link Job} is being submitted to the underlying middleware.
     */
    SUBMITTING,
    
    /**
     * The {@link Job} is submitted to the underlying middleware.
     */
    SUBMITTED,

    /**
     * The input files of the {@link Job} are being pre staged.
     */
    PRE_STAGING,

    /**
     * Scheduled state indicator.
     * 
     * The {@link Job} has been submitted to a resource broker and is scheduled
     * to be executed.
     */
    SCHEDULED,

    /**
     * Running state indicator.
     * 
     * The process of the {@link Job} is executing, or sometimes scheduled to be
     * executed.
     */
    RUNNING,

    /**
     * Deploy state indicator.
     * 
     * The {@link Job} has been started and initialized, and has joined the
     * experiment.
     */
    DEPLOYED,

    /**
     * The output files of the {@link Job} are being post staged.
     */
    POST_STAGING,

    /**
     * Stopped state indicator.
     * 
     * The {@link Job} has properly run. All the cleanup and administration of
     * the {@link Job} is completely done.
     */
    STOPPED,

    /**
     * Error state indicator.
     * 
     * The {@link Job} hasn't properly run. All the cleanup and administration
     * of the {@link Job} is completely done.
     */
    ERROR,

}

package ibis.deploy.gui;

/**
 * Enum detrmining the mode of the IbisDeploy GUI. This influences the elements
 * visable in the GUI, available menu items, etc.
 *  
 * @author Niels Drost
 *
 */
public enum Mode {
    
	/**
	 * Normal mode. Jobs can be created, started, stopped, etc
	 */
    NORMAL,
    
    /**
     * Read only mode. Jobs can be started and stopped, but not
     * created, deleted, edited, etc
     */
    READONLY_WORKSPACE,
    
    /**
     * Monitor only mode. The GUI can in no way be used to influence
     * deployment. Useful to monitor a deployment done by an external program
     * or library (for instance AMUSE)
     */
    MONITORING_ONLY,

}

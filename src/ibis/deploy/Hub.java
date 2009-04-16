package ibis.deploy;

public interface Hub {

    /**
     * Ensure this hub is running, wait for it if needed.
     * 
     * @throws Exception
     *             when the hub could not be started.
     */
    void waitUntilRunning() throws Exception;

    /**
     * Get the address of this hub. Also waits until it is running.
     * 
     * @return the address of this server
     * @throws Exception
     *             if the server failed to start
     */
    String getAddress() throws Exception;

    public void addListener(StateListener listener);

    public State getState();

}

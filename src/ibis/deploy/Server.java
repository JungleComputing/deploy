package ibis.deploy;

import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public interface Server {

    /**
     * Ensure this server is running, wait for it if needed.
     * 
     * @throws Exception
     *             when the server could not be started.
     */
    void waitUntilRunning() throws Exception;

    /**
     * Get the address of this server. Also waits until it is running.
     * 
     * @return the address of this server
     * @throws Exception
     *             if the server failed to start
     */
    String getAddress() throws Exception;

    void addListener(StateListener listener);

    State getState();
    
    boolean isFinished();
    
    void kill();
    
    public RegistryServiceInterface getRegistryService();

    public ManagementServiceInterface getManagementService(); 
    
}

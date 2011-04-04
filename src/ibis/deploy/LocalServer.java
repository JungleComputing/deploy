package ibis.deploy;

import ibis.deploy.util.Colors;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.server.ServerProperties;
import ibis.smartsockets.virtual.VirtualSocketFactory;


import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server or Hub running on the local machine (inside the deploy jvm).
 * 
 */
public class LocalServer implements Server {

    private static final Logger logger = LoggerFactory
            .getLogger(LocalServer.class);

    private final boolean isServer;

    // used in case of a local server
    private final ibis.ipl.server.Server server;

    LocalServer(boolean isServer,boolean verbose, int port)
            throws Exception {
        this.isServer = isServer;

        if (isServer) {
            logger.debug("Starting build-in server");
        } else {
            logger.debug("Starting build-in hub");
        }

        
            Properties properties = new Properties();
            properties.put(ServerProperties.HUB_ONLY, !isServer + "");
            properties.put(ServerProperties.PRINT_ERRORS, "true");
            properties.put(ServerProperties.VIZ_INFO, "D^Ibis Deploy @ local^"
                    + Colors.LOCAL_COLOR + "^0");
            properties.put(ServerProperties.PORT, "" + port);

            if (verbose) {
                properties.put(ServerProperties.PRINT_EVENTS, "true");
                properties.put(ServerProperties.PRINT_STATS, "true");
            }

            server = new ibis.ipl.server.Server(properties);

        logger.debug(server.toString());
    }


    /**
     * Get the address of this server.
     * 
     * @return the address of this server
     * 
     * 
     */
    public String getAddress() {
        return server.getAddress();
    }

    void addHubs(String... hubs) {
        server.addHubs(hubs);
    }

    String[] getHubs() {
        return server.getHubs();
    }

    public void kill() {
        if (server != null) {
            server.end(-1);
        }
    }

    public boolean isFinished() {
        return false;
    }

    public void waitUntilRunning() {
        // NOTHING
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (isServer) {
            return "Local Server @ " + getAddress();
        } else {
            return "Local Hub @ " + getAddress();
        }
    }

    public void addListener(StateListener listener) {
        if (listener != null) {
            // signal listener we're done deploying.
            listener.stateUpdated(State.DEPLOYED, null);
        }
    }

    public State getState() {
        return State.DEPLOYED;
    }

    public VirtualSocketFactory getSocketFactory() {
        return server.getSocketFactory();
    }

    public RegistryServiceInterface getRegistryService() {
        return server.getRegistryService();
    }
    
    public ManagementServiceInterface getManagementService() {
        return server.getManagementService();
    }
}

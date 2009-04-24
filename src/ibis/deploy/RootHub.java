package ibis.deploy;

import ibis.ipl.server.ServerProperties;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server or Hub running on the local machine (inside the deploy jvm). Runs on
 * default server port (usually 8888)
 * 
 */
public class RootHub implements Hub {

    private static final Logger logger = LoggerFactory.getLogger(RootHub.class);

    private final boolean isServer;

    // used in case of a local server
    private final ibis.ipl.server.Server server;

    RootHub(boolean isServer, boolean verbose) throws Exception {
        this.isServer = isServer;

        
        if (isServer) {
            logger.debug("Starting build-in server");
        } else {
            logger.debug("Starting build-in hub");
        }
        
        Properties properties = new Properties();
        properties.put(ServerProperties.HUB_ONLY, !isServer + "");
        properties.put(ServerProperties.PRINT_ERRORS, "true");
        properties.put(ServerProperties.VIZ_INFO, "D^Ibis Deploy @ local^" + Grid.LOCAL_COLOR);


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
        return server.getLocalAddress();
    }

    void addHubs(String... hubs) {
        server.addHubs(hubs);
    }

    String[] getHubs() {
        return server.getHubs();
    }

    void kill() {
        server.end(-1);
    }

    void killAll() {
        // TODO:implement using Smartsockets kill mechanism
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
        }
        return "Local Hub @ " + getAddress();

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
}

package ibis.deploy;

import ibis.server.ServerProperties;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server or Hub running on the local machine (inside the deploy jvm)
 * 
 */
public class LocalServer implements Hub {
    private static final Logger logger = LoggerFactory
            .getLogger(LocalServer.class);

    private final boolean hubOnly;

    // used in case of a local server
    private final ibis.server.Server server;

    /**
     * Start a server/hub locally.
     * 
     * @param hubOnly
     *            if true, only start a hub. If false, also start a server.
     * @throws Exception
     *             if starting the server fails.
     */
    @SuppressWarnings("unchecked")
    LocalServer(boolean hubOnly) throws Exception {
        this.hubOnly = hubOnly;

        if (hubOnly) {
            logger.debug("Starting build-in hub");
        } else {
            logger.debug("Starting build-in server");
        }

        Properties properties = new Properties();
        properties.put(ServerProperties.HUB_ONLY, hubOnly + "");
        // properties.put(ServerProperties.PRINT_ERRORS, "true");
        // properties.put(ServerProperties.PRINT_EVENTS, "true");
        // properties.put(ServerProperties.PRINT_STATS, "true");
        properties.put(ServerProperties.PORT, "0");

        server = new ibis.server.Server(properties);

        logger.debug(server.toString());
    }

    /**
     * Get the address of this server.
     * 
     * @return the address of this server
     * 
     * 
     */
    @Override
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
        //NOTHING
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (hubOnly) {
            return "Local Hub @ " + getAddress();
        }

        return "Local Server @ " + getAddress();
    }
}

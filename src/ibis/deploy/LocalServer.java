package ibis.deploy;

import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.server.ServerProperties;
import ibis.smartsockets.virtual.VirtualSocketFactory;
import ibis.zorilla.Config;

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

    private final boolean isZorilla;

    // used in case of a local Zorilla node
    private final ibis.zorilla.Node zorilla;

    // used in case of a local server
    private final ibis.ipl.server.Server server;

    LocalServer(boolean isServer, boolean isZorilla, boolean verbose, int port)
            throws Exception {
        this.isServer = isServer;
        this.isZorilla = isZorilla;

        if (isZorilla) {
            logger.debug("Starting build-in Zorilla node, with server");
        } else if (isServer) {
            logger.debug("Starting build-in server");
        } else {
            logger.debug("Starting build-in hub");
        }

        if (isZorilla) {
            Properties properties = new Properties();

            properties.put(Config.VIZ_INFO,
                    "DZ^Ibis Deploy with Zorilla Node @ local^"
                            + Grid.LOCAL_COLOR);
            properties.put(Config.PORT, "" + port);
            properties.put(Config.RESOURCE_CORES, "0");

            zorilla = new ibis.zorilla.Node(properties);
            server = zorilla.getIPLServer();
        } else {
            zorilla = null;

            Properties properties = new Properties();
            properties.put(ServerProperties.HUB_ONLY, !isServer + "");
            properties.put(ServerProperties.PRINT_ERRORS, "true");
            properties.put(ServerProperties.VIZ_INFO, "D^Ibis Deploy @ local^"
                    + Grid.LOCAL_COLOR);
            properties.put(ServerProperties.PORT, "" + port);

            if (verbose) {
                properties.put(ServerProperties.PRINT_EVENTS, "true");
                properties.put(ServerProperties.PRINT_STATS, "true");
            }

            server = new ibis.ipl.server.Server(properties);
        }

        logger.debug(server.toString());
    }

    void addZorillaNode(String node) {
        zorilla.discoveryService().addPeer(node);
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
        if (zorilla != null) {
            zorilla.end(-1);
        } else if (server != null) {
            server.end(-1);
        }
    }

    public boolean isFinished() {
        return false;
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
        if (isZorilla) {
            return "Local Zorilla Node @ " + getAddress();
        } else if (isServer) {
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
}

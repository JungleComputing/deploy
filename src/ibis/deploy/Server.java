package ibis.deploy;

import ibis.server.ServerProperties;

import java.io.File;
import java.util.Properties;

import org.gridlab.gat.URI;

/**
 * Running server/hub
 * 
 */
public class Server {

    // used in case of a local server
    private final ibis.server.Server server;

    private final String address;
    
    //URI used to deploy this server.
    private final URI clusterURI;

    /**
     * Start a server/hub locally.
     * 
     * @param libs
     *            jar files/directories needed to start the server
     * @param hubOnly
     *            if true, only start a hub. If false, also start a server.
     * @throws Exception
     *             if starting the server fails.
     */
    public Server(File[] libs, boolean hubOnly) throws Exception {
        Properties properties = new Properties();
        properties.put(ServerProperties.HUB_ONLY, hubOnly + "");

        server = new ibis.server.Server(properties);
        address = server.getLocalAddress();
        clusterURI = null;
        
    }

    /**
     * Start a server/hub on the given cluster.
     * 
     * @param libs
     *            jar files/directories needed to start the server
     * @param fork
     *            if true, start in seperate vm. If false, start in this VM (if
     *            so, libs not supported)
     */
    public Server(File[] libs, Cluster cluster, boolean hubOnly) {
        server = null;
        
        address = null;
        clusterURI = null;

    }

    public String getAddress() {
        return address;
    }

    public String[] getHubs() {
        return null;
    }
    
    public void addHubs(String... hubs) {
    }

    public void killAll() {
    }

    public void kill() {
    }
    
    public URI getClusterURI() {
    	return clusterURI;
    }
}

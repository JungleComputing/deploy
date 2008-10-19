package ibis.deploy;

import java.io.File;

/**
 * Running server (or hub)
 * 
 */
public class Server {

	/**
	 * Start a server locally.
	 * 
	 * @param libs
	 *            jar files/directories needed to start the server
	 * @param fork
	 *            if true, start in seperate vm. If false, start in this VM (if
	 *            so, libs not supported)
	 */
	public Server(File[] libs, boolean fork, boolean hubOnly) {

	}

	/**
	 * Start a server on the given cluster.
	 * 
	 * @param libs
	 *            jar files/directories needed to start the server
	 * @param fork
	 *            if true, start in seperate vm. If false, start in this VM (if
	 *            so, libs not supported)
	 */
	public Server(File[] libs, Cluster cluster, boolean hubOnly) {

	}

}

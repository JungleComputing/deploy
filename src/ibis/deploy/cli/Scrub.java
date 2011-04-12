package ibis.deploy.cli;

import ibis.deploy.Workspace;

import java.io.File;

/**
 * Cleans up property files by reading, then writing them. Also creates a
 * backup, just in case.
 * 
 * @author Niels Drost
 * 
 */
public class Scrub {

	/**
	 * @param arguments
	 *            arguments of application
	 */
	public static void main(String[] arguments) {
		File file = null;

		if (arguments.length != 1) {
			System.err.println("Usage: scrub WORKSPACE_DIR");
			System.exit(0);
		}

		try {
			file = new File(arguments[0]);

			System.err.println("Scrubbing WORKSPACE dir \"" + file + "\"");
			Workspace workspace = new Workspace(file);
			workspace.save(file);
		} catch (Exception e) {
			System.err.println("Exception on scrubbing file");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}

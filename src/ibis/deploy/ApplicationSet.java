package ibis.deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Set of applications. Mostly used as a convenient way of specifying defaults
 * and saving applications to a file.
 * 
 * @author Niels Drost
 * 
 */
public class ApplicationSet {

    private List<Application> applications;

    /**
     * Constructs a application group object from properties stored in the given
     * file. Also constructs the applications in this group.
     * 
     * @param file
     *            the file containing the properties
     * @throws FileNotFoundException
     *             if the given file cannot be found
     * @throws Exception
     *             if reading from the given file fails, or the file has an
     *             extention other than ".applications"
     */
    public ApplicationSet(File file) throws FileNotFoundException, Exception {
        applications = new ArrayList<Application>();

        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        if (!file.getName().endsWith(".applications")) {
            throw new Exception(
                    "application files must have a \".applications\" extension (not: "
                            + file + ")");
        }

        DeployProperties properties = new DeployProperties();
        properties.loadFromFile(file.getAbsolutePath());

        loadSettingsFromProperties(properties, null);
    }

    /**
     * Constructs a application group object from the given properties. Also
     * constructs the applications in this group.
     * 
     * @param properties
     *            properties of the applicationSet
     * @param prefix
     *            prefix to use on all keys
     * @throws Exception
     *             if the applications cannot be initialized
     */
    public ApplicationSet(DeployProperties properties, String prefix)
            throws Exception {
        applications = new ArrayList<Application>();

        loadSettingsFromProperties(properties, prefix);
    }

    public void loadSettingsFromProperties(DeployProperties properties,
            String prefix) throws Exception {
        
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        String[] applicationNames = properties.getElementList(prefix);
        for (String applicationName : applicationNames) {
            Application application = new Application(properties,
                    applicationName, prefix + applicationName, this);
            applications.add(application);
        }
    }

    /**
     * Constructs an empty application-group
     * 
     */
    public ApplicationSet() {
        applications = new ArrayList<Application>();
    }

    /**
     * Returns the Applications in this ApplicationSet.
     * 
     * @return the applications in this ApplicationSet
     */
    public Application[] getApplications() {
        return applications.toArray(new Application[0]);
    }

    /**
     * Removes the given application from the application-group (if it belongs
     * to the application-group at all).
     * 
     * @param application
     *            the application to be removed from this group
     */
    public void removeApplication(Application application) {
        applications.remove(application);
    }

    /**
     * Creates a new application in this application-group, with a given name.
     * 
     * @param name
     *            the name of the application
     * @return the resulting application
     * @throws Exception
     *             if the name given is <code>null</code>
     */
    public Application createNewApplication(String name) throws Exception {
        if (hasApplication(name)) {
            throw new AlreadyExistsException(
                    "Cannot add application, application \"" + name
                            + "\" already exists");
        }

        Application result = new Application(name, this);

        applications.add(result);

        return result;
    }

    /**
     * Returns if an Application with the given name exists.
     * 
     * @param name
     *            name of the Application.
     * @return if a Application with the given name exists.
     */
    public boolean hasApplication(String name) {
        for (Application Application : applications) {
            if (Application.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an application with a given name from this ApplicationSet
     * 
     * @param applicationName
     *            the name of the application to search for
     * @return the application with the given name, or <code>null</code> if no
     *         applications with the given name exist in this ApplicationSet.
     */
    public Application getApplication(String applicationName) {
        for (Application application : applications) {
            if (application.getName().equals(applicationName)) {
                return application;
            }
        }
        return null;
    }

    /**
     * Save this application group and all contained applications to a property
     * file
     * 
     * @param file
     *            file to write to
     * @throws Exception
     *             in case file cannot be written
     */
    public void save(File file) throws Exception {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("failed to create a new file '" + file
                        + "'.");
            }
        }
        PrintWriter out = new PrintWriter(file);
        
        out.println("# Application file, " + "generated by Ibis Deploy on "
                + new Date(System.currentTimeMillis()));
        out.println();

        save(out, null);

        out.flush();
        out.close();
    }

    /**
     * Save this applicationSet to the given stream
     * 
     * @param out
     *            stream to save applicationSet to
     * @param prefix
     *            prefix for all keys written
     * @throws Exception
     *             in case data cannot be written
     */
    public void save(PrintWriter out, String prefix) throws Exception {
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        Application.printTableOfKeys(out);
        out.println();

        // write applications
        for (Application application : applications) {
            out.println();
            out.println("# Application \"" + application.getName() + "\"");
            application.save(out, prefix + application.getName(), true);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Application Set";
    }

    /**
     * Returns an info string suitable for printing (with newlines)
     * 
     * @return an info string suitable for printing (with newlines)
     */
    public String toPrintString() {
        String result = "ApplicationSet containing " + applications.size()
                + " applications:\n ";

        for (Application application : applications) {
            result += application.toPrintString() + "\n";
        }

        return result;
    }

}

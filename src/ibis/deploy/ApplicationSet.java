package ibis.deploy;

import ibis.util.TypedProperties;

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

    // application representing defaults
    private Application defaults;

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
        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        if (!file.getName().endsWith(".applications")) {
            throw new Exception(
                    "application files must have a \".applications\" extension (not: " + file + ")");
        }

        TypedProperties properties = new TypedProperties();
        properties.loadFromFile(file.getAbsolutePath());

        defaults = new Application(properties, null, "default", this);

        applications = new ArrayList<Application>();

        String[] applicationNames = Util.getElementList(properties);
        for (String applicationName : applicationNames) {
            Application application = new Application(properties,
                    applicationName, applicationName, this);
            applications.add(application);
        }
    }

    /**
     * Constructs a application group object from the given properties. Also
     * constructs the applications in this group.
     * 
     * @param properties
     *            properties of the applicationSet
     * @param prefix
     *            prefix to use on all keys
     */
    public ApplicationSet(TypedProperties properties, String prefix) {
        if (prefix != null) {
            prefix = prefix + ".";
        }
        
        defaults = new Application(properties, null, prefix + "default", this);

        applications = new ArrayList<Application>();

        String[] applicationNames = Util.getElementList(properties, prefix);
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
        this.applications = new ArrayList<Application>();
        try {
            defaults = new Application("default", this);
        } catch (Exception e) {
            // should not happen, but just in case
            throw new RuntimeException(e);
        }
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
        Application result = new Application(name, this);

        applications.add(result);

        return result;
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
     * Returns application representing defaults of this application-group.
     * 
     * @return application representing defaults of this application-group.
     */
    public Application getDefaults() {
        return defaults;
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
        // write defaults
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
        out.println("# Default settings:");
        defaults.save(out, prefix + "default", true);

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
                + " applications:\n\nDefault ";
        result += defaults.toPrintString() + "\n";

        for (Application application : applications) {
            result += application.toPrintString() + "\n";
        }

        return result;
    }

}

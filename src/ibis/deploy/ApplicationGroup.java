package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApplicationGroup {

    private String name;

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
     * @throws IOException
     *             if reading from the given file fails
     * @throws Exception
     *             if the properties don't contain a 'name' property with the
     *             name of the application group
     */
    public ApplicationGroup(File file) throws FileNotFoundException,
            IOException, Exception {
        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        TypedProperties properties = new TypedProperties();
        properties.loadFromFile(file.getAbsolutePath());

        name = properties.getProperty("name");

        if (name == null || name.length() == 0) {
            throw new Exception("no application-group name specified in application-group file: " + file);
        }

        defaults = new Application(properties, null, null);

        applications = new ArrayList<Application>();
        String[] applicationNames = properties.getStringList("applications");
        if (applicationNames != null) {
            for (String applicationName : applicationNames) {
                Application application = new Application(properties,
                        applicationName, this);
                applications.add(application);
            }
        }
    }

    /**
     * Constructs a application-group with the given name.
     * 
     * @param name
     *            the name of the application-group
     * @throws Exception
     *             if the name is <code>null</code>
     */
    public ApplicationGroup(String name) throws Exception {
        if (name == null) {
            throw new Exception("no name specified for application-group");
        }

        this.name = name;
        this.applications = new ArrayList<Application>();
        defaults = new Application("defaults", null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the Applications in this ApplicationGroup.
     * 
     * @return the applications in this ApplicationGroup
     */
    public Application[] getApplications() {
        return applications.toArray(new Application[0]);
    }

    /**
     * Removes the given application from the application-group (if it belongs to the application-group at
     * all).
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
     * @throws Exception
     *             if the name given is <code>null</code>
     */
    public Application createNewApplication(String name) throws Exception {
        Application result = new Application(name, this);

        applications.add(result);

        return result;
    }

    /**
     * Get an application with a given name from this ApplicationGroup
     * 
     * @param applicationName
     *            the name of the application to search for
     * @return the application with the given name, or <code>null</code> if no
     *         applications with the given name exist in this ApplicationGroup.
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

    public void save(File file) throws Exception {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("failed to create a new file '" + file
                        + "'.");
            }
        }
        PrintWriter out = new PrintWriter(file);
        // write defaults
        out.println("# ApplicationGroup properties file, "
                + "generated by Ibis Deploy on "
                + new Date(System.currentTimeMillis()));
        out.println();
        out.println("# ApplicationGroup name:");
        out.println("name = " + getName());

        out.println();
        out.println("# Default settings:");
        defaults.print(out, false);

        // write names of applications
        out.println();
        out.println("# Comma separated list of applications in this application-group:");

        if (applications.size() > 0) {
            out.print("applications = ");
            for (int i = 0; i < applications.size() - 1; i++) {
                out.print(applications.get(i).getName() + ",");
            }
            out.println(applications.get(applications.size() - 1).getName());
        } else {
            out.println("applications = ");
        }

        // write applications
        for (Application application : applications) {
            out.println();
            out.println("# Details of application \"" + application.getName()
                    + "\"");
            application.print(out, true);

        }
        out.flush();
        out.close();
    }

    public String toString() {
        return name;
    }

    /**
     * Returns an info string suitable for printing (with newlines)
     * 
     * @return an info string suitable for printing (with newlines)
     */
    public String toPrintString() {
        String result = "ApplicationGroup \"" + getName() + "\" containing "
                + applications.size() + " applications\n";

        for (Application application : applications) {
            result += application.toPrintString() + "\n";
        }

        return result;
    }

}

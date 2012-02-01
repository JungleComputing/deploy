package ibis.deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Jungle containing resources. Basically just a HashMap with resource objects,
 * and some functions for loading and saving jungles.
 * 
 * @author Niels Drost
 * 
 */
public class Jungle {

    private final List<Resource> resources;

    /**
     * Constructs a new empty jungle.
     */
    public Jungle() {
        resources = new ArrayList<Resource>();

        try {
            resources.add(Resource.getLocalResource());
        } catch (Exception e) {
            // should not happen
            throw new RuntimeException("exception while creating jungle", e);
        }
    }

    /**
     * Constructs a junge object from properties stored in the given file. Also
     * constructs the resources inside this jungle.
     * 
     * @param file
     *            the file containing the properties
     * @throws FileNotFoundException
     *             if the given file cannot be found
     * @throws Exception
     *             if reading from the given file fails, or the file contains
     *             invalid properties
     */
    public Jungle(File file) throws FileNotFoundException, Exception {
        this(file, false);
    }

    /**
     * Constructs a jungle from properties stored in the given file.
     * 
     * @param file
     *            the file containing the properties
     * @throws FileNotFoundException
     *             if the given file cannot be found
     * @throws Exception
     *             if reading from the given file fails, or the file contains
     *             invalid properties
     */
    public Jungle(File file, boolean tryClasspath) throws FileNotFoundException, Exception {
        resources = new ArrayList<Resource>();

        resources.add(Resource.getLocalResource());

        load(file, tryClasspath);
    }

    /**
     * Loads resources from properties stored in the given file.
     * 
     * @param file
     *            the file containing the resource properties
     * @throws FileNotFoundException
     *             if the given file cannot be found
     * @throws Exception
     *             if reading from the given file fails, or the file contains
     *             invalid properties
     */
    public void load(File file, boolean tryClasspath) throws FileNotFoundException, Exception {

        DeployProperties properties = new DeployProperties();
        
        if (!file.getName().endsWith(".jungle")) {
            throw new Exception("jungle files must have a \".jungle\" extension (loading file " + file.getName() + ")");
        }

        if (file.exists()) {
            properties.loadFromFile(file.getAbsolutePath());
        } else if (tryClasspath) {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(file.getName());

            if (inputStream == null) {
                throw new FileNotFoundException("file \"" + file + "\" does not exist, and cannot find on classpath");
            } else {
                try {
                    properties.load(inputStream);
                } finally {
                    try {
                        inputStream.close();
                    } catch (Exception e2) {
                        // IGNORE
                    }
                }
            }
        } else {
            throw new FileNotFoundException("file \"" + file + "\" does not exist");
        }

        String[] resourceNames = properties.getElementList("");

        if (resourceNames != null) {
            for (String resourceName : resourceNames) {
                Resource resource = getResource(resourceName);

                if (resource == null) {
                    resource = new Resource(resourceName);
                    addResource(resource);
                }

                // add default properties (if any)
                resource.loadFromProperties(properties, "default");

                // add normal properties
                resource.loadFromProperties(properties, resourceName);

            }
        }

    }

    /**
     * Returns the Resources in this Jungle.
     * 
     * @return the resources in this Jungle
     */
    public Resource[] getResources() {
        return resources.toArray(new Resource[0]);
    }

    /**
     * Removes the resource with the given name from the jungle (if it belongs
     * to the jungle at all).
     * 
     * @param name
     *            the name of the resource to be removed from this group
     */
    public void removeResource(String name) {
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getName().equals(name)) {
                resources.remove(i);
                // go back one
                i--;
            }
        }
    }

    /**
     * Adds a new resource to this jungle.
     * 
     * @param resource
     *            the resource.
     * 
     * @throws AlreadyExistsException
     *             if the resource (name) is already present in this jungle
     */
    public void addResource(Resource resource) throws Exception {
        if (hasResource(resource.getName())) {
            throw new AlreadyExistsException("Cannot add resource, resource with name \"" + resource.getName()
                    + "\" already exists");
        }

        resources.add(resource);
    }

    /**
     * Returns if a resource with the given name exists.
     * 
     * @param name
     *            name of the resource.
     * @return if a resource with the given name exists.
     */
    public boolean hasResource(String name) {
        for (Resource resource : resources) {
            if (resource.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an resource with a given name from this Jungle
     * 
     * @param resourceName
     *            the name of the resource to search for
     * @return the resource with the given name, or <code>null</code> if no
     *         resources with the given name exist in this Jungle.
     */
    public Resource getResource(String resourceName) {
        for (Resource resource : resources) {
            if (resource.getName().equals(resourceName)) {
                return resource;
            }
        }
        return null;
    }

    /**
     * Save this jungle and all contained resources to a property file
     * 
     * @param file
     *            file to save jungle to
     * 
     * @throws Exception
     *             in case file cannot be written
     */
    public void save(File file) throws Exception {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("failed to create a new file '" + file + "'.");
            }
        }
        PrintWriter out = new PrintWriter(file);

        out.println("# Jungle file, " + "generated by Ibis Deploy on " + new Date(System.currentTimeMillis()));
        out.println();

        save(out, null);

        out.flush();
        out.close();
    }

    /**
     * Save this jungle and all contained resources to the given stream
     * 
     * @param out
     *            stream to save jungle to
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

        Resource.printTableOfKeys(out);
        out.println();

        // write resources
        for (Resource resource : resources) {
            if (!resource.getName().equals("local")) {
                out.println();
                out.println("# Details of resource \"" + resource.getName() + "\"");
                resource.save(out, prefix + resource.getName(), true);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Jungle containing " + resources.size() + " resources";
    }

    /**
     * Returns an info string suitable for printing (with newlines)
     * 
     * @return an info string suitable for printing (with newlines)
     */
    public String toPrintString() {
        String result = "Jungle containing " + resources.size() + " resources:\n";

        for (Resource resource : resources) {
            result += resource.toPrintString() + "\n";
        }

        return result;
    }
}

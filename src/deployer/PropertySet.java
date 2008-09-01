package deployer;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A PropertySet is the abstract base class of a named object that contains data
 * in the form of a set of key, value pairs.
 * 
 * @author rkemp
 */
public class PropertySet {

    private String name;

    private List<PropertyCategory> categories = new ArrayList<PropertyCategory>();

    /**
     * Constructs a new property set with the given name.
     * 
     * @param name
     *                the name of the new property set
     * @throws Exception
     *                 if name is <code>null</code>
     */
    public PropertySet(String name) throws Exception {
        if (name == null) {
            throw new Exception("property 'name' not found, but mandatory");
        }
        this.name = name;
    }

    /**
     * Loads a properties object from a file.
     * 
     * @param fileName
     *                the filename to load from
     * @return the loaded properties
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static TypedProperties getPropertiesFromFile(String fileName)
            throws FileNotFoundException, IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(fileName));
        return properties;
    }

    /**
     * Returns the PropertyCategories of this PropertySet.
     * 
     * @return the PropertyCategories of this PropertySet.
     */
    public List<PropertyCategory> getCategories() {
        return categories;
    }

    /**
     * Gets the name of the PropertySet
     * 
     * @return the name of the PropertySet
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of the PropertySet
     * 
     * @param name
     *                the name of the PropertySet
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the String representation of the PropertySet (this method will
     * call return {@link #getName()}
     * 
     * @return the String representation of the PropertySet (this method will
     *         call return {@link #getName()}
     */
    public final String toString() {
        return getName();
    }

    /**
     * Returns the default value for a given key in a given category or
     * <code>null</code> if the category or key doesn't exist.
     * 
     * @return the default value for a given key in a given category or
     *         <code>null</code> if the category or key doesn't exist.
     */
    public String defaultValueFor(String key, String category) {
        return null;
    }
}

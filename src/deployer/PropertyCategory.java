package deployer;

import java.util.SortedMap;

/**
 * Properties might be grouped together in a category. So all 'java' properties
 * of an application might form the 'java' PropertyCategory.
 * 
 * @author rkemp
 */
public class PropertyCategory {

    private SortedMap<String, String> data;

    private String name;

    /**
     * Constructs a PropertyCategory with a given name and a data set containing
     * key value pairs belonging to this category.
     * 
     * @param name
     *                the name of the category
     * @param data
     *                the data in the category
     */
    public PropertyCategory(String name, SortedMap<String, String> data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Returns the name of the category
     * 
     * @return the name of the category
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data set containing the key value pairs belonging to this
     * category
     * 
     * @return the data set containing the key value pairs belonging to this
     *         category
     */
    public SortedMap<String, String> getData() {
        return data;
    }

}

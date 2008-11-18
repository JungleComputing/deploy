package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.gridlab.gat.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility functions of Ibis-Deploy
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    /**
     * Finds a list of clusters, jobs or application in a list of properties
     * 
     * Each unique string in the set of keys(cut on the first ".") is returned,
     * except for "default"
     * 
     * @param properties
     *            to search for elements in.
     * 
     * @return the set of elements
     */
    public static String[] getElementList(Properties properties) {
        Set<String> result = new HashSet<String>();

        for (String key : properties.stringPropertyNames()) {
            // add part of key before the first period to the result
            result.add(key.split("\\.")[0]);
        }

        // make sure "default" is not in the list
        result.remove("default");

        return result.toArray(new String[0]);
    }

    /**
     * Extracts a URI property from a properties object
     * 
     * @param properties
     *            source properties object
     * @param key
     *            key of property to extract
     * @return URI version of property, or null if it does not exist
     * @throws URISyntaxException
     */
    public static URI getURIProperty(Properties properties, String key)
            throws URISyntaxException {
        if (properties.getProperty(key) == null) {
            return null;
        }
        return new URI(properties.getProperty(key));
    }

    /**
     * Extracts a File property from a properties object
     * 
     * @param properties
     *            source properties object
     * @param key
     *            key of property to extract
     * @return File version of property, or null if it does not exist
     */
    public static File getFileProperty(Properties properties, String key) {
        if (properties.getProperty(key) == null) {
            return null;
        }
        return new File(properties.getProperty(key));
    }

    /**
     * Extracts a list of files from a properties object
     * 
     * @param properties
     *            source properties object
     * @param key
     *            key of property to extract
     * @return List of Files , or null if the property does not exist
     */
    public static List<File> getFileListProperty(TypedProperties properties,
            String key) {
        if (properties.getProperty(key) == null) {
            return null;
        }

        List<File> result = new ArrayList<File>();
        for (String string : properties.getStringList(key)) {
            result.add(new File(string));
        }

        return result;
    }

    /**
     * Get a string map from a property object
     * 
     * @param properties
     *            the properties to extract the map from.
     * @param key
     *            the key of the map.
     * @return a string map.
     */
    public static Map<String, String> getStringMapProperty(
            TypedProperties properties, String key) {
        if (properties.getProperty(key) == null) {
            return null;
        }

        Map<String, String> result = new HashMap<String, String>();
        for (String string : properties.getStringList(key)) {
            String[] keyValue = string.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                result.put(keyValue[0], null);
            } else {
                logger.warn("error on parsing key " + key + " with value "
                        + properties.getProperty(key));
            }
        }

        return result;
    }

    /**
     * Convert a list of Strings to a single comma separated String
     * 
     * @param list
     *            the input list
     * @return a comma separated version of the list
     */
    public static String strings2CSS(List<String> list) {
        if (list == null) {
            return null;
        }

        if (list.size() == 0) {
            return "";
        }
        String result = "";
        for (String object : list) {
            result = result + object.toString() + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * convert a list of Strings to a single comma separated String
     * 
     * @param list
     *            the input list
     * @return a comma separated version of the list
     */
    public static String strings2CSS(String[] list) {
        if (list == null) {
            return null;
        }

        if (list.length == 0) {
            return "";
        }
        String result = "";
        for (String object : list) {
            result = result + object.toString() + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * convert a list of files to a single comma separated String
     * 
     * @param list
     *            the input list
     * @return a comma separated version of the list
     */
    public static String files2CSS(List<File> list) {
        if (list == null) {
            return null;
        }

        if (list.size() == 0) {
            return "";
        }
        String result = "";
        for (File file : list) {
            result = result + file.toString() + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * convert a list of files to a single comma separated String
     * 
     * @param list
     *            the input list
     * @return a comma separated version of the list
     */
    public static String files2CSS(File[] list) {
        if (list == null) {
            return null;
        }

        if (list.length == 0) {
            return "";
        }
        String result = "";
        for (File file : list) {
            result = result + file.toString() + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * convert a string map to a single comma separated String
     * 
     * @param map
     *            the input map
     * @return a comma separated version of the map
     */
    public static String toCSString(Map<String, String> map) {
        if (map == null) {
            return null;
        }

        String result = "";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result += entry.getKey() + "=" + entry.getValue() + ",";
        }
        return result;
    }

    /**
     * Returns a property as a list of strings. Returns null if property not
     * found
     * 
     * @param properties
     *            the source properties object
     * @param key
     *            key of the property to extract
     * @return the property as a list of strings, or null if the property does
     *         not exist
     */
    public static List<String> getStringListProperty(
            TypedProperties properties, String key) {
        if (properties.getProperty(key) == null) {
            return null;
        }

        return Arrays.asList(properties.getStringList(key));
    }

}

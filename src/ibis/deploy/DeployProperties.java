package ibis.deploy;

import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.gridlab.gat.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.util.TypedProperties;

/**
 * Properties for Ibis Deploy. Extension of TypedProperties with some
 * Ibis-Deploy specific functions. Also splits lists on spaces as well as commas
 * by default.
 * 
 * @author Niels Drost
 * 
 */
public class DeployProperties extends TypedProperties {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory
            .getLogger(DeployProperties.class);
    
    /**
     * Convert a list of Strings to a single space separated String
     * 
     * @param list
     *            the input list
     * @return a comma separated version of the list
     */
    public static String strings2SSS(List<String> list) {
        if (list == null) {
            return null;
        }

        if (list.size() == 0) {
            return "";
        }
        String result = "";
        for (String object : list) {
            result = result + object.toString() + " ";
        }
        return result.substring(0, result.length() - 1);
    }
    
    /**
     * convert a list of Strings to a single space separated String
     * 
     * @param list
     *            the input list
     * @return a comma separated version of the list
     */
    public static String strings2SSS(String[] list) {
        if (list == null) {
            return null;
        }

        if (list.length == 0) {
            return "";
        }
        String result = "";
        for (String object : list) {
            result = result + object.toString() + " ";
        }
        return result.substring(0, result.length() - 2);
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
            result = result + object.toString() + ", ";
        }
        return result.substring(0, result.length() - 2);
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
            result = result + object.toString() + ", ";
        }
        return result.substring(0, result.length() - 2);
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
            result = result + file.toString() + ", ";
        }
        return result.substring(0, result.length() - 2);
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
            result = result + file.toString() + ", ";
        }
        return result.substring(0, result.length() - 2);
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
            result += entry.getKey() + "=" + entry.getValue() + ", ";
        }
        return result;
    }

    /**
     * Finds a list of clusters, jobs or application in a list of properties
     * 
     * Each unique string in the set of keys(cut on the first ".") is returned,
     * except for "default". The list is sorted by alphabet.
     * 
     * @return the set of elements
     */
    public String[] getElementList() {
        Set<String> result = new TreeSet<String>();

        for (Object key : keySet()) {
            // add part of key before the first period to the result
            result.add(key.toString().split("\\.")[0]);
        }

        // make sure "default" is not in the list
        result.remove("default");

        return result.toArray(new String[0]);
    }

    /**
     * Finds a list of clusters, jobs or application in a list of properties
     * 
     * Each unique string in the set of keys(cut on the first ".") starting with
     * the given prefix is returned, except for "default". Result is also sorted.
     * 
     * @param prefix
     *            prefix to filter on
     * 
     * @return the set of elements
     */
    public String[] getElementList(String prefix) {
        Set<String> result = new TreeSet<String>();

        for (Object key : keySet()) {
            if (key.toString().startsWith(prefix)) {
                key = key.toString().substring(prefix.length());
                // add part of key before the first period to the result
                result.add(key.toString().split("\\.")[0]);
            }
        }

        // make sure "default" is not in the list
        result.remove("default");

        return result.toArray(new String[0]);
    }

    /**
     * Extracts a URI property from a properties object
     * 
     * @param key
     *            key of property to extract
     * @return URI version of property, or null if it does not exist
     * @throws URISyntaxException
     */
    public URI getURIProperty(String key) throws URISyntaxException {
        if (getProperty(key) == null) {
            return null;
        }
        return new URI(getProperty(key));
    }

    /**
     * Extracts a File property from a properties object
     * 
     * @param key
     *            key of property to extract
     * @return File version of property, or null if it does not exist
     */
    public File getFileProperty(String key) {
        if (getProperty(key) == null) {
            return null;
        }
        return new File(getProperty(key));
    }

    /**
     * Extracts a list of files from a properties object. Uses a comma as a
     * delimited (just in case there are spaces in the filenames)
     * 
     * @param key
     *            key of property to extract
     * @return List of Files , or null if the property does not exist
     */
    public List<File> getFileListProperty(String key) {
        if (getProperty(key) == null) {
            return null;
        }

        List<File> result = new ArrayList<File>();
        for (String string : getStringList(key)) {
            result.add(new File(string));
        }

        return result;
    }

    /**
     * Get a string map from a property object.
     * 
     * @param key
     *            the key of the map.
     * @return a string map.
     */
    public Map<String, String> getStringMapProperty(String key) {
        if (getProperty(key) == null) {
            return null;
        }

        Map<String, String> result = new HashMap<String, String>();
        for (String string : getStringList(key)) {
            String[] keyValue = string.split("=", 2);
            if (keyValue.length == 2 && keyValue[1].equals("")) {
                result.put(keyValue[0], null);
            } else if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                result.put(keyValue[0], null);
            } else {
                logger.warn("error on parsing key " + key + " with value "
                        + getProperty(key));
            }
        }

        return result;
    }
    
    public Color getColorProperty(String key) {
    	try {
			return Color.decode(getProperty(key));
		} catch (NumberFormatException e){
			logger.error("Cannot decode color " + getProperty(key), e);
			return null;
		}
    }

    /**
     * Returns a property as a list of strings. Returns null if property not
     * found
     * 
     * @param key
     *            key of the property to extract
     * @return the property as a list of strings, or null if the property does
     *         not exist
     */
    public List<String> getStringListProperty(String key) {
        if (getProperty(key) == null) {
            return null;
        }

        return Arrays.asList(getStringList(key));
    }
    
    /**
     * Returns a property as a list of strings. Returns null if property not
     * found
     * 
     * @param key
     *            key of the property to extract
     * @return the property as a list of strings, or null if the property does
     *         not exist
     */
    public List<String> getStringListProperty(String key, String delim) {
        if (getProperty(key) == null) {
            return null;
        }

        return Arrays.asList(getStringList(key, delim));
    }

}

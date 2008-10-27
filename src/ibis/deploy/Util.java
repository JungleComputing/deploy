package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.gridlab.gat.URI;

/**
 * some utility functions of Ibis-Deploy
 */
public class Util {

    public static URI getURIProperty(Properties properties, String key)
            throws URISyntaxException {
        if (properties.getProperty(key) == null) {
            return null;
        }
        return new URI(properties.getProperty(key));
    }

    public static File getFileProperty(Properties properties, String key) {
        if (properties.getProperty(key) == null) {
            return null;
        }
        return new File(properties.getProperty(key));
    }

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

    public static Map<String, String> getStringMapProperty(
            TypedProperties properties, String key) throws Exception {
        if (properties.getProperty(key) == null) {
            return null;
        }

        Map<String, String> result = new HashMap<String, String>();
        for (String string : properties.getStringList(key)) {
            String[] keyValue = string.split(",", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                result.put(keyValue[0], null);
            } else {
                throw new Exception("error on parsing key " + key
                        + " with value " + properties.getProperty(key));
            }
        }

        return result;
    }

    // convert a list of Strings to a single comma seperated String
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

    // convert a list of Strings to a single comma seperated String
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

    // convert a list of files to a single comma seperated String
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

    // convert a list of files to a single comma seperated String
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

    // convert a String map to a single comma seperated String
    public static String toCSString(Map<String, String> map) {
        return null;
    }

    // returns a property as a list of strings. Returns null if property not
    // found
    public static List<String> getStringListProperty(
            TypedProperties properties, String key) {
        if (properties.getProperty(key) == null) {
            return null;
        }

        return Arrays.asList(properties.getStringList(key));
    }

}

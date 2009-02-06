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
import java.util.TreeSet;

import org.gridlab.gat.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility functions of Ibis-Deploy
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);



    /**
     * Convert a list of Strings to a single comma separated String
     * 
     * @param list
     *                the input list
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
     *                the input list
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
     *                the input list
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
     *                the input list
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
     *                the input map
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


}
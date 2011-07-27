package ibis.deploy.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Colors {

    // colors used for clusters
    private static final String[] colors = { "#FF0000", "#FF8000", "#80FF00",
        "#00FF80", "#00FFFF", "#007FFF", "#8000FF", "#FF0080",
        "#FF8080", "#FFBF80", "#FFFF80", "#BFFF80", "#80FF80", "#80FFBF",
        "#80FFFF", "#80BFFF", "#8080FF", "#BF80FF", "#FF80FF", "#FF80BF",
        "#008040", "#008080", "#00FF00" };

    // red, reserved for local
    public static final Color LOCAL_COLOR = Color.decode("#FF0000");

    private static Map<String, String> colorMap = new HashMap<String, String>();

    private static int next = 0;

    private static int next() {
        int result = next;

        next++;
        next = next % colors.length;

        return result;
    }

    /**
     * Create a random (but consistent) color from a location string.
     */
    public static synchronized Color fromLocation(String location) {
        if (location == null) {
            return null;
        }
        
        String result = colorMap.get(location);

        if (result == null) {
            result = colors[next()];

            colorMap.put(location, result);
        }

        return Color.decode(result);
    }

    public static String color2colorCode(Color color) {
        if (color == null) {
            return null;
        }
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(),
                color.getBlue());

    }

    public static Color getLightColor(Color color) {
        if (color == null) {
            return null;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 135);
    }
}

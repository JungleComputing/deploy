package ibis.deploy;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class Colors {
    /**
     * Create a random (but consistent) color from a location string.
     */
    public static String locationToColorString(String location) {
        if (location == null) {
            return "";
        }

        Color uncorrected = new Color(location.hashCode());

        float[] components = uncorrected.getColorComponents(null);

        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        float[] correctedElements = colorSpace.fromCIEXYZ(components);

        Color corrected = new Color(correctedElements[0], correctedElements[1],
                correctedElements[2]);

        return "^#"
                + String.format("%x%x%x", corrected.getRed(), corrected
                        .getGreen(), corrected.getBlue());
    }

}

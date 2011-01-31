package ibis.deploy;

import ibis.smartsockets.viz.UniqueColor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Colors {

	// colors used for clusters
	private static final String[] colors = { "#FF0000", "#FF8000", "#FFFF00",
			"#80FF00", "#00FFFF", "#007FFF", "#0000FF", "#8000FF", "#FF00FF",
			"#FF0080", "#FF8080", "#FFBF80", "#FFFF80", "#BFFF80", "#80FF80",
			"#80FFBF", "#80FFFF", "#80BFFF", "#8080FF", "#BF80FF", "#FF80FF",
			"#FF80BF", "#800000", "#804000", "#808000", "#00FF00", "#408000",
			"#008000", "#008040", "#008080", "#004080", "#000080", "#400080",
			"#00FF80", "#800080", "#800040" };

	private static Map<String, String> colorMap = new HashMap<String, String>();

	private static int next = 0;

	private static int next() {
		int result = next;

		next++;
		next = next % colors.length;

		return result;
	}

	public static void main(String[] arguments) {
		UniqueColor uniqueColor = new UniqueColor();

		for (int i = 0; i < (3 * 12); i++) {
			Color color = uniqueColor.getUniqueColor();

			System.err.printf("\"#%02X%02X%02X\",", color.getRed(), color
					.getGreen(), color.getBlue());
		}
	}

	/**
	 * Create a random (but consistent) color from a location string.
	 */
	public static synchronized String locationToColorString(String location) {
		if (location == null) {
			return "";
		}

		String result = colorMap.get(location);

		if (result == null) {
			result = colors[next()];

			colorMap.put(location, result);
		}

		System.err.println("Next color = " + result);

		return result;
	}

}

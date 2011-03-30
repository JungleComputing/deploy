package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.render.PatternFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class UIConstants {

	public static BufferedImage LOCATIONS_SHAPE_LIST[] = {
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.RED),
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.ORANGE),
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.YELLOW),
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.GREEN),
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.BLUE),
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.GRAY),
			PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f,
					Color.BLACK), };
	public static Color LOCATION_COLOR_LIST[] = { Color.RED, Color.ORANGE, Color.YELLOW,
			Color.GREEN, Color.BLUE, Color.GRAY, Color.BLACK, };

	public static int NPOSITIONS = 10;
	public static int ARC_HEIGHT = 8000;
}

package ibis.deploy.vizFramework.globeViz.viz.utils;

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
	
	public static Color EDGE_WITH_PARTICLES_COLOR = Color.GRAY;

	public static int NPOSITIONS = 10;
	public static int ARC_HEIGHT = 8000;
	public static int NUMBER_OF_CONTROL_POINTS = 3;
	public static int LOCATION_CIRCLE_SIZE = 22;
	
	public static double increment = 2*Math.PI/50;
	
	public static int LEVELS = 2;
	
	public static int MARKER_SIZE = 6;
	
	public static int EDGE_WITH_PARTICLE_SIZE = 1;
	public static int EDGE_WITHOUT_PARTICLE_SIZE = 2;
}

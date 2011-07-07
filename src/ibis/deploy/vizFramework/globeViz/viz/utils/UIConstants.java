package ibis.deploy.vizFramework.globeViz.viz.utils;

import gov.nasa.worldwind.render.PatternFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;

import prefuse.util.ColorLib;

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
    public static Color LOCATION_COLOR_LIST[] = { Color.RED, Color.ORANGE,
            Color.YELLOW, Color.GREEN, Color.BLUE, Color.GRAY, Color.BLACK, };
    public static Color EDGE_WITH_PARTICLES_COLOR = Color.GRAY;
    public static int NPOSITIONS = 10;
    public static int ARC_HEIGHT = 8000;
    public static int ARC_SECOND_HEIGHT = 16000;
    public static int NUMBER_OF_CONTROL_POINTS = 3;
    public static int LOCATION_CIRCLE_SIZE = 25;
    public static float LOCATION_GAP = 0.3f;
    public static double increment = 2 * Math.PI / 50;
    public static int FAKE_LEVELS = 2;
    public static int REAL_LEVELS = 1;
    public static int MARKER_SIZE = 6;
    public static int EDGE_WITH_PARTICLE_SIZE = 1;
    public static int EDGE_WITHOUT_PARTICLE_SIZE = 2;
    public static final String CLUSTER = "cluster";
    public static final String NODE_TYPE_SITE_NODE = "site node";
    public static final String NODE_TYPE_IBIS_NODE = "ibis node";
    public static final String NODE_TYPE_ROOT_NODE = "ibis deploy";
    public static final int BSPLINE_EDGE_TYPE = 100;
    public static final double INITIAL_BUNDLING_FACTOR = 0.9;
    public static final Color DEFAULT_START_COLOR = Color.red;
    public static final Color DEFAULT_STOP_COLOR = Color.green;
    public static final int SELECTED_FILL_COLOR = ColorLib.rgb(0, 0, 255);
    public static final int SELECTED_TEXT_COLOR = ColorLib.rgb(255, 255, 255);
    public static final int DEFAULT_TEXT_COLOR = ColorLib.gray(0);
    public static final int DEFAULT_ROOT_NODE_COLOR = ColorLib.gray(200);
    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String NODE_NAME = "name";
    public static final String NODE_TYPE = "type";
    public static final String WEIGHT = "weight";
    public static final long DEFAULT_WEIGHT = 0;
    public static boolean initialized = false;
    public static final String[] colors = { "#FF0000", "#FF8000", "#80FF00",
            "#00FF80", "#00FFFF", "#007FFF", "#8000FF", "#FF0080",
            "#FF8080", "#FFBF80", "#FFFF80", "#BFFF80", "#80FF80", "#80FFBF",
            "#80FFFF", "#80BFFF", "#8080FF", "#BF80FF", "#FF80FF", "#FF80BF",
            "#008040", "#008080", "#00FF00" };
    public static int colorIndex = 0;
    public static double minAlpha = 0.4;
    public static double maxAlpha = 0.9;
    public static int offset = 1000;
}

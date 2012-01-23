package ibis.amuse.visualization;

import ibis.amuse.visualization.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
    static Logger logger = LoggerFactory.getLogger(Settings.class);

    // Size settings for default startup and screenshots
    private static int DEFAULT_SCREEN_WIDTH = 1024;
    private static int DEFAULT_SCREEN_HEIGHT = 768;

    private static int SCREENSHOT_SCREEN_WIDTH = 1280;
    private static int SCREENSHOT_SCREEN_HEIGHT = 720;

    // Settings for the initial view
    private static int INITIAL_SIMULATION_FRAME = 0;
    private static float INITIAL_ROTATION_X = 17f;
    private static float INITIAL_ROTATION_Y = -25f;
    private static float INITIAL_ZOOM = -390.0f;

    // Setting per movie frame
    private static boolean MOVIE_ROTATE = true;
    private static float MOVIE_ROTATION_SPEED_MIN = -1f;
    private static float MOVIE_ROTATION_SPEED_MAX = 1f;
    private static float MOVIE_ROTATION_SPEED_DEF = -0.25f;

    // Settings for the gas cloud octree
    private static int MAX_CLOUD_DEPTH = 25;
    private static float GAS_EDGES = 800f;

    // Settings that should never change, but are listed here to make sure they
    // can be found if necessary
    private static int MAX_EXPECTED_MODELS = 1000;

    // Minimum and maximum values for the brightness sliders
    private static float POSTPROCESSING_OVERALL_BRIGHTNESS_MIN = 0f;
    private static float POSTPROCESSING_OVERALL_BRIGHTNESS_MAX = 10f;
    private static float POSTPROCESSING_AXES_BRIGHTNESS_MIN = 0f;
    private static float POSTPROCESSING_AXES_BRIGHTNESS_MAX = 4f;
    private static float POSTPROCESSING_GAS_BRIGHTNESS_MIN = 0f;
    private static float POSTPROCESSING_GAS_BRIGHTNESS_MAX = 4f;
    private static float POSTPROCESSING_STAR_HALO_BRIGHTNESS_MIN = 0f;
    private static float POSTPROCESSING_STAR_HALO_BRIGHTNESS_MAX = 4f;
    private static float POSTPROCESSING_STAR_BRIGHTNESS_MIN = 0f;
    private static float POSTPROCESSING_STAR_BRIGHTNESS_MAX = 4f;

    // Settings for the postprocessing shader
    private static float POSTPROCESSING_OVERALL_BRIGHTNESS_DEF = 4f;
    private static float POSTPROCESSING_AXES_BRIGHTNESS_DEF = 1f;
    private static float POSTPROCESSING_GAS_BRIGHTNESS_DEF = 3f;
    private static float POSTPROCESSING_STAR_HALO_BRIGHTNESS_DEF = 3f;
    private static float POSTPROCESSING_STAR_BRIGHTNESS_DEF = 4f;

    // Settings for the star-shape blur method (the + shape of stars)
    private static int STAR_SHAPE_BLUR_SIZE = 1;
    private static float STAR_SHAPE_BLURFILTER_SIZE = 8f;
    private static float STAR_SHAPE_SIGMA = 100f;
    private static float STAR_SHAPE_ALPHA = 0.5f;
    private static int STAR_SHAPE_BLUR_TYPE = 0;

    // Settings for the detail levels.
    private static int LOW_GAS_BLUR_PASSES = 0;
    private static float LOW_GAS_BLUR_SIZE = 2;
    private static int LOW_GAS_BLUR_TYPE = 8;

    private static int LOW_STAR_HALO_BLUR_PASSES = 1;
    private static float LOW_STAR_HALO_BLUR_SIZE = 1;
    private static int LOW_STAR_HALO_BLUR_TYPE = 6;

    private static int LOW_GAS_SUBDIVISION = 0;
    private static int LOW_STAR_SUBDIVISION = 1;
    private static int LOW_GAS_PARTICLES_PER_OCTREE_NODE = 100;

    private static int MEDIUM_GAS_BLUR_PASSES = 1;
    private static float MEDIUM_GAS_BLUR_SIZE = 2;
    private static int MEDIUM_GAS_BLUR_TYPE = 8;

    private static int MEDIUM_STAR_HALO_BLUR_PASSES = 1;
    private static float MEDIUM_STAR_HALO_BLUR_SIZE = 1;
    private static int MEDIUM_STAR_HALO_BLUR_TYPE = 6;

    private static int MEDIUM_GAS_SUBDIVISION = 1;
    private static int MEDIUM_STAR_SUBDIVISION = 2;
    private static int MEDIUM_GAS_PARTICLES_PER_OCTREE_NODE = 25;

    private static int HIGH_GAS_BLUR_PASSES = 2;
    private static float HIGH_GAS_BLUR_SIZE = 2;
    private static int HIGH_GAS_BLUR_TYPE = 8;

    private static int HIGH_STAR_HALO_BLUR_PASSES = 2;
    private static float HIGH_STAR_HALO_BLUR_SIZE = 1;
    private static int HIGH_STAR_HALO_BLUR_TYPE = 6;

    private static int HIGH_GAS_SUBDIVISION = 1;
    private static int HIGH_STAR_SUBDIVISION = 3;
    private static int HIGH_GAS_PARTICLES_PER_OCTREE_NODE = 2;

    // Snaphots have different settings, since they are rendered at extremely
    // high resolutions pixels
    private static int SNAPSHOT_GAS_BLUR_PASSES = 2; // 2
    private static float SNAPSHOT_GAS_BLUR_SIZE = 2; // 6
    private static int SNAPSHOT_GAS_BLUR_TYPE = 8; // 10

    private static int SNAPSHOT_STAR_HALO_BLUR_PASSES = 2; // 2
    private static float SNAPSHOT_STAR_HALO_BLUR_SIZE = 1; // 1
    private static int SNAPSHOT_STAR_HALO_BLUR_TYPE = 6; // 6

    private static boolean GAS_COLOR_INVERTED = false;

    static {
        TypedProperties props = new TypedProperties();
        props.loadFromFile("settings.properties");

        try {
            // Size settings for default startup and screenshots
            DEFAULT_SCREEN_WIDTH = props.getIntProperty("DEFAULT_SCREEN_WIDTH");
            DEFAULT_SCREEN_HEIGHT = props
                    .getIntProperty("DEFAULT_SCREEN_HEIGHT");

            SCREENSHOT_SCREEN_WIDTH = props
                    .getIntProperty("SCREENSHOT_SCREEN_WIDTH");
            SCREENSHOT_SCREEN_HEIGHT = props
                    .getIntProperty("SCREENSHOT_SCREEN_HEIGHT");

            // Settings for the initial view
            INITIAL_SIMULATION_FRAME = props
                    .getIntProperty("INITIAL_SIMULATION_FRAME");
            INITIAL_ROTATION_X = props.getFloatProperty("INITIAL_ROTATION_X");
            INITIAL_ROTATION_Y = props.getFloatProperty("INITIAL_ROTATION_Y");
            INITIAL_ZOOM = props.getFloatProperty("INITIAL_ZOOM");

            // Setting per movie frame
            MOVIE_ROTATE = props.getBooleanProperty("MOVIE_ROTATE");
            MOVIE_ROTATION_SPEED_MIN = props
                    .getFloatProperty("MOVIE_ROTATION_SPEED_MIN");
            MOVIE_ROTATION_SPEED_MAX = props
                    .getFloatProperty("MOVIE_ROTATION_SPEED_MAX");
            MOVIE_ROTATION_SPEED_DEF = props
                    .getFloatProperty("MOVIE_ROTATION_SPEED_DEF");

            // Settings for the gas cloud octree
            MAX_CLOUD_DEPTH = props.getIntProperty("MAX_CLOUD_DEPTH");
            GAS_EDGES = props.getFloatProperty("GAS_EDGES");

            // Settings that should never change, but are listed here to make
            // sure
            // they
            // can be found if necessary
            MAX_EXPECTED_MODELS = props.getIntProperty("MAX_EXPECTED_MODELS");

            // Minimum and maximum values for the brightness sliders
            POSTPROCESSING_OVERALL_BRIGHTNESS_MIN = props
                    .getFloatProperty("POSTPROCESSING_OVERALL_BRIGHTNESS_MIN");
            POSTPROCESSING_OVERALL_BRIGHTNESS_MAX = props
                    .getFloatProperty("POSTPROCESSING_OVERALL_BRIGHTNESS_MAX");
            POSTPROCESSING_AXES_BRIGHTNESS_MIN = props
                    .getFloatProperty("POSTPROCESSING_AXES_BRIGHTNESS_MIN");
            POSTPROCESSING_AXES_BRIGHTNESS_MAX = props
                    .getFloatProperty("POSTPROCESSING_AXES_BRIGHTNESS_MAX");
            POSTPROCESSING_GAS_BRIGHTNESS_MIN = props
                    .getFloatProperty("POSTPROCESSING_GAS_BRIGHTNESS_MIN");
            POSTPROCESSING_GAS_BRIGHTNESS_MAX = props
                    .getFloatProperty("POSTPROCESSING_GAS_BRIGHTNESS_MAX");
            POSTPROCESSING_STAR_HALO_BRIGHTNESS_MIN = props
                    .getFloatProperty("POSTPROCESSING_STAR_HALO_BRIGHTNESS_MIN");
            POSTPROCESSING_STAR_HALO_BRIGHTNESS_MAX = props
                    .getFloatProperty("POSTPROCESSING_STAR_HALO_BRIGHTNESS_MAX");
            POSTPROCESSING_STAR_BRIGHTNESS_MIN = props
                    .getFloatProperty("POSTPROCESSING_STAR_BRIGHTNESS_MIN");
            POSTPROCESSING_STAR_BRIGHTNESS_MAX = props
                    .getFloatProperty("POSTPROCESSING_STAR_BRIGHTNESS_MAX");

            // Settings for the postprocessing shader
            POSTPROCESSING_OVERALL_BRIGHTNESS_DEF = props
                    .getFloatProperty("POSTPROCESSING_OVERALL_BRIGHTNESS_DEF");
            POSTPROCESSING_AXES_BRIGHTNESS_DEF = props
                    .getFloatProperty("POSTPROCESSING_AXES_BRIGHTNESS_DEF");
            POSTPROCESSING_GAS_BRIGHTNESS_DEF = props
                    .getFloatProperty("POSTPROCESSING_GAS_BRIGHTNESS_DEF");
            POSTPROCESSING_STAR_HALO_BRIGHTNESS_DEF = props
                    .getFloatProperty("POSTPROCESSING_STAR_HALO_BRIGHTNESS_DEF");
            POSTPROCESSING_STAR_BRIGHTNESS_DEF = props
                    .getFloatProperty("POSTPROCESSING_STAR_BRIGHTNESS_DEF");

            // Settings for the star-shape blur method (the + shape of stars)
            STAR_SHAPE_BLUR_SIZE = props.getIntProperty("STAR_SHAPE_BLUR_SIZE");
            STAR_SHAPE_BLURFILTER_SIZE = props
                    .getFloatProperty("STAR_SHAPE_BLURFILTER_SIZE");
            STAR_SHAPE_SIGMA = props.getFloatProperty("STAR_SHAPE_SIGMA");
            STAR_SHAPE_ALPHA = props.getFloatProperty("STAR_SHAPE_ALPHA");
            STAR_SHAPE_BLUR_TYPE = props.getIntProperty("STAR_SHAPE_BLUR_TYPE");

            // Settings for the detail levels.
            LOW_GAS_BLUR_PASSES = props.getIntProperty("LOW_GAS_BLUR_PASSES");
            LOW_GAS_BLUR_SIZE = props.getFloatProperty("LOW_GAS_BLUR_SIZE");
            LOW_GAS_BLUR_TYPE = props.getIntProperty("LOW_GAS_BLUR_TYPE");

            LOW_STAR_HALO_BLUR_PASSES = props
                    .getIntProperty("LOW_STAR_HALO_BLUR_PASSES");
            LOW_STAR_HALO_BLUR_SIZE = props
                    .getFloatProperty("LOW_STAR_HALO_BLUR_SIZE");
            LOW_STAR_HALO_BLUR_TYPE = props
                    .getIntProperty("LOW_STAR_HALO_BLUR_TYPE");

            LOW_GAS_SUBDIVISION = props.getIntProperty("LOW_GAS_SUBDIVISION");
            LOW_STAR_SUBDIVISION = props.getIntProperty("LOW_STAR_SUBDIVISION");
            LOW_GAS_PARTICLES_PER_OCTREE_NODE = props
                    .getIntProperty("LOW_GAS_PARTICLES_PER_OCTREE_NODE");

            MEDIUM_GAS_BLUR_PASSES = props
                    .getIntProperty("MEDIUM_GAS_BLUR_PASSES");
            MEDIUM_GAS_BLUR_SIZE = props
                    .getFloatProperty("MEDIUM_GAS_BLUR_SIZE");
            MEDIUM_GAS_BLUR_TYPE = props.getIntProperty("MEDIUM_GAS_BLUR_TYPE");

            MEDIUM_STAR_HALO_BLUR_PASSES = props
                    .getIntProperty("MEDIUM_STAR_HALO_BLUR_PASSES");
            MEDIUM_STAR_HALO_BLUR_SIZE = props
                    .getFloatProperty("MEDIUM_STAR_HALO_BLUR_SIZE");
            MEDIUM_STAR_HALO_BLUR_TYPE = props
                    .getIntProperty("MEDIUM_STAR_HALO_BLUR_TYPE");

            MEDIUM_GAS_SUBDIVISION = props
                    .getIntProperty("MEDIUM_GAS_SUBDIVISION");
            MEDIUM_STAR_SUBDIVISION = props
                    .getIntProperty("MEDIUM_STAR_SUBDIVISION");
            MEDIUM_GAS_PARTICLES_PER_OCTREE_NODE = props
                    .getIntProperty("MEDIUM_GAS_PARTICLES_PER_OCTREE_NODE");

            HIGH_GAS_BLUR_PASSES = props.getIntProperty("HIGH_GAS_BLUR_PASSES");
            HIGH_GAS_BLUR_SIZE = props.getFloatProperty("HIGH_GAS_BLUR_SIZE");
            HIGH_GAS_BLUR_TYPE = props.getIntProperty("HIGH_GAS_BLUR_TYPE");

            HIGH_STAR_HALO_BLUR_PASSES = props
                    .getIntProperty("HIGH_STAR_HALO_BLUR_PASSES");
            HIGH_STAR_HALO_BLUR_SIZE = props
                    .getFloatProperty("HIGH_STAR_HALO_BLUR_SIZE");
            HIGH_STAR_HALO_BLUR_TYPE = props
                    .getIntProperty("HIGH_STAR_HALO_BLUR_TYPE");

            HIGH_GAS_SUBDIVISION = props.getIntProperty("HIGH_GAS_SUBDIVISION");
            HIGH_STAR_SUBDIVISION = props
                    .getIntProperty("HIGH_STAR_SUBDIVISION");
            HIGH_GAS_PARTICLES_PER_OCTREE_NODE = props
                    .getIntProperty("HIGH_GAS_PARTICLES_PER_OCTREE_NODE");

            // Snaphots have different settings, since they are rendered at
            // extremely
            // high resolutions pixels
            SNAPSHOT_GAS_BLUR_PASSES = props
                    .getIntProperty("SNAPSHOT_GAS_BLUR_PASSES");
            SNAPSHOT_GAS_BLUR_SIZE = props
                    .getFloatProperty("SNAPSHOT_GAS_BLUR_SIZE");
            SNAPSHOT_GAS_BLUR_TYPE = props
                    .getIntProperty("SNAPSHOT_GAS_BLUR_TYPE");

            SNAPSHOT_STAR_HALO_BLUR_PASSES = props
                    .getIntProperty("SNAPSHOT_STAR_HALO_BLUR_PASSES");
            SNAPSHOT_STAR_HALO_BLUR_SIZE = props
                    .getFloatProperty("SNAPSHOT_STAR_HALO_BLUR_SIZE");
            SNAPSHOT_STAR_HALO_BLUR_TYPE = props
                    .getIntProperty("SNAPSHOT_STAR_HALO_BLUR_TYPE");

            GAS_COLOR_INVERTED = props
                    .getBooleanProperty("SNAPSHOT_STAR_HALO_BLUR_TYPE");

        } catch (NumberFormatException e) {
            logger
                    .info("A settings property was most likely entered incorrectly, "
                            + e.getMessage());
        }
    }

    public static int getGasBlurPasses(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_GAS_BLUR_PASSES;
        case 1:
            return MEDIUM_GAS_BLUR_PASSES;
        case 2:
            return HIGH_GAS_BLUR_PASSES;
        }
        return 0;
    }

    public static float getGasBlurSize(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_GAS_BLUR_SIZE;
        case 1:
            return MEDIUM_GAS_BLUR_SIZE;
        case 2:
            return HIGH_GAS_BLUR_SIZE;
        }
        return 0;
    }

    public static int getGasBlurType(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_GAS_BLUR_TYPE;
        case 1:
            return MEDIUM_GAS_BLUR_TYPE;
        case 2:
            return HIGH_GAS_BLUR_TYPE;
        }
        return 0;
    }

    public static int getStarHaloBlurPasses(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_STAR_HALO_BLUR_PASSES;
        case 1:
            return MEDIUM_STAR_HALO_BLUR_PASSES;
        case 2:
            return HIGH_STAR_HALO_BLUR_PASSES;
        }
        return 0;
    }

    public static float getStarHaloBlurSize(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_STAR_HALO_BLUR_SIZE;
        case 1:
            return MEDIUM_STAR_HALO_BLUR_SIZE;
        case 2:
            return HIGH_STAR_HALO_BLUR_SIZE;
        }
        return 0;
    }

    public static int getStarHaloBlurType(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_STAR_HALO_BLUR_TYPE;
        case 1:
            return MEDIUM_STAR_HALO_BLUR_TYPE;
        case 2:
            return HIGH_STAR_HALO_BLUR_TYPE;
        }
        return 0;
    }

    public static int getGasSubdivision(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_GAS_SUBDIVISION;
        case 1:
            return MEDIUM_GAS_SUBDIVISION;
        case 2:
            return HIGH_GAS_SUBDIVISION;
        }
        return 0;
    }

    public static int getStarSubdivision(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_STAR_SUBDIVISION;
        case 1:
            return MEDIUM_STAR_SUBDIVISION;
        case 2:
            return HIGH_STAR_SUBDIVISION;
        }
        return 0;
    }

    public static int getGasParticlesPerOctreeNode(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_GAS_PARTICLES_PER_OCTREE_NODE;
        case 1:
            return MEDIUM_GAS_PARTICLES_PER_OCTREE_NODE;
        case 2:
            return HIGH_GAS_PARTICLES_PER_OCTREE_NODE;
        }
        return 0;
    }

    public static float getPostprocessingOverallBrightness() {
        return POSTPROCESSING_OVERALL_BRIGHTNESS_DEF;
    }

    public static float getPostprocessingAxesBrightness() {
        return POSTPROCESSING_AXES_BRIGHTNESS_DEF;
    }

    public static float getPostprocessingGasBrightness() {
        return POSTPROCESSING_GAS_BRIGHTNESS_DEF;
    }

    public static float getPostprocessingStarHaloBrightness() {
        return POSTPROCESSING_STAR_HALO_BRIGHTNESS_DEF;
    }

    public static float getPostprocessingStarBrightness() {
        return POSTPROCESSING_STAR_BRIGHTNESS_DEF;
    }

    public static int getStarShapeBlurSize() {
        return STAR_SHAPE_BLUR_SIZE;
    }

    public static float getStarShapeBlurfilterSize() {
        return STAR_SHAPE_BLURFILTER_SIZE;
    }

    public static float getStarShapeSigma() {
        return STAR_SHAPE_SIGMA;
    }

    public static float getStarShapeAlpha() {
        return STAR_SHAPE_ALPHA;
    }

    public static int getStarShapeBlurType() {
        return STAR_SHAPE_BLUR_TYPE;
    }

    public static int getSnapshotGasBlurPasses() {
        return SNAPSHOT_GAS_BLUR_PASSES;
    }

    public static float getSnapshotGasBlurSize() {
        return SNAPSHOT_GAS_BLUR_SIZE;
    }

    public static int getSnapshotGasBlurType() {
        return SNAPSHOT_GAS_BLUR_TYPE;
    }

    public static int getSnapshotStarHaloBlurPasses() {
        return SNAPSHOT_STAR_HALO_BLUR_PASSES;
    }

    public static float getSnapshotStarHaloBlurSize() {
        return SNAPSHOT_STAR_HALO_BLUR_SIZE;
    }

    public static int getSnapshotStarHaloBlurType() {
        return SNAPSHOT_STAR_HALO_BLUR_TYPE;
    }

    public static int getDefaultScreenWidth() {
        return DEFAULT_SCREEN_WIDTH;
    }

    public static int getDefaultScreenHeight() {
        return DEFAULT_SCREEN_HEIGHT;
    }

    public static int getScreenshotScreenWidth() {
        return SCREENSHOT_SCREEN_WIDTH;
    }

    public static int getScreenshotScreenHeight() {
        return SCREENSHOT_SCREEN_HEIGHT;
    }

    public static int getMaxCloudDepth() {
        return MAX_CLOUD_DEPTH;
    }

    public static float getGasEdges() {
        return GAS_EDGES;
    }

    public static int getMaxExpectedModels() {
        return MAX_EXPECTED_MODELS;
    }

    public static float getPostprocessingOverallBrightnessMin() {
        return POSTPROCESSING_OVERALL_BRIGHTNESS_MIN;
    }

    public static float getPostprocessingOverallBrightnessMax() {
        return POSTPROCESSING_OVERALL_BRIGHTNESS_MAX;
    }

    public static float getPostprocessingAxesBrightnessMin() {
        return POSTPROCESSING_AXES_BRIGHTNESS_MIN;
    }

    public static float getPostprocessingAxesBrightnessMax() {
        return POSTPROCESSING_AXES_BRIGHTNESS_MAX;
    }

    public static float getPostprocessingGasBrightnessMin() {
        return POSTPROCESSING_GAS_BRIGHTNESS_MIN;
    }

    public static float getPostprocessingGasBrightnessMax() {
        return POSTPROCESSING_GAS_BRIGHTNESS_MAX;
    }

    public static float getPostprocessingStarHaloBrightnessMin() {
        return POSTPROCESSING_STAR_HALO_BRIGHTNESS_MIN;
    }

    public static float getPostprocessingStarHaloBrightnessMax() {
        return POSTPROCESSING_STAR_HALO_BRIGHTNESS_MAX;
    }

    public static float getPostprocessingStarBrightnessMin() {
        return POSTPROCESSING_STAR_BRIGHTNESS_MIN;
    }

    public static float getPostprocessingStarBrightnessMax() {
        return POSTPROCESSING_STAR_BRIGHTNESS_MAX;
    }

    public static void setPostprocessingOverallBrightness(float value) {
        POSTPROCESSING_OVERALL_BRIGHTNESS_DEF = value;
    }

    public static void setPostprocessingAxesBrightness(float value) {
        POSTPROCESSING_AXES_BRIGHTNESS_DEF = value;
    }

    public static void setPostprocessingGasBrightness(float value) {
        POSTPROCESSING_GAS_BRIGHTNESS_DEF = value;
    }

    public static void setPostprocessingStarHaloBrightness(float value) {
        POSTPROCESSING_STAR_HALO_BRIGHTNESS_DEF = value;
    }

    public static void setPostprocessingStarBrightness(float value) {
        POSTPROCESSING_STAR_BRIGHTNESS_DEF = value;
    }

    public static float getInitialRotationX() {
        return INITIAL_ROTATION_X;
    }

    public static float getInitialRotationY() {
        return INITIAL_ROTATION_Y;
    }

    public static float getInitialZoom() {
        return INITIAL_ZOOM;
    }

    public static void setMovieRotate(int stateChange) {
        if (stateChange == 1)
            MOVIE_ROTATE = true;
        if (stateChange == 2)
            MOVIE_ROTATE = false;
    }

    public static boolean getMovieRotate() {
        return MOVIE_ROTATE;
    }

    public static void setMovieRotationSpeed(float value) {
        MOVIE_ROTATION_SPEED_DEF = value;
    }

    public static float getMovieRotationSpeedMin() {
        return MOVIE_ROTATION_SPEED_MIN;
    }

    public static float getMovieRotationSpeedMax() {
        return MOVIE_ROTATION_SPEED_MAX;
    }

    public static float getMovieRotationSpeedDef() {
        return MOVIE_ROTATION_SPEED_DEF;
    }

    public static int getInitialSimulationFrame() {
        return INITIAL_SIMULATION_FRAME;
    }

    public static void setInitial_simulation_frame(int initialSimulationFrame) {
        INITIAL_SIMULATION_FRAME = initialSimulationFrame;
    }

    public static void setInitial_rotation_x(float initialRotationX) {
        INITIAL_ROTATION_X = initialRotationX;
    }

    public static void setInitial_rotation_y(float initialRotationY) {
        INITIAL_ROTATION_Y = initialRotationY;
    }

    public static boolean invertGasColor() {
        return GAS_COLOR_INVERTED;
    }

    public static void setInvertGasColor(int stateChange) {
        if (stateChange == 1)
            GAS_COLOR_INVERTED = true;
        if (stateChange == 2)
            GAS_COLOR_INVERTED = false;
    }
}

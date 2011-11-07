package ibis.deploy.gui.outputViz;

public class Settings {

    // Size settings for default startup and screenshots
    private static final int DEFAULT_SCREEN_WIDTH = 1024;
    private static final int DEFAULT_SCREEN_HEIGHT = 768;

    private static final int SCREENSHOT_SCREEN_WIDTH = 1280;
    private static final int SCREENSHOT_SCREEN_HEIGHT = 720;

    // Settings for the initial view
    private static final int INITIAL_SIMULATION_FRAME = 0;
    private static int initial_simulation_frame = INITIAL_SIMULATION_FRAME;
    private static final float INITIAL_ROTATION_X = 0f;
    private static float initial_rotation_x = INITIAL_ROTATION_X;
    private static final float INITIAL_ROTATION_Y = 0f;
    private static float initial_rotation_y = INITIAL_ROTATION_Y;
    private static final float INITIAL_ZOOM = -540.0f;

    // Setting per movie frame
    private static final float PER_FRAME_ROTATION = 0.5f / 3.0f;

    // Settings for the gas cloud octree
    private static final int MAX_CLOUD_DEPTH = 25;
    private static final float GAS_EDGES = 800f;

    // Settings that should never change, but are listed here to make sure they
    // can be found if necessary
    private static final int MAX_EXPECTED_MODELS = 1000;

    // Minimum and maximum values for the brightness sliders
    private static final float POSTPROCESSING_OVERALL_BRIGHTNESS_MIN = 0f;
    private static final float POSTPROCESSING_OVERALL_BRIGHTNESS_MAX = 10f;
    private static final float POSTPROCESSING_AXES_BRIGHTNESS_MIN = 0f;
    private static final float POSTPROCESSING_AXES_BRIGHTNESS_MAX = 4f;
    private static final float POSTPROCESSING_GAS_BRIGHTNESS_MIN = 0f;
    private static final float POSTPROCESSING_GAS_BRIGHTNESS_MAX = 4f;
    private static final float POSTPROCESSING_STAR_HALO_BRIGHTNESS_MIN = 0f;
    private static final float POSTPROCESSING_STAR_HALO_BRIGHTNESS_MAX = 4f;
    private static final float POSTPROCESSING_STAR_BRIGHTNESS_MIN = 0f;
    private static final float POSTPROCESSING_STAR_BRIGHTNESS_MAX = 4f;

    // Settings for the postprocessing shader
    private static float postprocessing_overall_brightness = 4f;
    private static float postprocessing_axes_brightness = 1f;
    private static float postprocessing_gas_brightness = 1.75f;
    private static float postprocessing_star_halo_brightness = 2f;
    private static float postprocessing_star_brightness = 2f;

    // Settings for the star-shape blur method (the + shape of stars)
    private static final int STAR_SHAPE_BLUR_SIZE = 1;
    private static final float STAR_SHAPE_BLURFILTER_SIZE = 8f;
    private static final float STAR_SHAPE_SIGMA = 100f;
    private static final float STAR_SHAPE_ALPHA = 0.5f;
    private static final int STAR_SHAPE_BLUR_TYPE = 0;

    // Settings for the detail levels.
    private static int LOW_GAS_BLUR_PASSES = 0;
    private static float LOW_GAS_BLUR_SIZE = 2;
    private static int LOW_GAS_BLUR_TYPE = 8;

    private static int LOW_STAR_HALO_BLUR_PASSES = 1;
    private static float LOW_STAR_HALO_BLUR_SIZE = 1;
    private static final int LOW_STAR_HALO_BLUR_TYPE = 6;

    private static final int LOW_GAS_SUBDIVISION = 0;
    private static final int LOW_STAR_SUBDIVISION = 1;
    private static final int LOW_GAS_PARTICLES_PER_OCTREE_NODE = 100;

    private static final int MEDIUM_GAS_BLUR_PASSES = 1;
    private static final float MEDIUM_GAS_BLUR_SIZE = 2;
    private static final int MEDIUM_GAS_BLUR_TYPE = 8;

    private static final int MEDIUM_STAR_HALO_BLUR_PASSES = 1;
    private static final float MEDIUM_STAR_HALO_BLUR_SIZE = 1;
    private static final int MEDIUM_STAR_HALO_BLUR_TYPE = 6;

    private static final int MEDIUM_GAS_SUBDIVISION = 1;
    private static final int MEDIUM_STAR_SUBDIVISION = 2;
    private static final int MEDIUM_GAS_PARTICLES_PER_OCTREE_NODE = 25;

    private static final int HIGH_GAS_BLUR_PASSES = 2;
    private static final float HIGH_GAS_BLUR_SIZE = 2;
    private static final int HIGH_GAS_BLUR_TYPE = 8;

    private static final int HIGH_STAR_HALO_BLUR_PASSES = 2;
    private static final float HIGH_STAR_HALO_BLUR_SIZE = 1;
    private static final int HIGH_STAR_HALO_BLUR_TYPE = 6;

    private static final int HIGH_GAS_SUBDIVISION = 1;
    private static final int HIGH_STAR_SUBDIVISION = 3;
    private static final int HIGH_GAS_PARTICLES_PER_OCTREE_NODE = 2;

    // Snaphots have different settings, since they are rendered at extremely
    // high resolutions pixels
    private static final int SNAPSHOT_GAS_BLUR_PASSES = 2; // 2
    private static final float SNAPSHOT_GAS_BLUR_SIZE = 2; // 6
    private static final int SNAPSHOT_GAS_BLUR_TYPE = 8; // 10

    private static final int SNAPSHOT_STAR_HALO_BLUR_PASSES = 2; // 2
    private static final float SNAPSHOT_STAR_HALO_BLUR_SIZE = 1; // 1
    private static final int SNAPSHOT_STAR_HALO_BLUR_TYPE = 6; // 6

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
        return postprocessing_overall_brightness;
    }

    public static float getPostprocessingAxesBrightness() {
        return postprocessing_axes_brightness;
    }

    public static float getPostprocessingGasBrightness() {
        return postprocessing_gas_brightness;
    }

    public static float getPostprocessingStarHaloBrightness() {
        return postprocessing_star_halo_brightness;
    }

    public static float getPostprocessingStarBrightness() {
        return postprocessing_star_brightness;
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
        postprocessing_overall_brightness = value;
    }

    public static void setPostprocessingAxesBrightness(float value) {
        postprocessing_axes_brightness = value;
    }

    public static void setPostprocessingGasBrightness(float value) {
        postprocessing_gas_brightness = value;
    }

    public static void setPostprocessingStarHaloBrightness(float value) {
        postprocessing_star_halo_brightness = value;
    }

    public static void setPostprocessingStarBrightness(float value) {
        postprocessing_star_brightness = value;
    }

    public static float getInitialRotationX() {
        return initial_rotation_x;
    }

    public static float getInitialRotationY() {
        return initial_rotation_y;
    }

    public static float getInitialZoom() {
        return INITIAL_ZOOM;
    }

    public static float getPerFrameRotation() {
        return PER_FRAME_ROTATION;
    }

    public static int getInitialSimulationFrame() {
        return initial_simulation_frame;
    }

    public static void setInitial_simulation_frame(int initialSimulationFrame) {
        initial_simulation_frame = initialSimulationFrame;
    }

    public static void setInitial_rotation_x(float initialRotationX) {
        initial_rotation_x = initialRotationX;
    }

    public static void setInitial_rotation_y(float initialRotationY) {
        initial_rotation_y = initialRotationY;
    }
}

package ibis.deploy.gui.outputViz;

public class Settings {
    // Settings for the postprocessing shader
    private static final float POSTPROCESSING_OVERALL_BRIGHTNESS = 4f;
    private static final float POSTPROCESSING_AXES_BRIGHTNESS = 1f;
    private static final float POSTPROCESSING_GAS_BRIGHTNESS = 1.75f;
    private static final float POSTPROCESSING_STAR_HALO_BRIGHTNESS = 1f;
    private static final float POSTPROCESSING_STAR_BRIGHTNESS = 2f;

    // Settings for the star-shape blur method (the + shape of stars)
    private static final int STAR_SHAPE_BLUR_SIZE = 2;
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

    private static final int HIGH_GAS_BLUR_PASSES2 = 2;
    private static final float HIGH_GAS_BLUR_SIZE2 = 2;
    private static final int HIGH_GAS_BLUR_TYPE2 = 8;

    private static final int HIGH_STAR_HALO_BLUR_PASSES2 = 2;
    private static final float HIGH_STAR_HALO_BLUR_SIZE2 = 1;
    private static final int HIGH_STAR_HALO_BLUR_TYPE2 = 6;

    private static final int HIGH_GAS_SUBDIVISION = 1;
    private static final int HIGH_STAR_SUBDIVISION = 3;
    private static final int HIGH_GAS_PARTICLES_PER_OCTREE_NODE = 2;

    // Snaphots have different settings, since they are rendered at extremely
    // high resolutions pixels
    private static final int SNAPSHOT_GAS_BLUR_PASSES = 2;
    private static final float SNAPSHOT_GAS_BLUR_SIZE = 4;
    private static final int SNAPSHOT_GAS_BLUR_TYPE = 8;

    private static final int SNAPSHOT_STAR_HALO_BLUR_PASSES = 2;
    private static final float SNAPSHOT_STAR_HALO_BLUR_SIZE = 1;
    private static final int SNAPSHOT_STAR_HALO_BLUR_TYPE = 6;

    public static int getGasBlurPasses(int levelOfDetail) {
        switch (levelOfDetail) {
        case 0:
            return LOW_GAS_BLUR_PASSES;
        case 1:
            return MEDIUM_GAS_BLUR_PASSES;
        case 2:
            return HIGH_GAS_BLUR_PASSES2;
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
            return HIGH_GAS_BLUR_SIZE2;
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
            return HIGH_GAS_BLUR_TYPE2;
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
            return HIGH_STAR_HALO_BLUR_PASSES2;
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
            return HIGH_STAR_HALO_BLUR_SIZE2;
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
            return HIGH_STAR_HALO_BLUR_TYPE2;
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
        return POSTPROCESSING_OVERALL_BRIGHTNESS;
    }

    public static float getPostprocessingAxesBrightness() {
        return POSTPROCESSING_AXES_BRIGHTNESS;
    }

    public static float getPostprocessingGasBrightness() {
        return POSTPROCESSING_GAS_BRIGHTNESS;
    }

    public static float getPostprocessingStarHaloBrightness() {
        return POSTPROCESSING_STAR_HALO_BRIGHTNESS;
    }

    public static float getPostprocessingStarBrightness() {
        return POSTPROCESSING_STAR_BRIGHTNESS;
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
}

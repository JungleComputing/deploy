package ibis.amuse.visualization.amuseAdaptor;

import ibis.amuse.visualization.Settings;
import ibis.amuse.visualization.openglCommon.exceptions.FileOpeningException;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.scenegraph.OctreeNode;

import java.util.ArrayList;
import java.util.HashMap;

public class Hdf5Snapshotter {
    private static OctreeNode cubeRoot;
    private ArrayList<Star> stars;

    private final static String evoNamePostfix = ".evo";
    private final static String gravNamePostfix = ".grav";
    private final static String gasNamePostfix = ".gas";

    private int lastDisplayedFrame = -1;

    public Hdf5Snapshotter() {
        stars = new ArrayList<Star>();
    }

    public void open(String namePrefix, int currentFrame, int levelOfDetail,
            HashMap<Integer, Model> cloudModels, boolean overrideUpdate)
            throws FileOpeningException {

        if (currentFrame != lastDisplayedFrame || overrideUpdate) {
            int gasSubdivision = Settings.getGasSubdivision(levelOfDetail);
            int starSubdivision = Settings.getStarSubdivision(levelOfDetail);
            int gasParticlesPerOctreeNode = Settings
                    .getGasParticlesPerOctreeNode(levelOfDetail);

            String evoName, gravName, gasName;

            gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
            evoName = namePrefix + intToString(currentFrame) + evoNamePostfix;
            gravName = namePrefix + intToString(currentFrame) + gravNamePostfix;

            cubeRoot = new OctreeNode(gasParticlesPerOctreeNode, 0,
                    gasSubdivision, cloudModels, new VecF3(-Settings
                            .getGasEdges(), -Settings.getGasEdges(), -Settings
                            .getGasEdges()), Settings.getGasEdges());
            Hdf5GasCloudReader.read(cubeRoot, gasName);

            stars = Hdf5StarReader.read(starSubdivision, evoName, gravName);

            if (Settings.getGasStarInfluencedColor()) {
                cubeRoot.recolor(stars);
            }

            lastDisplayedFrame = currentFrame;
        }
    }

    public OctreeNode getOctreeRoot() {
        return cubeRoot;
    }

    public ArrayList<Star> getStars() {
        return stars;
    }

    private static String intToString(int input) {
        String result = "";
        if (input < 10) {
            result += "00000" + input;
        } else if (input >= 10 && input < 100) {
            result += "0000" + input;
        } else if (input >= 100 && input < 1000) {
            result += "000" + input;
        } else if (input >= 1000 && input < 10000) {
            result += "00" + input;
        } else if (input >= 10000 && input < 100000) {
            result += "0" + input;
        } else {
            result += input;
        }

        return result;
    }
}

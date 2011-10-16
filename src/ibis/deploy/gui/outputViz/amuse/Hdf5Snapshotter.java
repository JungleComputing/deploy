package ibis.deploy.gui.outputViz.amuse;

import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.scenegraph.OctreeNode;
import ibis.deploy.gui.outputViz.exceptions.FileOpeningException;
import ibis.deploy.gui.outputViz.models.Model;

import java.util.HashMap;

public class Hdf5Snapshotter {
    private static OctreeNode cubeRoot;
    private static StarSGNode sgRoot;

    private final static String evoNamePostfix = ".evo";
    private final static String gravNamePostfix = ".grav";
    private final static String gasNamePostfix = ".gas";

    public Hdf5Snapshotter() {
    }

    public void open(String namePrefix, int currentFrame, int levelOfDetail, HashMap<Integer, Model> starModels,
            HashMap<Integer, Model> cloudModels) {
        int gasSubdivision = 0, starSubdivision = 0, gasParticlesPerOctreeNode = 200;
        if (levelOfDetail == 0) {
            gasSubdivision = 0;
            starSubdivision = 1;
            gasParticlesPerOctreeNode = 100;
        } else if (levelOfDetail == 1) {
            gasSubdivision = 1;
            starSubdivision = 2;
            gasParticlesPerOctreeNode = 25;
        } else if (levelOfDetail == 2) {
            gasSubdivision = 1;
            starSubdivision = 3;
            gasParticlesPerOctreeNode = 2;
        }

        String evoName, gravName, gasName;

        try {
            gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
            evoName = namePrefix + intToString(currentFrame) + evoNamePostfix;
            gravName = namePrefix + intToString(currentFrame) + gravNamePostfix;

            cubeRoot = new OctreeNode(gasParticlesPerOctreeNode, 0, gasSubdivision, cloudModels, new Vec3(
                    -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
            Hdf5GasCloudReader.read(cubeRoot, gasName);

            sgRoot = new StarSGNode();
            Hdf5StarReader.read(sgRoot, starModels, starSubdivision, evoName, gravName);
        } catch (FileOpeningException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public OctreeNode getOctreeRoot() {
        return cubeRoot;
    }

    public StarSGNode getSgRoot() {
        return sgRoot;
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

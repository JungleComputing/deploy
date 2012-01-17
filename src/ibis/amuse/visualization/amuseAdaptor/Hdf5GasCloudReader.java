package ibis.amuse.visualization.amuseAdaptor;


import ibis.amuse.visualization.Settings;
import ibis.amuse.visualization.openglCommon.exceptions.FileOpeningException;
import ibis.amuse.visualization.openglCommon.scenegraph.OctreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

public class Hdf5GasCloudReader {
    private static ArrayList<FileFormat> openFiles = new ArrayList<FileFormat>();

    // public long[] keys;

    public Hdf5GasCloudReader() {
    }

    public static void read(OctreeNode cubeRoot, String gasName)
            throws FileOpeningException {

        HashMap<String, Dataset> result = new HashMap<String, Dataset>();

        List<HObject> memberList = getRoot(gasName).getMemberList();
        traverse("gas", result, memberList);

        Dataset keysSet = result.get("gas/particles/0000000001/keys");
        int numParticles = (int) keysSet.getDims()[0];

        double[] x, y, z, u;

        // keys = (long[]) keysSet.read();
        try {
            x = (double[]) result.get("gas/particles/0000000001/attributes/x")
                    .read();
            y = (double[]) result.get("gas/particles/0000000001/attributes/y")
                    .read();
            z = (double[]) result.get("gas/particles/0000000001/attributes/z")
                    .read();
            u = (double[]) result.get("gas/particles/0000000001/attributes/u")
                    .read();

            for (int i = 0; i < numParticles; i++) {
                float px = (float) (x[i] / 10E14);
                float py = (float) (y[i] / 10E14);
                float pz = (float) (z[i] / 10E14);

                if (!(px < -Settings.getGasEdges()
                        && px > Settings.getGasEdges()
                        && py < -Settings.getGasEdges()
                        && py > Settings.getGasEdges()
                        && pz < -Settings.getGasEdges() && pz > Settings
                        .getGasEdges())) {
                    cubeRoot.addGas(Astrophysics.locationToScreenCoord(x[i],
                            y[i], z[i]), u[i]);
                }
            }
            cubeRoot.doneAddingGas();
        } catch (FileOpeningException e) {
            throw e;
        } catch (Exception e) {
            System.err
                    .println("General Exception cought in Hdf5GasCloudReader.");
            e.printStackTrace();
        }

        for (Dataset d : result.values()) {
            d.clear();
            d.close(0);
        }

        closeFiles();
    }

    protected static Group getRoot(String filename) throws FileOpeningException {
        // retrieve an instance of H5File
        FileFormat fileFormat = FileFormat
                .getFileFormat(FileFormat.FILE_TYPE_HDF5);
        if (fileFormat == null) {
            throw new FileOpeningException("Cannot find HDF5 FileFormat.");
        }

        // open the file with read and write access
        FileFormat file = null;
        try {
            file = fileFormat.createInstance(filename, FileFormat.READ);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (file == null) {
            throw new FileOpeningException(
                    "Failed to open file, file is null: " + filename);
        }

        // open the file and retrieve the file structure
        try {
            file.open();
        } catch (Exception e) {
            throw new FileOpeningException("Failed to open file: " + filename,
                    e);
        }

        openFiles.add(file);

        return (Group) ((javax.swing.tree.DefaultMutableTreeNode) file
                .getRootNode()).getUserObject();
    }

    protected static void traverse(String prefix,
            HashMap<String, Dataset> result, List<HObject> memberList) {
        for (HObject o : memberList) {
            if (o instanceof Group) {
                traverse(prefix, result, ((Group) o).getMemberList());
            } else if (o instanceof Dataset) {
                // System.out.println(prefix+o.getFullName());
                result.put(prefix + o.getFullName(), (Dataset) o);
                ((Dataset) o).init();
            } else {
                System.err.println("Unknown object type discovered: "
                        + o.getFullName());
                System.exit(1);
            }
        }
    }

    protected static void closeFiles() {
        for (FileFormat f : openFiles) {
            // close file resource
            try {
                f.close();
            } catch (Exception e) {
                System.err.println("Error closing file: " + f.getPath());
                e.printStackTrace();
            }
        }

        openFiles.clear();
    }
}

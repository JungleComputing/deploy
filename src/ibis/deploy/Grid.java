package ibis.deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Grid containing clusters. Basically just a HashMap with cluster objects, and
 * some functions for loading and saving grids.S
 * 
 * @author Niels Drost
 * 
 */
public class Grid {

    private final List<Cluster> clusters;

    /**
     * Constructs a new empty grid.
     */
    public Grid() {
        clusters = new ArrayList<Cluster>();

        try {
            clusters.add(Cluster.getLocalCluster());
        } catch (Exception e) {
            // should not happen
            throw new RuntimeException("exception while creating grid", e);
        }
    }

    /**
     * Constructs a grid object from properties stored in the given file. Also
     * constructs the clusters inside this grid.
     * 
     * @param file
     *            the file containing the properties
     * @throws FileNotFoundException
     *             if the given file cannot be found
     * @throws Exception
     *             if reading from the given file fails, or the file contains
     *             invalid properties
     */
    public Grid(File file) throws FileNotFoundException, Exception {
        clusters = new ArrayList<Cluster>();

        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        if (!file.getName().endsWith(".grid")) {
            throw new Exception("grid files must have a \".grid\" extension");
        }

        clusters.add(Cluster.getLocalCluster());

        DeployProperties properties = new DeployProperties();
        properties.loadFromFile(file.getAbsolutePath());

        String[] clusterNames = properties.getElementList("");
        if (clusterNames != null) {
            for (String clusterName : clusterNames) {
                Cluster cluster = getCluster(clusterName);

                if (cluster == null) {
                    cluster = new Cluster(clusterName);
                    addCluster(cluster);
                }

                // add default properties (if any)
                cluster.loadFromProperties(properties, "default");

                // add normal properties
                cluster.loadFromProperties(properties, clusterName);

            }
        }
    }

    /**
     * Returns the Clusters in this Grid.
     * 
     * @return the clusters in this Grid
     */
    public Cluster[] getClusters() {
        return clusters.toArray(new Cluster[0]);
    }

    /**
     * Removes the cluster with the given name from the grid (if it belongs to
     * the grid at all).
     * 
     * @param name
     *            the name of the cluster to be removed from this group
     */
    public void removeCluster(String name) {
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).getName().equals(name)) {
                clusters.remove(i);
                // go back one
                i--;
            }
        }
    }

    /**
     * Adds a new cluster to this grid.
     * 
     * @param cluster
     *            the cluster.
     *            
     * @throws AlreadyExistsException
     *             if the cluster (name) is already present in this grid
     */
    public void addCluster(Cluster cluster) throws Exception {
        if (hasCluster(cluster.getName())) {
            throw new AlreadyExistsException(
                    "Cannot add cluster, cluster with name \""
                            + cluster.getName() + "\" already exists");
        }

        clusters.add(cluster);
    }

    /**
     * Returns if a cluster with the given name exists.
     * 
     * @param name
     *            name of the cluster.
     * @return if a cluster with the given name exists.
     */
    public boolean hasCluster(String name) {
        for (Cluster cluster : clusters) {
            if (cluster.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an cluster with a given name from this Grid
     * 
     * @param clusterName
     *            the name of the cluster to search for
     * @return the cluster with the given name, or <code>null</code> if no
     *         clusters with the given name exist in this Grid.
     */
    public Cluster getCluster(String clusterName) {
        for (Cluster cluster : clusters) {
            if (cluster.getName().equals(clusterName)) {
                return cluster;
            }
        }
        return null;
    }

    /**
     * Save this grid and all contained clusters to a property file
     * 
     * @param file
     *            file to save grid to
     * 
     * @throws Exception
     *             in case file cannot be written
     */
    public void save(File file) throws Exception {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("failed to create a new file '" + file
                        + "'.");
            }
        }
        PrintWriter out = new PrintWriter(file);

        out.println("# Grid file, " + "generated by Ibis Deploy on "
                + new Date(System.currentTimeMillis()));
        out.println();

        save(out, null);

        out.flush();
        out.close();
    }

    /**
     * Save this grid and all contained clusters to the given stream
     * 
     * @param out
     *            stream to save grid to
     * @param prefix
     *            prefix for all keys written
     * @throws Exception
     *             in case data cannot be written
     */
    public void save(PrintWriter out, String prefix) throws Exception {
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        Cluster.printTableOfKeys(out);
        out.println();

        // write clusters
        for (Cluster cluster : clusters) {
            if (!cluster.getName().equals("local")) {
                out.println();
                out.println("# Details of cluster \"" + cluster.getName()
                        + "\"");
                cluster.save(out, prefix + cluster.getName(), true);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Grid containing " + clusters.size() + " clusters";
    }

    /**
     * Returns an info string suitable for printing (with newlines)
     * 
     * @return an info string suitable for printing (with newlines)
     */
    public String toPrintString() {
        String result = "Grid containing " + clusters.size() + " clusters:\n";

        for (Cluster cluster : clusters) {
            result += cluster.toPrintString() + "\n";
        }

        return result;
    }
}

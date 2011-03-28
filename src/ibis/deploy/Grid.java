package ibis.deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Grid containing clusters. Also has a list of default values.
 * 
 * @author Niels Drost
 * 
 */
public class Grid {

    public static final String LOCAL_COLOR = "#FF0000";

    // colors used for clusters
    private static final String[] COLORS = { "#FF8000", "#FFFF00", "#80FF00",
            "#00FFFF", "#007FFF", "#0000FF", "#8000FF", "#FF00FF", "#FF0080",
            "#FF8080", "#FFBF80", "#FFFF80", "#BFFF80", "#80FF80", "#80FFBF",
            "#80FFFF", "#80BFFF", "#8080FF", "#BF80FF", "#FF80FF", "#FF80BF",
            "#800000", "#804000", "#808000", "#00FF00", "#408000", "#008000",
            "#008040", "#008080", "#004080", "#000080", "#400080", "#00FF80",
            "#800080", "#800040" };

    private final List<Cluster> clusters;

    private int nextColor = 0;

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

        DeployProperties properties = new DeployProperties();
        properties.loadFromFile(file.getAbsolutePath());

        loadSettingsFromProperties(properties, null);
    }

    /**
     * Constructs a grid object from the given properties. Also constructs the
     * clusters inside this grid.
     * 
     * @param properties
     *            properties of the grid
     * @param prefix
     *            prefix to use on all keys
     * @throws Exception
     *             if cluster cannot be read properly
     * 
     */
    public Grid(DeployProperties properties, String prefix) throws Exception {
        clusters = new ArrayList<Cluster>();
        loadSettingsFromProperties(properties, prefix);
    }

    /**
     * Initializes the grid using data from the given properties
     * 
     * @param properties
     *            properties of the grid
     * @param prefix
     *            prefix to use on all keys
     **/
    private void loadSettingsFromProperties(DeployProperties properties,
            String prefix) throws Exception {
        if (prefix == null) {
            prefix = "";
        } else {
            prefix = prefix + ".";
        }

        String[] clusterNames = properties.getElementList(prefix);
        if (clusterNames != null) {
            for (String clusterName : clusterNames) {
                Cluster cluster = new Cluster(properties, clusterName, prefix
                        + clusterName);
                if (!clusterName.equals("local")) {
                    cluster.setColor(getNextColor());
                } else {
                    cluster.setColor(LOCAL_COLOR);
                    cluster.setVisibleOnMap(false);
                }
                clusters.add(cluster);
            }
        }

        // add "local" cluster if it doesn't exist yet
        if (!hasCluster("local")) {
            Cluster local = new Cluster("local");
            local.setColor(LOCAL_COLOR);
            local.setVisibleOnMap(false);
            clusters.add(local);
        }
    }

    /**
     * Constructs a new empty grid.
     */
    public Grid() {
        clusters = new ArrayList<Cluster>();
        try {
            Cluster local = new Cluster("local");
            local.setColor(LOCAL_COLOR);
            local.setVisibleOnMap(false);
            clusters.add(local);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized String getNextColor() {
        String result = COLORS[nextColor];

        nextColor++;
        nextColor = nextColor % COLORS.length;

        return result;
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
     * Removes the given cluster from the grid (if it belongs to the grid at
     * all).
     * 
     * @param cluster
     *            the cluster to be removed from this group
     */
    public void removeCluster(Cluster cluster) {
        clusters.remove(cluster);
    }

    /**
     * Creates a new cluster in this grid, with a given name.
     * 
     * @param name
     *            the name of the cluster.
     * 
     * @return the new cluster.
     * 
     * @throws Exception
     *             if the name given is <code>null</code>
     */
    public Cluster createNewCluster(String name) throws Exception {
        if (hasCluster(name)) {
            throw new AlreadyExistsException("Cannot add cluster, cluster \""
                    + name + "\" already exists");
        }

        Cluster result = new Cluster(name);
        result.setColor(getNextColor());

        clusters.add(result);

        return result;
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

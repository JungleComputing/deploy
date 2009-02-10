package ibis.deploy;

import ibis.util.TypedProperties;

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

    // cluster representing defaults
    private final Cluster defaults;

    // cluster representing defaults for local machine.
    private final Cluster localDefaults;

    private final List<Cluster> clusters;

    /**
     * Constructs a grid object from properties stored in the given file. Also
     * constructs the clusters inside this grid.
     * 
     * @param file
     *                the file containing the properties
     * @throws FileNotFoundException
     *                 if the given file cannot be found
     * @throws Exception
     *                 if reading from the given file fails, or the file
     *                 contains invalid properties
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

        TypedProperties properties = new TypedProperties();

        properties.loadFromFile(file.getAbsolutePath());

        defaults = new Cluster(properties, "defaults", "default", null);

        localDefaults = Cluster.getLocalCluster();

        String[] clusterNames = Util.getElementList(properties);
        if (clusterNames != null) {
            for (String clusterName : clusterNames) {
                Cluster cluster = new Cluster(properties, clusterName,
                        clusterName, this);
                clusters.add(cluster);
            }
        }

        // add "local" cluster if it doesn't exist yet
        if (!hasCluster("local")) {
            createNewCluster("local");
        }
    }

    /**
     * Constructs a grid object from the given properties. Also constructs the
     * clusters inside this grid.
     * 
     * @param properties
     *                properties of the grid
     * @param prefix
     *                prefix to use on all keys
     * @throws Exception
     *                 if cluster cannot be read properly
     * 
     */
    public Grid(TypedProperties properties, String prefix) throws Exception {
        clusters = new ArrayList<Cluster>();

        if (prefix == null) {
            prefix = "";
        } else {
            prefix = prefix + ".";
        }

        defaults = new Cluster(properties, "defaults", prefix + "default", null);

        localDefaults = Cluster.getLocalCluster();

        String[] clusterNames = Util.getElementList(properties, prefix);
        if (clusterNames != null) {
            for (String clusterName : clusterNames) {
                Cluster cluster = new Cluster(properties, clusterName, prefix
                        + clusterName, this);
                clusters.add(cluster);
            }
        }

        // add "local" cluster if it doesn't exist yet
        if (!hasCluster("local")) {
            createNewCluster("local");
        }
    }

    /**
     * Constructs a new empty grid.
     */
    public Grid() {
        clusters = new ArrayList<Cluster>();
        try {
            defaults = new Cluster("defaults", null);
            // add a default local cluster.
            localDefaults = Cluster.getLocalCluster();
            Cluster local = new Cluster("local", this);
            clusters.add(local);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
     * Removes the given cluster from the grid (if it belongs to the grid at
     * all).
     * 
     * @param cluster
     *                the cluster to be removed from this group
     */
    public void removeCluster(Cluster cluster) {
        clusters.remove(cluster);
    }

    /**
     * Creates a new cluster in this grid, with a given name.
     * 
     * @param name
     *                the name of the cluster.
     * 
     * @return the new cluster.
     * 
     * @throws Exception
     *                 if the name given is <code>null</code>
     */
    public Cluster createNewCluster(String name) throws Exception {
        if (hasCluster(name)) {
            throw new AlreadyExistsException("Cannot add cluster, cluster \"" + name
                    + "\" already exists");
        }

        Cluster result = new Cluster(name, this);

        clusters.add(result);

        return result;
    }

    /**
     * Returns if a cluster with the given name exists.
     * 
     * @param name
     *                name of the cluster.
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
     *                the name of the cluster to search for
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
     * Returns cluster representing defaults of this grid.
     * 
     * @return cluster representing defaults of this grid.
     */
    public Cluster getDefaults() {
        return defaults;
    }

    /**
     * Returns cluster representing defaults of this machine.
     * 
     * @return cluster representing defaults of this machine.
     */
    public Cluster getLocalDefaults() {
        return localDefaults;
    }

    /**
     * Save this grid and all contained clusters to a property file
     * 
     * @param file
     *                file to save grid to
     * 
     * @throws Exception
     *                 in case file cannot be written
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
     *                stream to save grid to
     * @param prefix
     *                prefix for all keys written
     * @throws Exception
     *                 in case data cannot be written
     */
    public void save(PrintWriter out, String prefix) throws Exception {
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        // write defaults
        Cluster.printTableOfKeys(out);
        out.println();
        out.println("# Default settings:");
        defaults.save(out, prefix + "default", true);

        // write clusters
        for (Cluster cluster : clusters) {
            if (cluster.getName().equals("local")) {
                if (!cluster.isEmpty()) {
                    out.println();
                    out
                            .println("# Settings overriding default \"local\" cluster");
                    cluster.save(out, prefix + cluster.getName(), false);
                }
            } else {
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
        String result = "Grid containing " + clusters.size()
                + " clusters:\n\nDefault ";

        result += defaults.toPrintString() + "\n";

        for (Cluster cluster : clusters) {
            result += cluster.toPrintString() + "\n";
        }

        return result;
    }

}

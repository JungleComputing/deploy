package ibis.deploy.cli;

import ibis.util.TypedProperties;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ibis.deploy.Util;

/**
 * Single job in an experiment.
 * 
 * @author ndrost
 * 
 */
public class Job {

    // experiment this job belongs to
    private final Experiment parent;

    // name of job
    private String name;

    // application to run
    private String application;

    // arguments of application.
    private List<String> arguments;

    private int processCount;

    private String grid;

    private String cluster;

    private int resourceCount;

    private Boolean sharedHub;

    /**
     * Creates a new job with the given name. Jobs cannot be created directly,
     * but are constructed by a parent Experiment object.
     * 
     * @param name
     *            the name of the job
     * @throws Exception
     *             if the name given is <code>null</code>
     */

    Job(String name, Experiment parent) throws Exception {
        this.parent = parent;

        if (name == null) {
            throw new Exception("no name specified for job");
        }
        this.name = name;

        application = null;
        arguments = null;
        processCount = 0;
        grid = null;
        cluster = null;
        resourceCount = 0;
        sharedHub = null;
    }

    /**
     * Load job from the given properties (usually loaded from an experiment
     * file)
     * 
     * @param properties
     *            properties to load job from
     * @param object
     *            name of this job, or null to load "defaults" job
     * @throws Exception
     *             if job cannot be read properly
     */
    Job(TypedProperties properties, String name, Experiment parent)
            throws Exception {
        this.parent = parent;
        this.name = name;

        String prefix;
        if (name == null) {
            prefix = "";
        } else {
            prefix = name + ".";
        }

        application = properties.getProperty(prefix + "application");
        arguments = Util.getStringListProperty(properties, prefix + "application");

        processCount = properties.getIntProperty(prefix + "process.count");

        grid = properties.getProperty(prefix + "grid");
        cluster = properties.getProperty(prefix + "cluster");
        resourceCount = properties.getIntProperty(prefix + "resource.count");
        if(!properties.containsKey("shared.hub")) {
            sharedHub = null;
        } else {
            sharedHub = properties.getBooleanProperty(prefix + "shared.hub");
        }
    }

    public String getExperimentName() {
        if (parent == null) {
            return null;
        }
        return parent.getName();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getApplication() {
        if (application == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getApplication();
        }
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
    
    public String[] getArguments() {
        if (arguments == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getArguments();
        }
        return arguments.toArray(new String[0]);
    }

    public void setArguments(String[] arguments) {
        if (arguments == null) {
            this.arguments = null;
        } else {
            this.arguments = Arrays.asList(arguments.clone());
        }
    }

    public void addArgument(String argument) {
        if (arguments == null) {
            arguments = new ArrayList<String>();
        }
        arguments.add(argument);
    }

    /**
     * Total number of processes in this job
     * 
     * @return Total number of processes in this job. Returns 0 if unknown
     */
    public int getProcessCount() {
        if (processCount <= 0) {
            if (parent == null) {
                return 0;
            }
            return parent.getDefaults().getProcessCount();
        }
        return processCount;
    }

    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }
    
    public String getGrid() {
        if (grid == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getGrid();
        }
        return grid;
    }

    public void setGrid(String grid) {
        this.grid = grid;
    }
    
    public String getCluster() {
        if (cluster == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getCluster();
        }
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    /**
     * Total number of resources in this job
     * 
     * @return Total number of resources in this cluster. Returns 0 if unknown
     */
    public int getResourceCount() {
        if (resourceCount <= 0) {
            if (parent == null) {
                return 0;
            }
            return parent.getDefaults().getResourceCount();
        }
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    /**
     * Returns if this job uses a shared hub (default), or one started especially for it.
     * 
     * @return if this job uses a shared hub (default), or one started especially for it.
     */
    public boolean getSharedHub() {
        if (sharedHub == null) {
            if (parent == null) {
                return true;
            }
            return parent.getDefaults().getSharedHub();
        }
        return sharedHub;
    }

    public void setSharedHub(boolean sharedHub) {
        this.sharedHub = sharedHub;
    }
   
    /**
     * Print the settings of this job to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prependName
     *            if true, key/value lines prepended with the job name
     * @throws Exception
     *             if this job has no name
     */
    public void print(PrintWriter out, boolean prependName) throws Exception {
        String prefix;

        if (prependName) {
            if (name == null || name.length() == 0) {
                throw new Exception("cannot print job to file,"
                        + " name is not specified");
            }
            prefix = name + ".";
        } else {
            prefix = "";
        }

        if (application == null) {
            out.println("#" + prefix + "application =");
        } else {
            out.println(prefix + "application = " + application);
        }

        if (arguments == null) {
            out.println("#" + prefix + "arguments =");
        } else {
            out.println(prefix + "arguments = " + Util.strings2CSS(arguments));
        }

        if (processCount == 0) {
            out.println("#" + prefix + "process.count =");
        } else {
            out.println(prefix + "process.count = " + processCount);
        }

        if (grid == null) {
            out.println("#" + prefix + "grid =");
        } else {
            out.println(prefix + "grid = " + grid);
        }

        if (cluster == null) {
            out.println("#" + prefix + "cluster =");
        } else {
            out.println(prefix + "cluster = " + cluster);
        }

        if (resourceCount == 0) {
            out.println("#" + prefix + "resource.count =");
        } else {
            out.println(prefix + "resource.count = " + resourceCount);
        }
        
        if (sharedHub == null) {
            out.println("#" + prefix + "shared.hub =");
        } else {
            out.println(prefix + "shared.hub = " + sharedHub);
        }
        
    }

    public String toPrintString() {
        String result = "Job " + getName() + "\n";
        result += " Application = " + getApplication() + "\n";

        result += " Arguments = " + Util.strings2CSS(getArguments()) + "\n";
        result += " Process Count = " + getProcessCount() + "\n";
        result += " Grid = " + getGrid() + "\n";
        result += " Cluster = " + getCluster() + "\n";
        result += " Resource Count = " + getResourceCount() + "\n";
        result += " Shared Hub = " + getSharedHub() + "\n";

        return result;
    }

    public String toString() {
        return name + "@" + getExperimentName();
    }

}

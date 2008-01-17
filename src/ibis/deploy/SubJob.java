package ibis.deploy;

import java.util.HashMap;

public class SubJob {
    private String name, clusterName, gridName;

    private int nodes = 0, coresPerMachine = 0;
    
    private Application application;
    
    private HashMap<String, Object> attributes = new HashMap<String, Object>();

    public SubJob(String name, String grid, String cluster, int nodes,
            int multicore, Application application, String[] attrs) {
        this.name = name;
        this.gridName = grid;
        this.clusterName = cluster;
        this.nodes = nodes;
        this.coresPerMachine = multicore;
        this.application = application;
        if (attrs != null) {
            for (int i = 0; i < attrs.length; i+=2 ) {
                attributes.put(attrs[i], attrs[i+1]);
            }
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    public int getCoresPerMachine() {
        return coresPerMachine;
    }

    public int getMachineCount() {
        return nodes;
    }

    public String toString() {
        return "SubJob " + name + ": " + gridName + " " + clusterName + " "
                + nodes + " machines, with " + coresPerMachine
                + " cores/machine, for a total of " + (nodes * coresPerMachine)
                + " cores";
    }

    public String getGridName() {
        return gridName;
    }

    public String getName() {
        return name;
    }
    
    public Application getApplication() {
        return application;
    }
    
    public HashMap<String, Object> getAttributes() {
        return attributes;
    }
}

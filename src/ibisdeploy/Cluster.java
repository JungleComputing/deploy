/*
 * Created on Mar 6, 2006
 */
package ibisdeploy;

public class Cluster {
    private String friendlyName;

    private String hostname;

    private String accessType;

    private int machineCount;

    private int CPUsPerMachine;

    private String javaHome;

    private String fileAccessType;
    
    /**
     * @param accessType the resource manager to use
     * @param hostname the hostname to contact
     */
    public Cluster(String friendlyName, String hostname, String accessType, String fileAccessType,
            int machineCount, int CPUsPerMachine, String javaHome) {
        this.friendlyName = friendlyName;
        this.accessType = accessType;
        this.fileAccessType = fileAccessType;
        this.hostname = hostname;
        this.machineCount = machineCount;
        this.CPUsPerMachine = CPUsPerMachine;
        this.javaHome = javaHome;
    }

    public int getCPUsPerMachine() {
        return CPUsPerMachine;
    }

    public int getMachineCount() {
        return machineCount;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getHostname() {
        return hostname;
    }

    public String toString() {
        return "Cluster " + friendlyName + " contact = " + hostname + " with "
                + accessType + " machineCount = " + machineCount
                + " CPUs/machine = " + CPUsPerMachine;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @return the javaHome
     */
    public String getJavaHome() {
        return javaHome;
    }

	public String getFileAccessType() {
		return fileAccessType;
	}
}

package ibis.deploy.gui.deployViz.data;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import javax.swing.AbstractAction;

import ibis.deploy.gui.deployViz.DeployVizPanel;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class DataCollector {

    private ManagementServiceInterface manInterface;
    private RegistryServiceInterface regInterface;
    private int refreshrate = 3000;
    private Timer refreshTimer;
    private final DeployVizPanel vizPanel;

    private HashMap<String, Set<String>> ibisesPerSite = new HashMap<String, Set<String>>();
    private HashMap<String, HashMap<String, Long>> connectionsPerIbis = new HashMap<String, HashMap<String, Long>>();

    private static final AttributeDescription connections = new AttributeDescription(
            "ibis", "connections");
    private static final AttributeDescription sentBytesPerIbis = new AttributeDescription(
            "ibis", "sentBytesPerIbis");

    private static final AttributeDescription receivedBytesPerIbis = new AttributeDescription(
            "ibis", "receivedBytesPerIbis");

    private static final AttributeDescription load = new AttributeDescription(
            "java.lang:type=OperatingSystem", "SystemLoadAverage");

    public DataCollector(ManagementServiceInterface manInterface,
            RegistryServiceInterface regInterface, DeployVizPanel vPanel) {

        this.manInterface = manInterface;
        this.regInterface = regInterface;
        this.vizPanel = vPanel;

        AbstractAction updateVisualizationAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent arg0) {
                try {
                    collectIbisData();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

        };

        refreshTimer = new Timer(refreshrate, updateVisualizationAction);
        refreshTimer.start();
    }

    public void stopTimer() {
        refreshTimer.stop();
    }

    public void startTimer() {
        refreshTimer.start();
    }

    public boolean isTimerRunning() {
        return refreshTimer.isRunning();
    }

    @SuppressWarnings("unchecked")
    public void collectIbisData() {
        Map<String, Integer> poolSizes = null;
        IbisIdentifier[] ibises = null;
        String poolName, ibisLocation;
        String[] locationList;
        Set<String> ibisList;
        String ibisName, neighbourName, aux;
        try {
            poolSizes = regInterface.getPoolSizes();
        } catch (IOException e) {
            System.err.println("Couldn\'t retrieve pool sizes");
            e.printStackTrace();
        }

        // maybe at some later point do some better memory management,
        // reallocating these resources all the time might be costly
        ibisesPerSite.clear();
        connectionsPerIbis.clear();

        if (poolSizes != null) {

            for (Map.Entry<String, Integer> pool : poolSizes.entrySet()) {

                poolName = pool.getKey();

                // first we take into account the sites and the ibises
                try {

                    // retrieve the list of locations
                    locationList = regInterface.getLocations(poolName);

                    // retrieve the list of ibises
                    ibises = regInterface.getMembers(poolName);

                    // The site name is after the @ sign, we make sure this
                    // array only contains unique names
                    for (int i = 0; i < locationList.length; i++) {
                        locationList[i] = locationList[i].split("@")[1];

                        // if the location didn't previously exist in the list,
                        // add it
                        if (!ibisesPerSite.containsKey(locationList[i])) {
                            ibisesPerSite.put(locationList[i],
                                    new HashSet<String>());
                        }

                    }

                    // add all the ibises
                    for (IbisIdentifier ibis : ibises) {
                        ibisLocation = ibis.location().toString().split("@")[1];

                        ibisName = ibis.name() + "-" + ibis.location();

                        // check if that location exists, if not, create it
                        if (ibisesPerSite.get(ibisLocation) == null) {
                            ibisList = new HashSet<String>();
                        } else {
                            ibisList = ibisesPerSite.get(ibisLocation);
                        }

                        // add the ibis to the corresponding list, if it's not
                        // already there
                        ibisList.add(ibisName);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Object[] tempResult;
                HashMap<IbisIdentifier, Long> sentBytes, receivedBytes;
                HashMap<String, Long> totalBytes;
                long sum = 0;

                // retrieve the connections per ibis
                for (IbisIdentifier ibis : ibises) {

                    ibisName = ibis.name() + "-" + ibis.location();

                    // retrieve the connection hashmap for this ibis
                    if (connectionsPerIbis.get(ibisName) == null) {
                        totalBytes = new HashMap<String, Long>();
                        connectionsPerIbis.put(ibisName, totalBytes);
                    } else {
                        totalBytes = connectionsPerIbis.get(ibisName);
                        totalBytes.clear();
                    }

                    try {

                        // get the number of bytes sent to every connected ibis
                        tempResult = manInterface.getAttributes(ibis,
                                sentBytesPerIbis);
                        sentBytes = (HashMap<IbisIdentifier, Long>) tempResult[0];

                        // get the number of bytes received from every connected
                        // ibis
                        tempResult = manInterface.getAttributes(ibis,
                                receivedBytesPerIbis);
                        receivedBytes = (HashMap<IbisIdentifier, Long>) tempResult[0];

                        for (IbisIdentifier neighbour : sentBytes.keySet()) {
                            neighbourName = neighbour.name() + "-"
                                    + neighbour.location();

                            // the weight of the connection is the sum of the
                            // number of sent and received bytes, if both values
                            // are recorded
                            if (receivedBytes.get(neighbour) != null) {
                                sum = sentBytes.get(neighbour)
                                        + receivedBytes.get(neighbour);
                                receivedBytes.remove(neighbour);
                            } else {
                                sum = sentBytes.get(neighbour);
                            }

                            totalBytes.put(neighbourName, sum);
                        }

                        // if there are neighbours from which I received and did
                        // not send to
                        for (IbisIdentifier neighbour : receivedBytes.keySet()) {
                            neighbourName = neighbour.name() + "-"
                                    + neighbour.location();
                            totalBytes.put(neighbourName, receivedBytes
                                    .get(neighbour));
                        }

                    } catch (Exception e1) {
                        System.err.println(e1.getMessage());

                        // e1.printStackTrace();
                        // TODO This one happens pretty often, usually a
                        // socket-related exception. Printing the stack clogs up
                        // the output...
                    }

                }
            }
        }

        //generateRandomData();

        vizPanel.updateVisualization(ibisesPerSite, connectionsPerIbis);
    }

    private void generateRandomData() {
        int random;
        int sites = 10, nibises = 15;

        ibisesPerSite.clear();
        for (int i = 0; i < sites; i++) {
            random = (int) (Math.random() * 100);
            random = (int) (Math.random() * 100);
            if (random % 2 == 0) {
                String sitename = "site" + i;
                HashSet<String> ibises = new HashSet<String>();
                ibisesPerSite.put(sitename, ibises);
                for (int j = 0; j < nibises; j++) {

                    String startIbis = sitename + "ibis" + j;

                    HashMap<String, Long> tempedge = connectionsPerIbis
                            .get(startIbis);
                    if (tempedge == null) {
                        tempedge = new HashMap<String, Long>();
                    } else {
                        tempedge.clear();
                    }

                    ibises.add(startIbis);
                    for (int k = 0; k < nibises; k++) {
                        String stopIbis = sitename + "ibis" + k;
                        random = (int) (Math.random() * 100);
                        if (random % 2 == 0) {
                            if (k != j) {

                                tempedge.put(stopIbis,
                                        (long) (Math.random() * 100));
                                connectionsPerIbis.put(startIbis, tempedge);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean ibisListContainsName(IbisIdentifier[] ibisList, String name) {
        for (IbisIdentifier ibis : ibisList) {
            if (ibis.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}

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
    private int refreshrate = 5000;
    private Timer refreshTimer;
    private final DeployVizPanel vizPanel;

    private HashMap<String, Set<String>> ibisesPerSite = new HashMap<String, Set<String>>();

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

    public void collectIbisData() {
        Map<String, Integer> poolSizes;
        IbisIdentifier[] ibises = null;
        String poolName, ibisLocation;
        String[] locationList;
        Set<String> oldIbisLocations, dataToRemove = new HashSet<String>(), ibisList;

        try {
            poolSizes = regInterface.getPoolSizes();
            
            //for every pool
            for (Map.Entry<String, Integer> pool : poolSizes.entrySet()) {

                poolName = pool.getKey();

                try {
                    
                    //retrieve the list of locations
                    locationList = regInterface.getLocations(poolName);
                    
                    ibises = regInterface.getMembers(poolName);

                    // The site name is after the @ sign, we make sure this array
                    // only contains unique names
                    for (int i = 0; i < locationList.length; i++) {
                        locationList[i] = locationList[i].split("@")[1];
                        
                        //if the location didn't previously exist in the list, add it
                        if(!ibisesPerSite.containsKey(locationList[i])){
                            ibisesPerSite.put(locationList[i], new HashSet<String>());
                        }
                        
                        //remove the ibises which are no longer active from the site lists
                        ibisList = ibisesPerSite.get(locationList[i]);
                        for(String ibisName:ibisList){
                            if(!ibisListContainsName(ibises, ibisName)){
                                dataToRemove.add(ibisName);
                            }
                        }
                        for(String ibisName: dataToRemove){
                            ibisList.remove(ibisName);
                        }
                        dataToRemove.clear();
                    }

                    // remove the locations that aren't up to date any more
                    oldIbisLocations = ibisesPerSite.keySet();
                    for (String location : oldIbisLocations) {
                        // check if the current location is still in the list
                        if (Arrays.binarySearch(locationList, 0,
                                locationList.length, location) < 0) {
                            //mark the location for removal
                            dataToRemove.add(location);
                        }
                    }
//                    //***************
//                    Set<String> tmp = new HashSet<String>();
//                    ibisesPerSite.put("X", tmp);
//                    tmp.add("a");
//                    tmp.add("dsasda");
//                    
//                    tmp = new HashSet<String>();
//                    ibisesPerSite.put("Y", tmp);
//                    tmp.add("aaa");
//                    tmp.add("dsasda");
//                    
//                    //*****************
                    
                    //remove all the locations that are no longer up to date
                    for(String location:dataToRemove){
                        ibisesPerSite.remove(location);
                    }
                    dataToRemove.clear();
                    
                    //add all the ibises that aren't up to date anymore
                    for (IbisIdentifier ibis : ibises) {
                        ibisLocation = ibis.location().toString().split("@")[1];
                        
                        //check if that location exists, if not, create it
                        if (ibisesPerSite.get(ibisLocation) == null) {
                            ibisList = new HashSet<String>();
                        } else {
                            ibisList = ibisesPerSite.get(ibisLocation);
                        }

                        //add the ibis to the corresponding list, if it's not already there
                        if (!ibisList.contains(ibis.name())) {
                            ibisList.add(ibis.name());
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                vizPanel.updateVisualization(ibisesPerSite);

                //
                // AttributeDescription connections = new
                // AttributeDescription("ibis",
                // "connections");
                //
                // for (IbisIdentifier ibis : ibises) {
                //
                // try {
                // // System.out.println(ibis
                // // + " connected to = "
                // // + Arrays.toString((IbisIdentifier[]) manInterface
                // // .getAttributes(ibis, connections)[0]));
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // }

                // AttributeDescription load = new AttributeDescription(
                // "java.lang:type=OperatingSystem", "SystemLoadAverage");
                //
                // AttributeDescription cpu = new AttributeDescription(
                // "java.lang:type=OperatingSystem", "ProcessCpuTime");
                //
                // AttributeDescription connections = new
                // AttributeDescription("ibis",
                // "connections");
                //
                // // for each ibis, print these attributes
                // if (ibises != null) {
                // for (IbisIdentifier ibis : ibises) {
                // try {
                // System.err
                // .println(ibis
                // + " connected to = "
                // + Arrays
                // .toString((IbisIdentifier[]) manInterface
                // .getAttributes(ibis,
                // connections)[0]));
                //
                // } catch (Exception e) {
                // System.err.println("Could not get management info: ");
                // e.printStackTrace();
                // }
                // }
                //
                // }
            }
        } catch (Exception e) {
            System.err.println("Couldn\'t retrieve pool sizes");
            e.printStackTrace();
        }

    }
    
    private boolean ibisListContainsName(IbisIdentifier[] ibisList, String name){
        for(IbisIdentifier ibis: ibisList){
            if(ibis.name().equals(name)){
                return true;
            }
        }
        return false;
    }
}

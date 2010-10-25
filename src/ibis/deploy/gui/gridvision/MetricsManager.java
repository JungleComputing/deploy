package ibis.deploy.gui.gridvision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.metrics.link.*;
import ibis.deploy.gui.gridvision.metrics.node.*;
import ibis.deploy.gui.gridvision.metrics.special.*;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class MetricsManager implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.gui.gridvision.MetricsManager");
	
	//Variables needed for the operation of this class
	private RegistryServiceInterface regInterface;
	private ManagementServiceInterface manInterface;

	private Map<String, Integer> poolSizes;	
	private Flock universe;
	private Map<IbisIdentifier, Flock> iiToFlock;
	
	private int refreshrate;
	private boolean reinitializeNeeded = false;
	
	//The list that holds the statistics necessary for initializing the visualization 
	private MetricsList initStatistics;
		
	public MetricsManager(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {
		this.regInterface = regInterface;
		this.manInterface = manInterface;
		this.refreshrate = 500;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		Location universeLocation = new ibis.ipl.impl.Location(new String[0]);
		universe = new ibis.deploy.gui.gridvision.impl.Flock(this, null, universeLocation, manInterface, regInterface, initStatistics);
		iiToFlock = new HashMap<IbisIdentifier, Flock>();
		
		//List that holds the initial statistics necessary to create the data structure (links and coordinates)
		initStatistics = new MetricsList();
		//initStatistics.add(new XcoordStatistic());
		//initStatistics.add(new YcoordStatistic());
		//initStatistics.add(new ZcoordStatistic());
		initStatistics.add(new ConnMetric());
		initStatistics.add(new CPUMetric());
		//initStatistics.add(new BytesSentMetric());
		initStatistics.add(new HeapMemMetric());
		initStatistics.add(new NonHeapMemMetric());
		initStatistics.add(new ThreadsMetric());
		initStatistics.add(new BytesSentPerIbisMetric());
		initStatistics.add(new BytesReceivedPerIbisMetric());	
	}
	
	public void update() {	
		//long start = System.currentTimeMillis();
		//String init = ""; 
		
		//Update the size of all pools and sites
		initUniverse();
		
		try {
			universe.update();
		} catch (NoSuitableModuleException e) {
			logger.debug("MetricsManager: NoSuitableModuleException while updating universe.");			
		} catch (StatNotRequestedException e) {
			logger.debug("MetricsManager: StatNotRequestedException while updating universe.");
		}
		
		//long end = System.currentTimeMillis();
        //System.err.println("update cycle"+init+" took " + (end - start) + " ms");
	}
			
	private void initUniverse() {
		boolean change = false;
		Map<String, Integer> newSizes = new HashMap<String, Integer>();
		
		try {		
			newSizes = regInterface.getPoolSizes();
		} catch (Exception e) {	
			logger.debug("MetricsManager: Exception when getting poolSizes.");		
		}	
		
		//Determine whether a change in the hierarchy has taken place
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
			String poolName = entry.getKey();
	        int newSize = entry.getValue();

	        if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {	         	
	         	change = true;
	         	reinitializeNeeded = true;
	        }	        			
		}
		
		poolSizes = newSizes;
		
				
		if (change) {				
			//If there was a change, make a new list of all the IbisIdentifiers, so we can 
			//see which change has taken place
			ArrayList<IbisIdentifier> newIbises = new ArrayList<IbisIdentifier>();
			
			try {
				String[] poolNames = regInterface.getPools();
				for (String poolName : poolNames) {
					try {
						IbisIdentifier[] members = regInterface.getMembers(poolName);
						for (IbisIdentifier newLeaf : members) {
							newIbises.add(newLeaf);
						}
					} catch (IOException e) {
						logger.debug("MetricsManager: Exception when getting poolMembers from "+poolName+".");
					}
				}
			} catch (IOException e) {
				logger.debug("MetricsManager: Exception when getting pools");
			}
			
			//Now, compare the previous map of IbisIdentifiers to the new map, so we spot the newcomers,
			//and/or the dropouts. 
			
			//add the newcomers			
			for (IbisIdentifier ibis : newIbises) {
				if (!iiToFlock.containsKey(ibis)) {
					iiToFlock.put(ibis, addLeaf(ibis));					
				}
			}
			
			//remove dropouts
			for (Map.Entry<IbisIdentifier, Flock> entry : iiToFlock.entrySet()) {
				IbisIdentifier ibis = entry.getKey();
				if (!newIbises.contains(ibis)) {
					iiToFlock.remove(removeLeaf(ibis));					
				}
			}
		}		
	}	
	
	public Flock addLeaf(IbisIdentifier ii) {		
		Location ibisLocation = ii.location();
		
		
		//TODO FIX
		return null;
	}
	
	public Flock removeLeaf(IbisIdentifier ii) {
		Flock flockToRemove = iiToFlock.get(ii);
		flockToRemove.getParent().removeChild(flockToRemove);
		return flockToRemove;
	}

	public Flock getUniverse() {
		reinitializeNeeded = false;
		return universe;		
	}
	
	public boolean isReinitializeNeeded() {
		return reinitializeNeeded;
	}
	
	public void run() {
		while (true) {
			update();
			try {
				Thread.sleep(refreshrate);
			} catch (InterruptedException e) {				
				break;
			}
		}
	}
	
	public void setRefreshrate(int newRate) {
		this.refreshrate = newRate;
	}
	
	public void registerIbis(IbisIdentifier ii, Flock ic) {
		iiToFlock.put(ii, ic);
	}
	
	public Flock getFlock(IbisIdentifier ii) {
		return iiToFlock.get(ii);
	}
}
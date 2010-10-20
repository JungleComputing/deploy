package ibis.deploy.gui.gridvision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.gui.gridvision.dataholders.*;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.metrics.link.*;
import ibis.deploy.gui.gridvision.metrics.node.*;
import ibis.deploy.gui.gridvision.metrics.special.*;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class MetricsManager implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.gui.gridvision.MetricsManager");
	
	//Variables needed for the operation of this class		
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	
	private Map<String, Integer> poolSizes;	
	private HashMap<String, IbisConcept> pools;
	
	private int refreshrate;
	private boolean reinitializeNeeded = false;
	
	//The list that holds the statistics necessary for initializing the visualization 
	private MetricsList initStatistics;
	
	private HashMap<IbisIdentifier, IbisConcept> iiToIbisConcept;
		
	public MetricsManager(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		this.refreshrate = 500;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		pools = new HashMap<String, IbisConcept>();
		
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
		
		iiToIbisConcept = new HashMap<IbisIdentifier, IbisConcept>();		
	}
	
	public void update() {	
		//long start = System.currentTimeMillis();
		//String init = ""; 
		
		//Update the size of all pools and sites
		ArrayList<Pool> newPools = initPools();
				
		//for all pools
		for (Pool pool : newPools) {
			try {
				pool.update();
			} catch (NoSuitableModuleException e) {
				e.printStackTrace();
				
			} catch (StatNotRequestedException e) {
				pools.clear();
				poolSizes.clear();
				initPools();
				//init = " with initialization";				
			}
		}
		
		//long end = System.currentTimeMillis();
        //System.err.println("update cycle"+init+" took " + (end - start) + " ms");
	}
			
	private void initDataGathering() {
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
	        }	        			
		}
				
		if (change) {
			//If there was a change, make a new map of all the IbisIdentifiers, so we can 
			//later see where the change has taken place
			Map<IbisIdentifier, String> leavesToPools = new HashMap<IbisIdentifier, String>();
			
			//TODO globalize, remove here
			Map<IbisIdentifier, String> old_leavesToPools = new HashMap<IbisIdentifier, String>();
			
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
				String poolName = entry.getKey();
				try {
					IbisIdentifier[] newLeaves = regInterface.getMembers(poolName);
					for (IbisIdentifier ii : newLeaves) {
						leavesToPools.put(ii, poolName);
					}
				} catch (IOException e) {
					logger.debug("MetricsManager: Exception when getting poolMembers from "+poolName+".");
				}
			}

			//Now, compare the previous map of IbisIdentifiers to the new map, so we spot the newcomers,
			//the migrants between pools and/or the dropouts. 
			ArrayList<IbisIdentifier> additions = new ArrayList<IbisIdentifier>();
			ArrayList<IbisIdentifier> migrants  = new ArrayList<IbisIdentifier>();
			ArrayList<IbisIdentifier> dropouts  = new ArrayList<IbisIdentifier>();
			
			for (Map.Entry<IbisIdentifier, String> newMapEntry : leavesToPools.entrySet()) {
				IbisIdentifier newIbis = newMapEntry.getKey();
				String newPool = newMapEntry.getValue();
				
				if (!old_leavesToPools.containsKey(newIbis)) {
					additions.add(newIbis);
				} else {
					if (old_leavesToPools.get(newIbis) != newPool) {
						migrants.add(newIbis);
					} 
				}					
			}
			
			for (Map.Entry<IbisIdentifier, String> oldMapEntry : old_leavesToPools.entrySet()) {
				IbisIdentifier oldIbis = oldMapEntry.getKey();
				String oldPool = oldMapEntry.getValue();
				
				if (!leavesToPools.containsKey(oldIbis)) {
					dropouts.add(oldIbis);
				}				
			}
			
			//Add the additions to the appropriate trees
			for (IbisIdentifier addedIbis : additions) {
				String poolName = leavesToPools.get(addedIbis);
				IbisConcept ic = registerLeaf(addedIbis);
				if (pools.containsKey(poolName)) {
					pools.get(poolName).addLeaf(ic);
				} else {
					Pool newPool = new Pool(this, manInterface, regInterface, initStatistics, poolName)
					newPool.addLeaf(ic);
					pools.put(poolName, newPool);
					
				}
			}
			//Move the migrants to their new homes
			for (IbisIdentifier migratingIbis : migrants) {
				IbisConcept ic = iiToIbisConcept.get(migratingIbis);
				String oldPoolName = old_leavesToPools.get(migratingIbis);
				String newPoolName = leavesToPools.get(migratingIbis);
				pools.get(oldPoolName).removeLeaf(ic);
				if (pools.get(oldPoolName).isEmpty()) {
					pools.remove(oldPoolName);
				}
				if (pools.containsKey(newPoolName)) {
					pools.get(newPoolName).addLeaf(ic);
				} else {
					Pool newPool = new Pool(this, manInterface, regInterface, initStatistics, newPoolName)
					newPool.addLeaf(ic);
					pools.put(newPoolName, newPool);
				}
			}
			//Remove the dropouts from the appropriate trees
		}
		
		
		
		
		
		
		
		/*
		boolean reinitializeSetter = false;
		Map<String, Integer> newSizes = new HashMap<String, Integer>();
		
		try {		
			newSizes = regInterface.getPoolSizes();
		} catch (Exception e) {	
			e.printStackTrace();
		}
	
		//clear the pools list, if warranted
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
			String poolName = entry.getKey();
	        int newSize = entry.getValue();

	        if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
	         	pools.clear();
	         	iiToIbisConcept.clear();
	         	reinitializeSetter = true;	         	
	        }	        			
		}	            
				
		//reinitialize the pools list
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
			String poolName = entry.getKey();
	        int newSize = entry.getValue();

	        if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
	        	if (newSize > 0) {		            
		          	pools.add(new Pool(this, manInterface, regInterface, initStatistics.clone(), poolName));		            	
		        }		        	            
	        }
		}
		
		if (reinitializeSetter) {
			reinitializeNeeded = true;
		}
		
		poolSizes = newSizes;
		
		return pools;
		*/
	}	

	public List<Pool> getTopConcepts() {
		reinitializeNeeded = false;
		return pools;		
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
	
	public void registerIbis(IbisIdentifier ii, IbisConcept ic) {
		iiToIbisConcept.put(ii, ic);
	}
	
	public IbisConcept getConcept(IbisIdentifier ii) {
		return iiToIbisConcept.get(ii);
	}
}
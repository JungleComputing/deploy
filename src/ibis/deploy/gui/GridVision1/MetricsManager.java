package ibis.deploy.gui.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.deploy.gui.performance.dataholders.*;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.link.*;
import ibis.deploy.gui.performance.metrics.node.*;
import ibis.deploy.gui.performance.metrics.special.*;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class StatsManager implements Runnable {
	//Variables needed for the operation of this class		
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	
	private Map<String, Integer> poolSizes;	
	private ArrayList<Pool> pools;
	
	private int refreshrate;
	private boolean reinitializeNeeded = false;
	
	//The list that holds the statistics necessary for initializing the visualization 
	private MetricsList initStatistics;
	
	public StatsManager(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		this.refreshrate = 500;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		pools = new ArrayList<Pool>();
		
		//List that holds the initial statistics necessary to create the data structure (links and coordinates)
		initStatistics = new MetricsList();
		//initStatistics.add(new XcoordStatistic());
		//initStatistics.add(new YcoordStatistic());
		//initStatistics.add(new ZcoordStatistic());
		initStatistics.add(new ConnStatistic());
		initStatistics.add(new CPUStatistic());
		//initStatistics.add(new BytesSentMetric());
		initStatistics.add(new HeapMemStatistic());
		initStatistics.add(new NonHeapMemStatistic());
		initStatistics.add(new ThreadsStatistic());
		initStatistics.add(new BytesSentPerIbisMetric());
		initStatistics.add(new BytesReceivedPerIbisMetric());
	}
	
	public void update() {	
		//long start = System.currentTimeMillis();
		String init = ""; 
		
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
				init = " with initialization";				
			}
		}
		
		//long end = System.currentTimeMillis();
        //System.err.println("update cycle"+init+" took " + (end - start) + " ms");
	}
			
	private ArrayList<Pool> initPools() {
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
	         	reinitializeSetter = true;	         	
	        }	        			
		}	            
				
		//reinitialize the pools list
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
			String poolName = entry.getKey();
	        int newSize = entry.getValue();

	        if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
	        	if (newSize > 0) {		            
		          	pools.add(new Pool(manInterface, regInterface, initStatistics.clone(), poolName));		            	
		        }		        	            
	        }
		}
		
		if (reinitializeSetter) {
			reinitializeNeeded = true;
		}
		
		poolSizes = newSizes;
		
		return pools;
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
}
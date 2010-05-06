package ibis.deploy.gui.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.deploy.gui.performance.dataholders.*;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.*;
import ibis.deploy.gui.performance.metrics.link.*;
import ibis.deploy.gui.performance.metrics.node.*;
import ibis.deploy.gui.performance.metrics.special.*;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class StatsManager {
	//Variables needed for the operation of this class		
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	private VisualManager visman;
	
	private Map<String, Integer> poolSizes;	
	private ArrayList<Pool> pools;
	
	//The list that holds the statistics necessary for initializing the visualization 
	private ArrayList<MetricsObject> initStatistics;
	
	//The lists that hold the currently available metrics, add to this when implementing new stats.
	private List<MetricsObject> availableSpecialMetrics;
	private List<NodeMetricsObject> availableNodeMetrics;
	private List<LinkMetricsObject> availableLinkMetrics;
	
	public StatsManager(VisualManager visman, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {		
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		this.visman = visman;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		pools = new ArrayList<Pool>();
		
		//List that holds the initial statistics necessary to create the data structure (links and coordinates)
		initStatistics = new ArrayList<MetricsObject>();
		//initStatistics.add(new XcoordStatistic());
		//initStatistics.add(new YcoordStatistic());
		//initStatistics.add(new ZcoordStatistic());
		initStatistics.add(new ConnStatistic());
		initStatistics.add(new CPUStatistic());
		//initStatistics.add(new BytesSentStatistic());		
		initStatistics.add(new HeapMemStatistic());
		initStatistics.add(new NonHeapMemStatistic());
		
		//List that holds all available special statistics
		availableSpecialMetrics = new ArrayList<MetricsObject>();
		availableSpecialMetrics.add(new ConnStatistic());
		
		//List that holds all available Node-based statistics
		availableNodeMetrics = new ArrayList<NodeMetricsObject>();
		availableNodeMetrics.add(new CPUStatistic());
		availableNodeMetrics.add(new HeapMemStatistic());
		availableNodeMetrics.add(new NonHeapMemStatistic());		
		availableNodeMetrics.add(new XcoordStatistic());
		availableNodeMetrics.add(new YcoordStatistic());
		availableNodeMetrics.add(new ZcoordStatistic());
			
		//List that holds all available Link-based statistics
		availableLinkMetrics = new ArrayList<LinkMetricsObject>();
		availableLinkMetrics.add(new BytesSentStatistic());
	}
	
	public void update() {		
		//Update the size of all pools and sites
		ArrayList<Pool> newPools = initPools();
				
		//for all pools
		for (Pool pool : newPools) {
			try {
				pool.update();
			} catch (StatNotRequestedException e) {				
				e.printStackTrace();
			}
		}
	}
			
	private ArrayList<Pool> initPools() {
		boolean needReinitializationOfVisuals = false;
		
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
	         	needReinitializationOfVisuals = true;
	        }	        			
		}	            
				
		//reinitialize the pools list
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
			String poolName = entry.getKey();
	        int newSize = entry.getValue();

	        if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
	        	if (newSize > 0) {		            
		          	pools.add(new Pool(manInterface, regInterface, initStatistics, poolName));		            	
		        }		        	            
	        }
		}
		
		if (needReinitializationOfVisuals) {
			visman.reinitialize(pools);
		}
		
		poolSizes = newSizes;
		
		return pools;
	}	

	public List<Pool> getTopConcepts() {
		return pools;
	}

	public List<NodeMetricsObject> getAvailableNodeMetrics() {
		return availableNodeMetrics;
	}
	
	public List<LinkMetricsObject> getAvailableLinkMetrics() {
		return availableLinkMetrics;
	}		
	
}
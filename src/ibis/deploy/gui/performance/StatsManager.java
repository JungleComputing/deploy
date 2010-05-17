package ibis.deploy.gui.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.deploy.gui.performance.dataholders.*;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.Metric;
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
	private MetricsList initStatistics;
	
	//The lists that hold the currently available metrics, add to this when implementing new stats.
	//private MetricsList availableSpecialMetrics;
	//private ArrayList<String> availableNodeMetrics;
	//private ArrayList<String> availableLinkMetrics;
	
	public StatsManager(VisualManager visman, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {		
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		this.visman = visman;
		
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
		initStatistics.add(new BytesSentStatistic());
		initStatistics.add(new HeapMemStatistic());
		initStatistics.add(new NonHeapMemStatistic());
		
		//List that holds all available special statistics
		//availableSpecialMetrics = new MetricsList();
		//availableSpecialMetrics.add(new ConnStatistic());
		
		//List that holds all available Node-based statistics		
		//availableNodeMetrics = new ArrayList<String>();
		//for (Metric metric : initStatistics) {
		//	if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
		//		availableNodeMetrics.add(metric.getName());
		//	}
		//}
			
		//List that holds all available Link-based statistics
		//availableLinkMetrics = new ArrayList<String>();
		//for (Metric metric : initStatistics) {
		//	if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {
		//		availableLinkMetrics.add(metric.getName());
		//	}
		//}
	}
	
	public void update() {		
		//Update the size of all pools and sites
		ArrayList<Pool> newPools = initPools();
				
		//for all pools
		for (Pool pool : newPools) {
			try {
				pool.update();
			} catch (Exception e) {
				e.printStackTrace();
				pools.clear();
				poolSizes.clear();
				initPools();
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
		          	pools.add(new Pool(manInterface, regInterface, initStatistics.clone(), poolName));		            	
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

	//public ArrayList<String> getAvailableNodeMetrics() {
	//	return availableNodeMetrics;
	//}
	
	//public ArrayList<String> getAvailableLinkMetrics() {
	//	return availableLinkMetrics;
	//}		
	
}
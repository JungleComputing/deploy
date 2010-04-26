package ibis.deploy.gui.performance;

import java.io.IOException;
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
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class StatsManager {
	//Variables needed for the operation of this class		
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	private Map<String, Integer> poolSizes;	
	private HashMap<IbisIdentifier, Node> ibisesToNodes;
	private List<IbisConcept> pools;	
	
	//The list that holds the statistics necessary for initializing the visualization 
	private ArrayList<MetricsObject> initStatistics;
	
	//The lists that hold the currently available metrics, add to this when implementing new stats.
	private List<MetricsObject> availableSpecialMetrics;
	private List<NodeMetricsObject> availableNodeMetrics;
	private List<LinkMetricsObject> availableLinkMetrics;
	
	public StatsManager(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {		
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		pools = new ArrayList<IbisConcept>();
		
		//Maps used for convenience's sake		
		ibisesToNodes = new HashMap<IbisIdentifier, Node>();
		
		//List that holds the initial statistics necessary to create the data structure (links and coordinates)
		initStatistics = new ArrayList<MetricsObject>();
		initStatistics.add(new XcoordStatistic());
		initStatistics.add(new YcoordStatistic());
		initStatistics.add(new ZcoordStatistic());
		initStatistics.add(new ConnStatistic());
		
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
	
	
	@SuppressWarnings("unchecked")
	public void update() {		
		//Update the size of all pools and sites
		if (checkPools()) {
			initPools();
		}
		
		//for all pools
		for (IbisConcept pool : pools) {
			for (Site site : (Site[])pool.getSubConcepts()) {
				//check which statistics we are interested in
				ArrayList<MetricsObject> currentSiteInterest = site.getCurrentlyGatheredStatistics();						
								
				//all ibises in this site, update
				for (Node node : (Node[])site.getSubConcepts()) {		
					node.update((ArrayList<MetricsObject>) currentSiteInterest.clone());				
				}
				
				try {
					site.update();
				} catch (StatNotRequestedException e) {
					e.printStackTrace();
				}
			}
			try {
				((Pool)pool).update();
			} catch (StatNotRequestedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkPools() {
		boolean changed = false;
		Map<String, Integer> newSizes;
		try {
			newSizes = regInterface.getPoolSizes();		
		
			//Check if anything has changed since the last update
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
				String poolName = entry.getKey();
	            int newSize = entry.getValue();
				if (newSize > 0) {
		            if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {		            	
		            	changed = true;
		            }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return changed;
	}
	
	private void initPools() {
		pools.clear();
		Map<String, Integer> newSizes;		
		try {		
			newSizes = regInterface.getPoolSizes();
			//reinitialize the pools list
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
	            String poolName = entry.getKey();
	            int newSize = entry.getValue();

	            if (newSize > 0) {
		            if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {		            	
		            	//Create and populate sites
		            	List<Site> sites = initSites(poolName);
		            	
		            	Site[] siteHolder = new Site[sites.size()];
		    			sites.toArray(siteHolder);
		            	pools.add(new Pool(manInterface, poolName, siteHolder));
		            }
	            }
	        }
			poolSizes = newSizes;
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
	
	private List<Site> initSites(String poolName) throws IOException {		
		List<Site> sites = new ArrayList<Site>();
				
		//Get the members of this pool
		IbisIdentifier[] ibises = regInterface.getMembers(poolName);
						
		//Initialize the list of sites
		List<String> siteNames = new ArrayList<String>();
		String[] locationsPerIbis = {};
		try {
			locationsPerIbis = regInterface.getLocations(poolName);
			
			//The site name is after the @ sign, we make this array only contain unique names
			for (int i=0; i<locationsPerIbis.length; i++) {
				locationsPerIbis[i] = locationsPerIbis[i].split("@")[1];
				siteNames.add(locationsPerIbis[i]);
			}			
		} catch (IOException e) {					
			e.printStackTrace();
		}
						
		//For all sites			
		for (String siteName : siteNames) {
			String ibisLocationName;
			List<Node> nodes = new ArrayList<Node>();						
			
			//Determine which ibises belong to this site
			for (int i=0; i<ibises.length; i++) {
				ibisLocationName = ibises[i].location().toString().split("@")[1];
				
				//And compare all ibises' locations to that sitename
				if (ibisLocationName.compareTo(siteName) == 0) {
					Node node = new Node(manInterface, siteName, ibises[i]);
					nodes.add(node);
					ibisesToNodes.put(ibises[i], node);	
					
					//Update this ibis's stats
					node.update(initStatistics);
				}
			}
			Node [] nodeHolder = new Node[nodes.size()];
			nodes.toArray(nodeHolder);
			sites.add(new Site(manInterface, siteName, nodeHolder));
		}
		
		return sites;
	}

	public List<IbisConcept> getTopConcepts() {
		return pools;
	}

	public List<NodeMetricsObject> getAvailableNodeMetrics() {
		return availableNodeMetrics;
	}
	
	public List<LinkMetricsObject> getAvailableLinkMetrics() {
		return availableLinkMetrics;
	}		
	
}
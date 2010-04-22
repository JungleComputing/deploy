package ibis.deploy.gui.performance.newtry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.metrics.*;
import ibis.deploy.gui.performance.newtry.metrics.node.*;
import ibis.deploy.gui.performance.newtry.metrics.link.*;
import ibis.deploy.gui.performance.newtry.metrics.special.*;
import ibis.deploy.gui.performance.newtry.dataobjects.*;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.support.management.AttributeDescription;

public class StatsManager {
	//Variables needed for the operation of this class		
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	private Map<String, Integer> poolSizes;	
	private HashMap<IbisIdentifier, Node> ibisesToNodes;
	private List<IbisConcept> pools;	
	
	//The list that holds the statistics necessary for initializing the visualization 
	private List<MetricsObject> initStatistics;
	
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
				List<MetricsObject> currentSiteInterest = site.getCurrentlyGatheredStatistics();						
				
				//Make a map of those statistics' necessary attributes and their indexes
				HashMap<MetricsObject, Integer> attributeIndexes = MakeAttributeIndexesMap(currentSiteInterest);
				
				//all ibises in this site, update
				for (Node node : (Node[])site.getSubConcepts()) {		
					node.update((HashMap<MetricsObject, Integer>) attributeIndexes.clone());				
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
		            	
		            	/*
		            	//Create trunks
		            	List<Trunk> trunks = new ArrayList<Trunk>();
		            			            	
		            	for (Site site : sites) {
		            		for (Link link : site.getLinks()) {
		            			if (link.getTo().getSiteName() != site.getName()) {
		            				//we've found an external link for this site, create a trunk to the destination site
		            				for (Site siteTo : sites) {
		            					String siteNameTo = link.getTo().getSiteName();
		            					if (siteTo.getName().compareTo(siteNameTo) == 0) {
		            						trunks.add(new Trunk(perfvis, site, siteTo));		            						
		            					}
		            				}		            				
		            			}
		            		}
		            	}
		            	*/
		            	pools.add(new Pool(manInterface, poolName, (Site[])sites.toArray()));
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
		
		//A map of all the initial statistics, we use this to initialize the visualization, 
		//by calling update with it at least once
		HashMap<MetricsObject, Integer> initialAttributesMap = MakeAttributeIndexesMap(initStatistics);
		
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
					node.update(initialAttributesMap);
				}
			}
			sites.add(new Site(manInterface, siteName, (Node[])nodes.toArray()));
		}
		
		/*
		//For each site, check for and (if available) create this site's links
		for (Site site : sites) {
			List<Link> links = new ArrayList<Link>();
			for (Node node : site.getNodes()) {					
				IbisIdentifier[] destinations;
				destinations = (IbisIdentifier[]) node.getConnectedIbises();
				
				for (int j=0; j<destinations.length; j++) {
					links.add(new Link(perfvis, node, ibisesToNodes.get(destinations[j])));
				}
			}
			site.setLinks((Link[]) links.toArray());
		}	
		*/	
		
		return sites;
	}
		
	public HashMap<MetricsObject, Integer> MakeAttributeIndexesMap(List<MetricsObject> interest) {			
		//calculate the size of the AttributeDescription array
		int attributesCount = 0;
		MetricsObject[] currentInterest = (MetricsObject[]) interest.toArray();
		for (int i=0; i<currentInterest.length; i++) {
			attributesCount += currentInterest[i].getAttributesCountNeeded();
		}
		
		AttributeDescription[] descriptions = new AttributeDescription[attributesCount];
		HashMap<MetricsObject, Integer> attributeIndexes = new HashMap<MetricsObject, Integer>();
		
		//fill the array with the necessary attributes and remember their starting indexes
		attributesCount = 0;
		for (int i=0; i<currentInterest.length; i++) {
			attributeIndexes.put(currentInterest[i], attributesCount);
			for (int j=0; j<currentInterest[i].getAttributesCountNeeded(); j++) {
				descriptions[attributesCount+j] = currentInterest[i].getNecessaryAttributes()[j];
			}
			
			attributesCount += currentInterest[i].getAttributesCountNeeded();
		}
		return attributeIndexes;
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
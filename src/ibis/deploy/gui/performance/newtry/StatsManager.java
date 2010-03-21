package ibis.deploy.gui.performance.newtry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.stats.*;
import ibis.deploy.gui.performance.newtry.concepts.*;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class StatsManager {
	//Variables needed for the operation of this class
	private PerfVis perfvis;
	private Map<String, Integer> poolSizes;	
	private HashMap<IbisIdentifier, Node> ibisesToNodes;
	private List<Pool> pools;	
	
	//The list that holds the statistics necessary for initializing the visualization 
	private List<StatisticsObject> initStatistics;
	
	//The list that holds the currently available statistics, add to this when implementing new stats.
	private List<StatisticsObject> availableStatistics;
	
	public StatsManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		pools = new ArrayList<Pool>();
		
		//Maps used for convenience's sake		
		ibisesToNodes = new HashMap<IbisIdentifier, Node>();
		
		//List that holds the initial statistics necessary to create the data structure (links and coordinates)
		initStatistics = new ArrayList<StatisticsObject>();
		initStatistics.add(new XcoordStatistic());
		initStatistics.add(new YcoordStatistic());
		initStatistics.add(new ZcoordStatistic());
		initStatistics.add(new ConnStatistic());
		
		//List that holds all available statistics
		availableStatistics = new ArrayList<StatisticsObject>();
		availableStatistics.add(new CPUStatistic());
		availableStatistics.add(new HeapMemStatistic());
		availableStatistics.add(new NonHeapMemStatistic());
		availableStatistics.add(new ConnStatistic());
		availableStatistics.add(new XcoordStatistic());
		availableStatistics.add(new YcoordStatistic());
		availableStatistics.add(new ZcoordStatistic());
		availableStatistics.add(new LinksStatistic());	
	}
	
	@SuppressWarnings("unchecked")
	public void update() {		
		//Update the size of all pools and sites
		initPools();
		
		//for all pools
		for (Pool pool : pools) {
			for (Site site : pool.getSites()) {
				//check which statistics we are interested in
				List<StatisticsObject> currentSiteInterest = site.getCurrentlyGatheredStatistics();						
				
				//Make a map of those statistics' necessary attributes and their indexes
				HashMap<StatisticsObject, Integer> attributeIndexes = MakeAttributeIndexesMap(currentSiteInterest);
				
				//all ibises in this site, update
				for (Node node : site.getNodes()) {		
					node.update((HashMap<StatisticsObject, Integer>) attributeIndexes.clone());				
				}
				
				try {
					site.updateAverages();
				} catch (StatNotRequestedException e) {
					e.printStackTrace();
				}
			}
			try {
				pool.updateAverages();
			} catch (StatNotRequestedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initPools() {
		try {
			Map<String, Integer> newSizes = perfvis.getRegInterface().getPoolSizes();
						
			//Check if anything has changed since the last update, if so, clear the pools list.
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
				String poolName = entry.getKey();
	            int newSize = entry.getValue();
				if (newSize > 0) {
		            if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
		            	pools.clear();
		            }
				}
			}
			
			//reinitialize the pools list
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
	            String poolName = entry.getKey();
	            int newSize = entry.getValue();

	            if (newSize > 0) {
		            if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {		            	
		            	//Create and populate sites
		            	List<Site> sites = initSites(poolName);
		            	
		            	//Create trunks
		            	List<Trunk> trunks = new ArrayList<Trunk>();
		            			            	
		            	for (Site site : sites) {
		            		for (Link link : site.getLinks()) {
		            			if (link.getTo().getSiteName() != site.getName()) {
		            				//we've found an external link for this site, create a trunk to the destination site
		            				for (Site siteTo : sites) {
		            					String siteNameTo = link.getTo().getSiteName();
		            					if (siteTo.getName().compareTo(siteNameTo) == 0) {
		            						trunks.add(new Trunk(site, siteTo));		            						
		            					}
		            				}		            				
		            			}
		            		}
		            	}
		            	
		            	pools.add(new Pool(poolName, (Site[])sites.toArray(), (Trunk[])trunks.toArray()));
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
		HashMap<StatisticsObject, Integer> initialAttributesMap = MakeAttributeIndexesMap(initStatistics);
		
		//Get the members of this pool
		IbisIdentifier[] ibises = perfvis.getRegInterface().getMembers(poolName);
						
		//Initialize the list of sites
		List<String> siteNames = new ArrayList<String>();
		String[] locationsPerIbis = {};
		try {
			locationsPerIbis = perfvis.getRegInterface().getLocations(poolName);
			
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
					Node node = new Node(perfvis, siteName, ibises[i]);
					nodes.add(node);
					ibisesToNodes.put(ibises[i], node);	
					
					//Update this ibis's stats
					node.update(initialAttributesMap);
				}
			}
			sites.add(new Site(siteName, (Node[])nodes.toArray()));
		}
		
		//For each site, check for and (if available) create this site's links
		for (Site site : sites) {
			List<Link> links = new ArrayList<Link>();
			for (Node node : site.getNodes()) {					
				IbisIdentifier[] destinations;
				destinations = (IbisIdentifier[]) node.getConnectedIbises();
				
				for (int j=0; j<destinations.length; j++) {
					links.add(new Link(node, ibisesToNodes.get(destinations[j])));
				}
			}
			site.setLinks((Link[]) links.toArray());
		}		
		
		return sites;
	}
		
	public HashMap<StatisticsObject, Integer> MakeAttributeIndexesMap(List<StatisticsObject> interest) {			
		//calculate the size of the AttributeDescription array
		int attributesCount = 0;
		StatisticsObject[] currentInterest = (StatisticsObject[]) interest.toArray();
		for (int i=0; i<currentInterest.length; i++) {
			attributesCount += currentInterest[i].getAttributesCountNeeded();
		}
		
		AttributeDescription[] descriptions = new AttributeDescription[attributesCount];
		HashMap<StatisticsObject, Integer> attributeIndexes = new HashMap<StatisticsObject, Integer>();
		
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

	public List<Pool> getPools() {
		return pools;
	}

	public List<StatisticsObject> getAvailableStatistics() {
		return availableStatistics;
	}
	
	
}


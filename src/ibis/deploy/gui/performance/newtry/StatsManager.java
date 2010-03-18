package ibis.deploy.gui.performance.newtry;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.stats.*;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class StatsManager {
	//Variables needed for the operation of this class
	private PerfVis perfvis;	
	private Map<String, Integer> poolSizes;
	private HashMap<IbisIdentifier, String> ibisesToPools;
	private HashMap<IbisIdentifier, String> ibisesToSites;
	
	//useful variables for visualization
	private HashMap<String, String[]> poolsToSites;
	private HashMap<String, IbisIdentifier[]> poolsToIbises;	
	private HashMap<String, IbisIdentifier[]> sitesToIbises;
	
	private HashMap<String, String[]> linksSitesToSites;
	private HashMap<IbisIdentifier, IbisIdentifier[]> linksIbisesToIbises;
	
	private HashMap<IbisIdentifier, IbisManager> ibisesToManagers;
	
	private HashMap<String, StatisticsObject> availableStatistics; 
	
	public StatsManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		poolsToSites = new HashMap<String, String[]>();
		poolsToIbises = new HashMap<String, IbisIdentifier[]>();		
		sitesToIbises = new HashMap<String, IbisIdentifier[]>();
		ibisesToPools = new HashMap<IbisIdentifier, String>();
		ibisesToSites = new HashMap<IbisIdentifier, String>();
		
		//Map used to store all of the returned values from the ibises
		ibisesToManagers = new HashMap<IbisIdentifier, IbisManager>();
		
		//Maps to store network links 
		linksSitesToSites 	= new HashMap<String, String[]>();
		linksIbisesToIbises = new HashMap<IbisIdentifier, IbisIdentifier[]>();
		
		availableStatistics = new HashMap<String, StatisticsObject>();
		availableStatistics.put(CPUStatistic.NAME, new CPUStatistic());
		availableStatistics.put(MEMStatistic.NAME, new MEMStatistic());
		availableStatistics.put(ConnStatistic.NAME,  new ConnStatistic());
		availableStatistics.put(CoordsStatistic.NAME,new CoordsStatistic());
		availableStatistics.put(LinksStatistic.NAME, new LinksStatistic());		
	}
	
	@SuppressWarnings("unchecked")
	public void update() {		
		//Update the size of all pools and sites
		initPools();
		
		//for all pools
		for (Map.Entry<String, IbisIdentifier[]> entry : poolsToIbises.entrySet()) {			
			//check which statistics we are interested in
			List<StatisticsObject> currentPoolInterest = perfvis.getCurrentUpdateInterest(entry.getKey());
						
			//calculate the size of the AttributeDescription array
			int attributesCount = 0;
			StatisticsObject[] currentInterest = (StatisticsObject[]) currentPoolInterest.toArray();
			for (int i=0; i<currentInterest.length; i++) {
				attributesCount += currentInterest[i].getAttributesCountNeeded();
			}
			
			AttributeDescription[] descriptions = new AttributeDescription[attributesCount];
			HashMap<StatisticsObject, Integer> attributeIndexes = new HashMap<StatisticsObject, Integer>();; 
			
			//fill the array with the necessary attributes and remember their starting indexes
			attributesCount = 0;
			for (int i=0; i<currentInterest.length; i++) {
				attributeIndexes.put(currentInterest[i], attributesCount);
				for (int j=0; j<currentInterest[i].getAttributesCountNeeded(); j++) {
					descriptions[attributesCount+j] = currentInterest[i].getNecessaryAttributes()[j];
				}
				
				attributesCount += currentInterest[i].getAttributesCountNeeded();
			}
			
			//For all ibises in this pool, update
			IbisIdentifier[] ibises = entry.getValue();
			for (int i=0; i<ibises.length; i++) {				
				//And for all the statistics we are interested in
				for (int j=0; j<currentInterest.length; j++) {
					//clone the statistics objects and update them
					ibisesToManagers.get(ibises[i]).update((HashMap<StatisticsObject, Integer>) attributeIndexes.clone());
				}
			}
			
			//check whether we are interested in the connections (and therefore, links)
			//Now that all the ibises have been updated, we should be able to get the 
			//necessary stats from thier managers
			if (currentPoolInterest.contains(availableStatistics.get("CONN"))) {
				try {
					initLinks();
				} catch (StatNotRequestedException e) {			
					e.printStackTrace();
				}
			}
		}
	}
	
	public void initPools() {
		try {
			Map<String, Integer> newSizes = perfvis.getRegInterface().getPoolSizes();
						
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {				
	            String poolName = entry.getKey();
	            int newSize = entry.getValue();

	            if (newSize > 0) {
		            if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
		            	IbisIdentifier[] ibises = perfvis.getRegInterface().getMembers(poolName);
		            	
		            	poolsToIbises.remove(poolName);
		            	poolsToIbises.put(poolName, ibises);
		            	
		            	initSites(poolName);
		            	
		        		//Create managers for all attached ibises		        		
	        			for (int i=0; i<ibises.length; i++) {
	        				ibisesToManagers.put(ibises[i], new IbisManager(perfvis, ibises[i]));
	        				ibisesToPools.put(ibises[i], poolName);
	        			}			        		
		            }
	            }
	        }

			poolSizes = newSizes;
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
	
	private void initSites(String poolName) throws IOException {
		IbisIdentifier[] ibises = perfvis.getRegInterface().getMembers(poolName);
		
		HashMap<String, Integer> siteNamesHelper = new HashMap<String, Integer>();
						
		String[] locationsPerIbis = {};
		String[] siteNames = {};
		try {
			locationsPerIbis = perfvis.getRegInterface().getLocations(poolName);
			
			//The site name is after the @ sign, we make this array only contain unique names
			for (int i=0; i<locationsPerIbis.length; i++) {
				locationsPerIbis[i] = locationsPerIbis[i].split("@")[1];
				siteNamesHelper.put(locationsPerIbis[i], 0);
			}
						
			siteNames = new String[siteNamesHelper.size()];
			int j = 0;
			for (Map.Entry<String, Integer> entry : siteNamesHelper.entrySet()) {
				siteNames[j] = entry.getKey();
				j++;
			}
			poolsToSites.remove(poolName);
			poolsToSites.put(poolName, siteNames);
		} catch (IOException e) {					
			e.printStackTrace();
		}		
				
		//per site, make a list of all the ibises in that site
		String ibisLocationName;		
		for (int i=0; i<siteNames.length;i++) {
			int locationSize = 0;
			IbisIdentifier[] localIbises = new IbisIdentifier[0];
			
			//Determine which ibises belong to this site
			for (int j=0; j<ibises.length;j++) {
				ibisLocationName = ibises[j].location().toString().split("@")[1];
				
				//First determine the amount of ibises at this site
				if (ibisLocationName.compareTo(siteNames[i]) == 0) {
					locationSize++;
				}
			}
			
			//Then, create a Ibisidentifier array with that size
			localIbises = new IbisIdentifier[locationSize];							
			
			//And add all the site's ibises to that array
			int k = 0;
			for (int j=0; j<ibises.length;j++) {
				ibisLocationName = ibises[j].location().toString().split("@")[1];
				if (ibisLocationName.compareTo(siteNames[i]) == 0) {
					localIbises[k] = ibises[j];
					ibisesToSites.put(ibises[j], ibisLocationName);
					k++;
				}
			}
			sitesToIbises.remove(siteNames[i]);
			sitesToIbises.put(siteNames[i], localIbises);
		}
	}
	
	public void initLinks() throws StatNotRequestedException {		
		//forall sites
		for (Map.Entry<String, IbisIdentifier[]> entry : sitesToIbises.entrySet()) {
			IbisIdentifier[] ibises = entry.getValue();
			HashMap<String, Integer> destinationSites = new HashMap<String, Integer>();
			//and all ibises in each site
			for (int i=0; i<ibises.length; i++) {
				//determine the links between ibises
				IbisIdentifier[] destinations = (IbisIdentifier[]) ibisesToManagers.get(ibises[i]).getValues("CONN");
				linksIbisesToIbises.put(ibises[i], destinations);
				
				//and between sites
				for (int j=0; j<destinations.length; j++) {
					destinationSites.put(ibisesToSites.get(destinations[i]), 1);
				}
				
				linksSitesToSites.put(entry.getKey(), (String[]) destinationSites.entrySet().toArray());
			}
		}
	}
	
	public HashMap<String, StatisticsObject> getAvalableStatistics() {
		return availableStatistics;
	}
}


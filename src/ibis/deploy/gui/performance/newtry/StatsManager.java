package ibis.deploy.gui.performance.newtry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class StatsManager {
	//Variables needed for the operation of this class
	private PerfVis perfvis;	
	private Map<String, Integer> poolSizes;
	HashMap<String, AttributeDescription[]> statistics;
	private HashMap<IbisIdentifier, String> ibisesToPools;
	private HashMap<IbisIdentifier, String> ibisesToSites;
	
	//useful variables for visualization
	private HashMap<String, String[]> poolsToSites;
	private HashMap<String, IbisIdentifier[]> poolsToIbises;	
	private HashMap<String, IbisIdentifier[]> sitesToIbises;
	
	private HashMap<String, String[]> linksSitesToSites;
	private HashMap<IbisIdentifier, IbisIdentifier[]> linksIbisesToIbises;
	
	private HashMap<IbisIdentifier, IbisManager> ibisesToManagers;
	
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
		
		//The attributes we could get
		statistics = new HashMap<String, AttributeDescription[]>();
		AttributeDescription[] necessaryStatistics;
		
		//CPU	
		necessaryStatistics = new AttributeDescription[3];
		necessaryStatistics[0] = new AttributeDescription("java.lang:type=OperatingSystem", "ProcessCpuTime");
		necessaryStatistics[1] = new AttributeDescription("java.lang:type=Runtime", "Uptime");
		necessaryStatistics[2] = new AttributeDescription("java.lang:type=OperatingSystem", "AvailableProcessors");
		statistics.put("CPU", necessaryStatistics);
		
		//MEM
		necessaryStatistics = new AttributeDescription[2];
		necessaryStatistics[0] = new AttributeDescription("java.lang:type=Memory", "HeapMemoryUsage");
		necessaryStatistics[1] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
		statistics.put("MEM", necessaryStatistics);
		
		//Connections
		necessaryStatistics = new AttributeDescription[1];
		necessaryStatistics[0] = new AttributeDescription("ibis", "connections");
		statistics.put("CONN", necessaryStatistics);
		
		//Links
		necessaryStatistics = new AttributeDescription[2];
		necessaryStatistics[0] = new AttributeDescription("ibis", "vivaldi");		
		necessaryStatistics[1] = new AttributeDescription("ibis", "bytesSent");
		statistics.put("LINKS", necessaryStatistics);
	}
	
	public void update() {
		//Update the size of all pools and sites
		initPools();
		
		//And update the network between ibises and sites
		if (perfvis.getCurrentStat() == PerfVis.STAT_ALL || perfvis.getCurrentStat() == PerfVis.STAT_LINKS) {
			try {
				initLinks();
			} catch (StatNotRequestedException e) {			
				e.printStackTrace();
			}
		}
		
		
		
		//Forall ibises, call the update function
		for (Map.Entry<String, IbisIdentifier[]> entry : poolsToIbises.entrySet()) {
			IbisIdentifier[] ibises = entry.getValue();
			for (int i=0; i<ibises.length; i++) {
				ibisesToManagers.get(ibises[i]).update();
			}
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
				IbisIdentifier[] destinations = ibisesToManagers.get(ibises[i]).getConnections();
				linksIbisesToIbises.put(ibises[i], destinations);
				
				//and between sites
				for (int j=0; j<destinations.length; j++) {
					destinationSites.put(ibisesToSites.get(destinations[i]), 1);
				}
				
				linksSitesToSites.put(entry.getKey(), (String[]) destinationSites.entrySet().toArray());
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
	
}


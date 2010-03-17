package ibis.deploy.gui.performance.newtry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.hierarchy.Hnode;
import ibis.deploy.gui.performance.hierarchy.Hpool;
import ibis.deploy.gui.performance.hierarchy.Hsite;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class StatsManager {
	private final static String[] NODESTATS = {"CPU Used", "MEM Used", "Connections"};
	private final static String[] LINKSTATS = {"SENT"};
	
	AttributeDescription[] statsToGet;
	
	HashMap<String, String[]> poolsToSites;
	HashMap<String, IbisIdentifier[]> poolsToIbises;	
	HashMap<String, IbisIdentifier[]> sitesToIbises;
	
	HashMap<String, String> linksPoolsToPools;
	HashMap<String, String> linksSitestToSites;
	HashMap<IbisIdentifier, IbisIdentifier> linksIbisesToIbises;
	
	HashMap<IbisIdentifier, IbisManager> ibisesToManagers;
	
	PerfVis perfvis;
	
	Map<String, Integer> poolSizes;
	
	public StatsManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		
		//The HashMap used to check whether pools have changed
		poolSizes = new HashMap<String, Integer>();
		
		//Maps to store ibises and their groups
		poolsToSites = new HashMap<String, String[]>();
		poolsToIbises = new HashMap<String, IbisIdentifier[]>();
		sitesToIbises = new HashMap<String, IbisIdentifier[]>();
		
		//Map used to store all of the returned values from the ibises
		ibisesToManagers = new HashMap<IbisIdentifier, IbisManager>();
		
		//Maps to store network links 
		linksPoolsToPools 	= new HashMap<String, String>();
		linksSitestToSites 	= new HashMap<String, String>();
		linksIbisesToIbises = new HashMap<IbisIdentifier, IbisIdentifier>();
		
		//The attributes we need to get
		//processCpuTime, upTime, cpus, mem_heap, mem_nonheap, vivaldi, connections, sent
		//CPU
		statsToGet[0] = new AttributeDescription("java.lang:type=OperatingSystem", "ProcessCpuTime");
		statsToGet[1] = new AttributeDescription("java.lang:type=Runtime", "Uptime");
		statsToGet[2] = new AttributeDescription("java.lang:type=OperatingSystem", "AvailableProcessors");
		
		//MEM
		statsToGet[3] = new AttributeDescription("java.lang:type=Memory", "HeapMemoryUsage");
		statsToGet[4] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
		
		//Links
		statsToGet[5] = new AttributeDescription("ibis", "vivaldi");
		statsToGet[6] = new AttributeDescription("ibis", "connections");
		statsToGet[7] = new AttributeDescription("ibis", "bytesSent");
				
		initPools();
		

		
	}
	
	public void update() {
		//Forall pools
		for (Map.Entry<String, IbisIdentifier[]> entry : poolsToIbises.entrySet()) {
			IbisIdentifier[] ibises = entry.getValue();
			//and all ibises in each pool
			for (int i=0; i<ibises.length; i++) {
				ibisesToManagers.get(ibises[i]).update();
			}
		}
	}
	
	public void initLinks() {
		//TODO			
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
	        				ibisesToManagers.put(ibises[i], new IbisManager(perfvis, ibises[i], statsToGet));
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
					k++;
				}
			}
			sitesToIbises.remove(siteNames[i]);
			sitesToIbises.put(siteNames[i], localIbises);
		}
	}
	
}


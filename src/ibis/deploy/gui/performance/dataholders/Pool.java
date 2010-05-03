package ibis.deploy.gui.performance.dataholders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.MetricsObject;
import ibis.deploy.gui.performance.metrics.special.ConnStatistic;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class Pool extends IbisConcept implements IbisConceptInterface {
	private String name;
	private List<Site> sites;

	public Pool(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, ArrayList<MetricsObject> initialStatistics, String poolName) {
		super(manInterface);
		this.name = poolName;
		
		this.sites = new ArrayList<Site>();	
		
		//Get the members of this pool
		IbisIdentifier[] ibises = {};
		try {
			ibises = regInterface.getMembers(poolName);
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
						
		//Initialize the list of sites
		Set<String> siteNames = new HashSet<String>();
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
			sites.add(new Site(manInterface, initialStatistics, ibises, siteName));
		}
	}
	
	public String getName() {
		return name;
	}	
	
	public IbisIdentifier[] getIbises() {
		List<IbisIdentifier> result = new ArrayList<IbisIdentifier>();
		
		for (Site site : sites) {
			IbisIdentifier[] nodes = site.getIbises();
			for (int j=0; j<nodes.length; j++) {
				result.add(nodes[j]);
			}
		}
		return (IbisIdentifier[]) result.toArray();		
	}	
	
	public void update() throws StatNotRequestedException {	
		for (Site site : sites) {			
			site.update();
		}
		
		ArrayList<MetricsObject> stats = sites.get(0).getCurrentlyGatheredStatistics();
		for (MetricsObject stat : stats) {
			if (compareStats(stat)) {
				if (!stat.getName().equals(ConnStatistic.NAME)) {
					String key = stat.getName();
					List<Float> results = new ArrayList<Float>();
					for (Site site : sites) {			
						results.add(site.getValue(key));
					}
					float total = 0, average = 0;
					for (Float entry : results) {
						total += entry;
					}
					average = total / results.size();
					nodeMetricsValues.put(key, average);
					nodeMetricsColors.put(key, stat.getColor());
				}
			}
		}
	}	
	
	private boolean compareStats(MetricsObject stat) {							
		for (Site site : sites) {
			if (!site.getCurrentlyGatheredStatistics().contains(stat)) return false;
		}		
		return true;
	}
	
	public void setCurrentlyGatheredStatistics(ArrayList<MetricsObject> currentlyGatheredStatistics) {
		for (Site site : sites) {
			site.setCurrentlyGatheredMetrics(currentlyGatheredStatistics);
		}
	}
	
	public Site[] getSubConcepts() {
		Site[] result = new Site[sites.size()];
		sites.toArray(result);
		return result;
	}
}

package ibis.deploy.gui.performance.dataholders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.performance.MetricsList;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.Metric;
import ibis.deploy.gui.performance.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Pool implements IbisConceptInterface {
	private HashMap<String, Float> nodeMetricsValues;
	private HashMap<IbisIdentifier, Map<String, Float>> linkMetricsValues;
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	private String name;
	private List<Site> sites;

	public Pool(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialStatistics, String poolName) {		
		this.name = poolName;
		
		this.sites = new ArrayList<Site>();	
		nodeMetricsValues = new HashMap<String, Float>();
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
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
			sites.add(new Site(manInterface, initialStatistics.clone(), ibises, siteName));
		}
		
		setCurrentlyGatheredMetrics(initialStatistics);
	}
	
	public String getName() {
		return name;
	}	
	
	public IbisIdentifier[] getIbises() {
		synchronized(this) {
			List<IbisIdentifier> result = new ArrayList<IbisIdentifier>();
		
			for (Site site : sites) {
				IbisIdentifier[] nodes = site.getIbises();
				for (int j=0; j<nodes.length; j++) {
					result.add(nodes[j]);
				}
			}
			return (IbisIdentifier[]) result.toArray();
		}		
	}	
	
	public void update() throws StatNotRequestedException, NoSuitableModuleException {	
		synchronized(this) {
			for (Site site : sites) {			
				site.update();
			}
			
			MetricsList stats = sites.get(0).getCurrentlyGatheredMetrics();
			for (Metric metric : stats) {
				if (compareStats(metric)) {
					if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					//if (!metric.getName().equals(ConnStatistic.NAME)) {
						String key = metric.getName();
						List<Float> results = new ArrayList<Float>();
						for (Site site : sites) {			
							results.add(site.getValue(key));
						}
						float total = 0, average = 0;
						for (Float entry : results) {
							total += entry;
						}
						average = total / results.size();
						
						if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
							nodeMetricsValues.put(metric.getName(), average);						
						//} else if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {
						//	linkMetricsValues.put(metric.getName(), average);
						}
					}
				}
			}
		}
	}	
	
	private boolean compareStats(Metric stat) {							
		for (Site site : sites) {
			if (!site.getCurrentlyGatheredMetrics().contains(stat)) return false;
		}		
		return true;
	}
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		nodeMetricsValues.clear();
		linkMetricsValues.clear();		
		
		for (Metric metric : newMetrics) {			
			if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
				nodeMetricsColors.put(metric.getName(), metric.getColor());
			//} else if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {
			//	linkMetricsColors.put(metric.getName(), metric.getColor());
			}
		}
		
		for (Site site : sites) {
			site.setCurrentlyGatheredMetrics(newMetrics);
		}		
	}
	
	public Site[] getSubConcepts() {
		synchronized(this) {
			Site[] result = new Site[sites.size()];
			sites.toArray(result);
			return result;
		}
	}
	
	public float getValue(String key) throws StatNotRequestedException {
		synchronized(this) {
			if (nodeMetricsValues.containsKey(key))	{
				return nodeMetricsValues.get(key);
			} else {
				System.out.println(key +" was not requested.");
				throw new StatNotRequestedException();
			}
		}
	}
		
	public HashMap<String, Float> getMonitoredNodeMetrics() {
		synchronized(this) {	
			return nodeMetricsValues;
		}
	}
	
	public Set<String> getMonitoredLinkMetrics() {
		synchronized(this) {
			HashSet<String> newSet = new HashSet<String>(); 
			for (Entry<IbisIdentifier, Map<String, Float>> entry : linkMetricsValues.entrySet()) {
				for (Entry<String, Float> entry2 : entry.getValue().entrySet()) {
					newSet.add(entry2.getKey());
				}
			}
			return newSet;
		}
	}
			
	public HashMap<IbisIdentifier, Map<String, Float>> getLinkValues() {	
		synchronized(this) {
			return linkMetricsValues;
		}
	}
	
	public HashMap<String, Float[]> getMetricsColors() {
		synchronized(this) {
			return nodeMetricsColors;
		}
	}
	
	public HashMap<String, Float[]> getLinkColors() {
		synchronized(this) {
			return linkMetricsColors;
		}
	}	
}

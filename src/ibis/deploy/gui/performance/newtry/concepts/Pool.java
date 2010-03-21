package ibis.deploy.gui.performance.newtry.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.stats.StatisticsObject;
import ibis.ipl.IbisIdentifier;

public class Pool {
	private String name;
	private Site[] sites;
	private Trunk[] trunks;
	private HashMap<String, Float> averageValues;
	
	public Pool(String name, Site[] sites, Trunk[] trunks) {
		this.name = name;
		this.sites = sites;
		this.trunks = trunks;
		
		averageValues = new HashMap<String, Float>();
	}

	public String getName() {
		return name;
	}

	public Site[] getSites() {
		return sites;
	}

	public Trunk[] getTrunks() {
		return trunks;
	}
	
	public IbisIdentifier[] getIbises() {
		List<IbisIdentifier> result = new ArrayList<IbisIdentifier>();
		for (int i=0; i< sites.length; i++) {
			IbisIdentifier[] nodes = sites[i].getIbises();
			for (int j=0; j<nodes.length; j++) {
				result.add(nodes[j]);
			}
		}
		return (IbisIdentifier[]) result.toArray();		
	}	
	
	public void updateAverages() throws StatNotRequestedException {		
		List<StatisticsObject> stats = sites[0].getCurrentlyGatheredStatistics();
		for (StatisticsObject stat : stats) {
			if (compareStats(stat)) {		
				String key = stat.getName();
				List<Float> results = new ArrayList<Float>();
				for (Site site : sites) {			
					results.add(site.getAverageValue(key));
				}
				float total = 0, average = 0;
				for (Float entry : results) {
					total += entry;
				}
				average = total / results.size();
				averageValues.put(key, average);
			}
		}
	}
	
	private boolean compareStats(StatisticsObject stat) {							
		for (Site site : sites) {
			if (!site.getCurrentlyGatheredStatistics().contains(stat)) return false;
		}		
		return true;
	}
	
	
	public float getAverageValue(String key) {
		return averageValues.get(key);
	}
}

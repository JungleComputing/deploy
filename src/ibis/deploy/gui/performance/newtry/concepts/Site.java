package ibis.deploy.gui.performance.newtry.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.stats.StatisticsObject;
import ibis.ipl.IbisIdentifier;

public class Site {	
	private String name;
	private Node[] nodes;
	private Link[] links;
	
	private HashMap<String, Float> averageValues;
	
	private List<StatisticsObject> currentlyGatheredStatistics;

	public Site(String name, Node[] nodes) {		
		this.name = name;
		this.nodes = nodes;
		
		averageValues = new HashMap<String, Float>();
	}
	
	public void setLinks(Link[] links) {
		this.links = links;
	}

	public String getName() {
		return name;
	}

	public Node[] getNodes() {
		return nodes;
	}
	
	public Link[] getLinks() {
		return links;
	}

	public IbisIdentifier[] getIbises() {
		IbisIdentifier[] result = new IbisIdentifier[nodes.length];
		for (int i=0; i< nodes.length; i++) {
			result[i] = nodes[i].getName();
		}
		return result;
	}
	
	public List<StatisticsObject> getCurrentlyGatheredStatistics() {
		return currentlyGatheredStatistics;
	}

	public void setCurrentlyGatheredStatistics(
			List<StatisticsObject> currentlyGatheredStatistics) {
		this.currentlyGatheredStatistics = currentlyGatheredStatistics;
	}
	
	public void updateAverages() throws StatNotRequestedException {		
		for (StatisticsObject stat : currentlyGatheredStatistics) {
			String key = stat.getName();
			List<Float> results = new ArrayList<Float>();
			for (Node node : nodes) {			
				results.add((Float)node.getValue(key));
			}
			float total = 0, average = 0;
			for (Float entry : results) {
				total += entry;
			}
			average = total / results.size();
			averageValues.put(key, average);
		}
	}

	public float getAverageValue(String key) {
		return averageValues.get(key);
	}
	
	
}

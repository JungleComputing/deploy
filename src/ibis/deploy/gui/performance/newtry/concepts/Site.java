package ibis.deploy.gui.performance.newtry.concepts;

import java.util.List;

import ibis.deploy.gui.performance.newtry.stats.StatisticsObject;
import ibis.ipl.IbisIdentifier;

public class Site {	
	String name;
	Node[] nodes;
	Link[] links;
	
	List<StatisticsObject> currentlyGatheredStatistics;

	public Site(String name, Node[] nodes) {		
		this.name = name;
		this.nodes = nodes;		
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
}

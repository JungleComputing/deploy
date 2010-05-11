package ibis.deploy.gui.performance.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ibis.deploy.gui.performance.MetricsList;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.Metric;
import ibis.deploy.gui.performance.metrics.special.ConnStatistic;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Site extends IbisConcept implements IbisConceptInterface {	
	private String name;	
	private ArrayList<Node> nodes;
	private HashMap<IbisIdentifier, Node> ibisesToNodes;
	
	private MetricsList currentlyGatheredMetrics;
	
	public Site(ManagementServiceInterface manInterface, MetricsList initialStatistics, IbisIdentifier[] poolIbises, String siteName) {	
		super(manInterface);
		this.name = siteName;				
		this.nodes = new ArrayList<Node>();
		this.ibisesToNodes = new HashMap<IbisIdentifier, Node>();
		
		String ibisLocationName;
		
		//Determine which ibises belong to this site
		for (int i=0; i<poolIbises.length; i++) {
			ibisLocationName = poolIbises[i].location().toString().split("@")[1];
			
			//And compare all ibises' locations to that sitename
			if (ibisLocationName.compareTo(siteName) == 0) {
				Node node = new Node(manInterface, siteName, poolIbises[i]);
				nodes.add(node);
				ibisesToNodes.put(poolIbises[i], node);
			}
		}
		
		setCurrentlyGatheredMetrics(initialStatistics);		
	}
		
	public String getName() {
		return name;
	}
	
	public Node getNode(IbisIdentifier ibis) {
		return ibisesToNodes.get(ibis);		
	}

	public IbisIdentifier[] getIbises() {
		IbisIdentifier[] result = new IbisIdentifier[nodes.size()];
		int i=0;
		for (Node node : nodes) {
			result[i] = node.getName();
			i++;
		}
		return result;
	}	
	
	public void update() throws StatNotRequestedException, NoSuitableModuleException {	
		nodeMetricsValues.clear();
		linkMetricsValues.clear();
		
		for (Node node : nodes) {			
			node.update();
		}
		
		for (Metric stat : currentlyGatheredMetrics) {
			if (!stat.getName().equals(ConnStatistic.NAME)) {
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
				nodeMetricsValues.put(key, average);
				nodeMetricsColors.put(key, stat.getColor());
			}
		}
	}
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		for (Node node : nodes) {			
			node.setCurrentlyGatheredMetrics(newMetrics);
		}
		this.currentlyGatheredMetrics = newMetrics;
	}

	public MetricsList getCurrentlyGatheredMetrics() {
		return currentlyGatheredMetrics;
	}
	
	public Node[] getSubConcepts() {
		Node[] result = new Node[nodes.size()];
		nodes.toArray(result);
		return result;
	}
	
	public float getValue(String key) throws StatNotRequestedException {
		if (nodeMetricsValues.containsKey(key))	{
			return nodeMetricsValues.get(key);
		} else if (linkMetricsValues.containsKey(key))	{
			return linkMetricsValues.get(key);
		} else {			
			throw new StatNotRequestedException();
		}
	}
}

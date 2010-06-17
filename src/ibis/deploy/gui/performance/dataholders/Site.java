package ibis.deploy.gui.performance.dataholders;

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
import ibis.deploy.gui.performance.metrics.link.LinkMetricsObject;
import ibis.deploy.gui.performance.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Site implements IbisConceptInterface {
	private HashMap<String, Float> nodeMetricsValues;
	private HashMap<IbisIdentifier, Map<String, Float>> linkMetricsValues;
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	private String name;	
	private ArrayList<Node> nodes;
	private HashMap<IbisIdentifier, Node> ibisesToNodes;
	
	private MetricsList currentlyGatheredMetrics;
	
	public Site(ManagementServiceInterface manInterface, MetricsList initialStatistics, IbisIdentifier[] poolIbises, String siteName) {		
		this.name = siteName;				
		this.nodes = new ArrayList<Node>();
		this.ibisesToNodes = new HashMap<IbisIdentifier, Node>();
		
		currentlyGatheredMetrics = new MetricsList();
		nodeMetricsValues = new HashMap<String, Float>();
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
		String ibisLocationName;
		
		//Determine which ibises belong to this site
		for (int i=0; i<poolIbises.length; i++) {
			ibisLocationName = poolIbises[i].location().toString().split("@")[1];
			
			//And compare all ibises' locations to that sitename
			if (ibisLocationName.compareTo(siteName) == 0) {
				Node node = new Node(manInterface, siteName, poolIbises[i]);
				nodes.add(node);
				//new Thread(node).start();
				ibisesToNodes.put(poolIbises[i], node);
			}
		}				
	}
		
	public String getName() {
		return name;
	}
	
	public Node getNode(IbisIdentifier ibis) {
		return ibisesToNodes.get(ibis);		
	}

	public IbisIdentifier[] getIbises() {
		synchronized(this) {
			IbisIdentifier[] result = new IbisIdentifier[nodes.size()];
			int i=0;
			for (Node node : nodes) {
				result[i] = node.getName();
				i++;
			}
			return result;
		}
	}	
	
	public void update() throws NoSuitableModuleException, StatNotRequestedException {	
		synchronized(this) {
			nodeMetricsValues.clear();
			linkMetricsValues.clear();
			
			for (Node node : nodes) {			
				node.update();
			}
			
			for (Metric metric : currentlyGatheredMetrics) {
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
				//if (!metric.getName().equals(ConnStatistic.NAME)) {
					String key = metric.getName();
					List<Float> results = new ArrayList<Float>();
					for (Node node : nodes) {			
						results.add((Float)node.getValue(key));
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
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		nodeMetricsValues.clear();
		linkMetricsValues.clear();
		currentlyGatheredMetrics.clear();
		
		for (Metric metric : newMetrics) {
			currentlyGatheredMetrics.add(metric.clone());
			if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
				nodeMetricsColors.put(metric.getName(), metric.getColor());
			} else if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {
				linkMetricsColors.put(metric.getName(), metric.getColor());
			}
		}
		
		for (Node node : nodes) {			
			node.setCurrentlyGatheredMetrics(newMetrics);
		}		
	}

	public MetricsList getCurrentlyGatheredMetrics() {
		synchronized(this) {
			return currentlyGatheredMetrics;
		}
	}
	
	public Node[] getSubConcepts() {
		synchronized(this) {
			Node[] result = new Node[nodes.size()];
			nodes.toArray(result);
			return result;
		}
	}
	
	public float getValue(String key) throws StatNotRequestedException {
		synchronized(this) {
			if (nodeMetricsValues.containsKey(key))	{
				return nodeMetricsValues.get(key);
			} else {			
				throw new StatNotRequestedException();
			}
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
			
	public Map<String, Float> getLinkValueMap(IbisIdentifier ibis) {
		synchronized(this) {
			Map<String, Float> copy = new HashMap<String, Float>(linkMetricsValues.get(ibis));
			return copy;
		}
	}
		
	public HashMap<String, Float> getMonitoredNodeMetrics() {
		synchronized(this) {
			HashMap<String, Float> copy = new HashMap<String, Float>(nodeMetricsValues);
			return copy;
		}
	}
			
	public HashMap<IbisIdentifier, Map<String, Float>> getLinkValues() {
		synchronized(this) {
			HashMap<IbisIdentifier, Map<String, Float>> copy = new HashMap<IbisIdentifier, Map<String, Float>>(linkMetricsValues);
			return copy;
		}
	}
	
	public HashMap<String, Float[]> getMetricsColors() {
		synchronized(this) {
			HashMap<String, Float[]> copy = new HashMap<String, Float[]>(nodeMetricsColors);
			return copy;
		}
	}
	
	public HashMap<String, Float[]> getLinkColors() {
		synchronized(this) {
			HashMap<String, Float[]> copy = new HashMap<String, Float[]>(linkMetricsColors);
			return copy;
		}
	}
}

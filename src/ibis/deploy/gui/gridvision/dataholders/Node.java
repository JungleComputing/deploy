package ibis.deploy.gui.gridvision.dataholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.interfaces.IbisConcept;
import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.deploy.gui.gridvision.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Node implements IbisConcept {
	//Variables needed for the operation of this class	
	private ManagementServiceInterface manInterface;

	private HashMap<String, Float> nodeMetricsValues;
	private HashMap<IbisIdentifier, Map<String, Float>> linkMetricsValues;
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	private MetricsList currentlyGatheredMetrics;
	
	private String siteName;
	private IbisIdentifier name;	
	
	protected HashSet<IbisIdentifier> connections;
	
	public Node(ManagementServiceInterface manInterface, MetricsList initialMetrics, String siteName, IbisIdentifier name) {
		this.siteName = siteName;
		this.name = name;
		this.manInterface = manInterface;
		
		currentlyGatheredMetrics = new MetricsList();
		setCurrentlyGatheredMetrics(initialMetrics.clone());
		
		nodeMetricsValues = new HashMap<String, Float>();
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
		connections = new HashSet<IbisIdentifier>();
	}
	
	public void setMetricUnmonitored(Metric metricToUncheck) {
		getCurrentlyGatheredMetrics();
	}
	
	public MetricsList getCurrentlyGatheredMetrics() {
		synchronized(this) {
			return currentlyGatheredMetrics;
		}
	}
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		currentlyGatheredMetrics.clear();
		nodeMetricsColors.clear();
		linkMetricsColors.clear();
		for (Metric metric : newMetrics) {
			currentlyGatheredMetrics.add(metric.clone());
			
			if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {			
				nodeMetricsColors.put(metric.getName(), metric.getColor());
			} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
				linkMetricsColors.put(metric.getName(), metric.getColor());					
			}
		}
	}
	
	public void update() throws NoSuitableModuleException {		
		HashMap<String, Float> newNodeMetricsValues = new HashMap<String, Float>();
		HashMap<IbisIdentifier, Map<String, Float>> newLinkMetricsValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		HashSet<IbisIdentifier> newConnections = new HashSet<IbisIdentifier>();		
		
		try {
			int size = 0;
			for (Metric metric : currentlyGatheredMetrics) {
				size += metric.getAttributesCountNeeded();
			}
			
			AttributeDescription[] requestArray = new AttributeDescription[size]; 
			int j=0;
			for (Metric metric : currentlyGatheredMetrics) {
				AttributeDescription[] tempArray = metric.getNecessaryAttributes();
				for (int i=0; i < tempArray.length; i++) {
					requestArray[j] = tempArray[i];
					j++;
				}
			}
			
			Object[] results = manInterface.getAttributes(name, requestArray);
			
			j=0;			
			for (Metric metric : currentlyGatheredMetrics) {
				Object[] partialResults = new Object[metric.getAttributesCountNeeded()];				
				for (int i=0; i < partialResults.length ; i++) {
					partialResults[i] = results[j];	
					j++;
				}
				metric.update(partialResults);
								
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					newNodeMetricsValues.put(metric.getName(), metric.getValue());
				
				} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
					Map<IbisIdentifier, Float> values = ((LinkMetricsMap) metric).getValues();
										
					for (IbisIdentifier ibis : values.keySet()) {
						if (!newLinkMetricsValues.containsKey(ibis)) {								
							newLinkMetricsValues.put(ibis, new HashMap<String, Float>());				
						}
						newLinkMetricsValues.get(ibis).put(metric.getName(), values.get(ibis));
						newConnections.add(ibis);
					}
				}
			}
		} catch (Exception e) {			
			e.printStackTrace();
		} 
		
		synchronized(this) {			
			nodeMetricsValues = newNodeMetricsValues;
			linkMetricsValues = newLinkMetricsValues;
			connections = newConnections;
		}
	}	

	public String getSiteName() {
		return siteName;
	}

	public IbisIdentifier getName() {
		return name;
	}
	
	public IbisConcept[] getSubConcepts() {	
		return null;
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
	
	public Set<IbisIdentifier> getConnections() {
		synchronized(this) {
			Set<IbisIdentifier> copy = new HashSet<IbisIdentifier>(connections);
			return copy;
		}
	}
	
}

package ibis.deploy.gui.performance.dataholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.performance.MetricsList;
import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.Metric;
import ibis.deploy.gui.performance.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.performance.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Node implements IbisConceptInterface {
	//Variables needed for the operation of this class	
	private ManagementServiceInterface manInterface;

	private HashMap<String, Float> nodeMetricsValues;
	private HashMap<IbisIdentifier, Map<String, Float>> linkMetricsValues;
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	private String siteName;
	private IbisIdentifier name;	
	
	protected HashSet<IbisIdentifier> connections;
	private MetricsList metrics;
	
	public Node(ManagementServiceInterface manInterface, String siteName, IbisIdentifier name) {
		this.siteName = siteName;
		this.name = name;
		this.manInterface = manInterface;
		
		nodeMetricsValues = new HashMap<String, Float>();
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
		connections = new HashSet<IbisIdentifier>();
		
		metrics = new MetricsList();
	}
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		metrics.clear();
		nodeMetricsColors.clear();
		linkMetricsColors.clear();
		for (Metric metric : newMetrics) {
			metrics.add(metric.clone());
			
			if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {			
				nodeMetricsColors.put(metric.getName(), metric.getColor());
			} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
				linkMetricsColors.put(metric.getName(), metric.getColor());					
			}
		}
	}
	
	public void update() throws NoSuitableModuleException {
		synchronized(this) {
			//First, clear the Maps with the values, to be refilled with the newly requested entries
			nodeMetricsValues.clear();
			linkMetricsValues.clear();
			connections.clear();
			
			try {
				int size = 0;
				for (Metric metric : metrics) {
					size += metric.getAttributesCountNeeded();
				}
				
				AttributeDescription[] requestArray = new AttributeDescription[size]; 
				int j=0;
				for (Metric metric : metrics) {
					AttributeDescription[] tempArray = metric.getNecessaryAttributes();
					for (int i=0; i < tempArray.length; i++) {
						requestArray[j] = tempArray[i];
						j++;
					}
				}
				
				Object[] results = manInterface.getAttributes(name, requestArray);
				
				j=0;			
				for (Metric metric : metrics) {
					Object[] partialResults = new Object[metric.getAttributesCountNeeded()];				
					for (int i=0; i < partialResults.length ; i++) {
						partialResults[i] = results[j];	
						j++;
					}
					metric.update(partialResults);
									
					if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
						nodeMetricsValues.put(metric.getName(), metric.getValue());
					
					} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
						Map<IbisIdentifier, Float> values = ((LinkMetricsMap) metric).getValues();
											
						for (IbisIdentifier ibis : values.keySet()) {
							if (!linkMetricsValues.containsKey(ibis)) {								
								linkMetricsValues.put(ibis, new HashMap<String, Float>());				
							}
							linkMetricsValues.get(ibis).put(metric.getName(), values.get(ibis));
							connections.add(ibis);
						}
					}
				}
			} catch (Exception e) {			
				e.printStackTrace();
			} 
		}
	}	

	public String getSiteName() {
		return siteName;
	}

	public IbisIdentifier getName() {
		return name;
	}
	
	public IbisConceptInterface[] getSubConcepts() {	
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

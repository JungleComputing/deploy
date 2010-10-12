package ibis.deploy.gui.gridvision.dataholders;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.deploy.gui.gridvision.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
		
	private HashMap<String, Float> nodeMetricsMaxValues;
	private HashMap<String, Float> nodeMetricsAvgValues;
	private HashMap<String, Float> nodeMetricsMinValues;
		
	private HashMap<IbisConcept, Map<String, Float>> linkMetricsMaxValues;
	private HashMap<IbisConcept, Map<String, Float>> linkMetricsAvgValues;
	private HashMap<IbisConcept, Map<String, Float>> linkMetricsMinValues;
	
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	private List<IbisConcept> subConcepts;
	private List<IbisConcept> links;
	
	private MetricsList currentlyGatheredMetrics;

	public IbisConcept(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics) {
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		
		nodeMetricsMaxValues = new HashMap<String, Float>();
		nodeMetricsAvgValues = new HashMap<String, Float>();
		nodeMetricsMinValues = new HashMap<String, Float>();
		
		linkMetricsMaxValues = new HashMap<IbisConcept, Map<String, Float>>();
		linkMetricsAvgValues = new HashMap<IbisConcept, Map<String, Float>>();
		linkMetricsMinValues = new HashMap<IbisConcept, Map<String, Float>>();
		
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
		subConcepts = new ArrayList<IbisConcept>();
		links = new ArrayList<IbisConcept>();
		
		currentlyGatheredMetrics = new MetricsList();
		setCurrentlyGatheredMetrics(initialMetrics.clone());
	}

	public float getNodeMetricsValue(String key, int mod) throws StatNotRequestedException, ModeUnknownException {
		if (currentlyGatheredMetrics.contains(key)) {
			if (nodeMetricsMaxValues.containsKey(key)) {
				if (mod == ibis.deploy.gui.gridvision.dataholders.IbisConcept.MAX) {
					return nodeMetricsMaxValues.get(key);
				} else if (mod == ibis.deploy.gui.gridvision.dataholders.IbisConcept.AVG) {
					return nodeMetricsAvgValues.get(key);
				} else if (mod == ibis.deploy.gui.gridvision.dataholders.IbisConcept.MIN) {
					return nodeMetricsMinValues.get(key);
				} else {
					throw new ModeUnknownException();
				}
			} else {
				throw new StatNotRequestedException();
			}
		} else {
			throw new StatNotRequestedException();
		}
	}

	public ibis.deploy.gui.gridvision.interfaces.IbisConcept[] getSubConcepts() {
		synchronized(this) {
			IbisConcept[] result = new IbisConcept[subConcepts.size()];
			subConcepts.toArray(result);
			return result;
		}
	}
	
	public ibis.deploy.gui.gridvision.interfaces.IbisConcept[] getLinks() {
		synchronized(this) {
			IbisConcept[] result = new IbisConcept[links.size()];
			links.toArray(result);
			return result;
		}
	}

	public Set<String> getMonitoredNodeMetrics() {
		synchronized(this) {
			Set<String> copy = new HashSet<String>();
			for (Metric metric: currentlyGatheredMetrics) {
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					copy.add(metric.getName());
				}
			}			
			return copy;
		}
	}

	public Set<String> getMonitoredLinkMetrics() {
		synchronized(this) {
			Set<String> copy = new HashSet<String>();
			for (Metric metric: currentlyGatheredMetrics) {
				if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
					copy.add(metric.getName());
				}
			}			
			return copy;
		}
	}

	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		nodeMetricsMaxValues.clear();
		nodeMetricsAvgValues.clear();
		nodeMetricsMinValues.clear();
		
		linkMetricsMaxValues.clear();
		linkMetricsAvgValues.clear();
		linkMetricsMinValues.clear();
		
		nodeMetricsColors.clear();
		linkMetricsColors.clear();
		
		currentlyGatheredMetrics.clear();
		
		for (Metric metric : newMetrics) {
			currentlyGatheredMetrics.add(metric.clone());
			if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {				
				nodeMetricsColors.put(metric.getName(), metric.getColor());				
			} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
				linkMetricsColors.put(metric.getName(), metric.getColor());
			}
		}
		
		for (ibis.deploy.gui.gridvision.interfaces.IbisConcept concept : subConcepts) {			
			concept.setCurrentlyGatheredMetrics(newMetrics);
		}		
	}
	
	public MetricsList getCurrentlyGatheredMetrics() {
		return currentlyGatheredMetrics;
	}

	public void update() throws NoSuitableModuleException, StatNotRequestedException {		
		HashMap<String, Float> newNodeMetricsMaxValues = new HashMap<String, Float>();
		HashMap<String, Float> newNodeMetricsAvgValues = new HashMap<String, Float>();
		HashMap<String, Float> newNodeMetricsMinValues = new HashMap<String, Float>();
		
		HashMap<IbisIdentifier, Map<String, Float>> newLinkMetricsMaxValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		HashMap<IbisIdentifier, Map<String, Float>> newLinkMetricsAvgValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		HashMap<IbisIdentifier, Map<String, Float>> newLinkMetricsMinValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		
		
		for (ibis.deploy.gui.gridvision.interfaces.IbisConcept subConcept : subConcepts) {			
			subConcept.update();
		}
		
		for (Metric metric : currentlyGatheredMetrics) {
			String key = metric.getName();
			float totalValue = 0.0f;
			int valueCount = 0;	
			
			if (compareStats(metric)) {
				for (ibis.deploy.gui.gridvision.interfaces.IbisConcept subConcept : subConcepts) {								
					if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
						float newValue = subConcept.getNodeMetricsValue(key, AVG);
						
						if (newNodeMetricsAvgValues.containsKey(key)) {							
							if (newNodeMetricsMaxValues.get(key) < newValue) { 								
								newNodeMetricsMaxValues.put(key, newValue);
							}
							if (newNodeMetricsMinValues.get(key) > newValue) { 
								newNodeMetricsMinValues.put(key, newValue);
							}							
						} else {
							newNodeMetricsMaxValues.put(key, newValue);
							newNodeMetricsMinValues.put(key, newValue);
						}
						totalValue += newValue;
						valueCount++;
					} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {	
						for (IbisConcept linkedConcept : links) {
							
							//TODO fix
							
						}
						float newValue = subConcept.getLinkMetricsValue(key, AVG);
						if (newLinkMetricsAvgValues.containsKey(key)) {
						} else {
							newLinkMetricsMaxValues.put(key, newValue);
							newLinkMetricsMinValues.put(key, newValue);
						}					
					}
				}
				newNodeMetricsAvgValues.put(key, totalValue/valueCount);
			} else {
				throw new StatNotRequestedException();
				return;			
			}
		}
		
		synchronized(this) {			
			nodeMetricsValues = newNodeMetricsValues;
			linkMetricsValues = newLinkMetricsValues;
		}
	}

	@Override
	public HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> getLinkValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Float[]> getNodeMetricColors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Float[]> getLinkMetricColors() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean compareStats(Metric stat) {							
		for (ibis.deploy.gui.gridvision.interfaces.IbisConcept subConcept : subConcepts) {
			if (!subConcept.getCurrentlyGatheredMetrics().contains(stat)) return false;
		}		
		return true;
	}

}

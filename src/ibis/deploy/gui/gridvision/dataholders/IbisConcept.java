package ibis.deploy.gui.gridvision.dataholders;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.MetricsManager;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.deploy.gui.gridvision.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {
	protected MetricsManager mm;
	protected ManagementServiceInterface manInterface;
			
	protected HashMap<String, Float> nodeMetricsMaxValues;
	protected HashMap<String, Float> nodeMetricsAvgValues;
	protected HashMap<String, Float> nodeMetricsMinValues;
		
	protected HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> linkMetricsMaxValues;
	protected HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> linkMetricsAvgValues;
	protected HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> linkMetricsMinValues;
	
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	protected List<ibis.deploy.gui.gridvision.interfaces.IbisConcept> children;
	private List<ibis.deploy.gui.gridvision.interfaces.IbisConcept> links;
	
	protected MetricsList currentlyGatheredMetrics;
	
	private ibis.deploy.gui.gridvision.interfaces.IbisConcept parent;
	
	protected String name;

	public IbisConcept(MetricsManager mm, ibis.deploy.gui.gridvision.interfaces.IbisConcept parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics) {
		this.mm = mm;
		this.manInterface = manInterface;
		
		nodeMetricsMaxValues = new HashMap<String, Float>();
		nodeMetricsAvgValues = new HashMap<String, Float>();
		nodeMetricsMinValues = new HashMap<String, Float>();
		
		linkMetricsMaxValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		linkMetricsAvgValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		linkMetricsMinValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
		children = new ArrayList<ibis.deploy.gui.gridvision.interfaces.IbisConcept>();
		links = new ArrayList<ibis.deploy.gui.gridvision.interfaces.IbisConcept>();
		
		currentlyGatheredMetrics = new MetricsList();
		setCurrentlyGatheredMetrics(initialMetrics.clone());
		
		this.parent = parent;		
	}

	public float getNodeMetricsValue(String key, int mod) throws StatNotRequestedException, ModeUnknownException {
		synchronized(this) {
			if (currentlyGatheredMetrics.contains(key)) {
				if (nodeMetricsMaxValues.containsKey(key)) {
					if (mod == MAX) {
						return nodeMetricsMaxValues.get(key);
					} else if (mod == AVG) {
						return nodeMetricsAvgValues.get(key);
					} else if (mod == MIN) {
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
	}
	
	public float getLinkMetricsValue(ibis.deploy.gui.gridvision.interfaces.IbisConcept link, String key, int mod) throws StatNotRequestedException, ModeUnknownException {
		synchronized(this) {
			if (currentlyGatheredMetrics.contains(key)) {
				if (linkMetricsMaxValues.containsKey(link)) {
					if (mod == MAX) {
						return linkMetricsMaxValues.get(link).get(key);
					} else if (mod == AVG) {
						return linkMetricsAvgValues.get(link).get(key);
					} else if (mod == MIN) {
						return linkMetricsMinValues.get(link).get(key);
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
	}

	public ibis.deploy.gui.gridvision.interfaces.IbisConcept[] getChildren() {
		synchronized(this) {
			IbisConcept[] result = new IbisConcept[children.size()];
			children.toArray(result);
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

	public ArrayList<String> getMonitoredNodeMetrics() {
		synchronized(this) {
			ArrayList<String> copy = new ArrayList<String>();
			for (Metric metric: currentlyGatheredMetrics) {
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					copy.add(metric.getName());
				}
			}			
			return copy;
		}
	}

	public ArrayList<String> getMonitoredLinkMetrics() {
		synchronized(this) {
			ArrayList<String> copy = new ArrayList<String>();
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
		
		for (ibis.deploy.gui.gridvision.interfaces.IbisConcept concept : children) {			
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
		
		HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> newLinkMetricsMaxValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> newLinkMetricsAvgValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> newLinkMetricsMinValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		
		
		for (ibis.deploy.gui.gridvision.interfaces.IbisConcept child : children) {			
			child.update();
		}
		
		//Update the network map
		if (parent != null) {
			//First, determine the IbisConcepts at the same level
			ibis.deploy.gui.gridvision.interfaces.IbisConcept[] siblings = parent.getChildren();
			
			//Then, ask these siblings for their children, and make a map of the close family
			HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, ibis.deploy.gui.gridvision.interfaces.IbisConcept> nephewsToSiblings = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, ibis.deploy.gui.gridvision.interfaces.IbisConcept>(); 
			
			for (ibis.deploy.gui.gridvision.interfaces.IbisConcept sibling : siblings) {
				ibis.deploy.gui.gridvision.interfaces.IbisConcept[] nephews = sibling.getChildren();
				for (ibis.deploy.gui.gridvision.interfaces.IbisConcept nephew : nephews) {
					nephewsToSiblings.put(nephew, sibling);
				}
			}
			
			//Now, ask the children for their links, and match them to the nephews
			HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, HashSet<ibis.deploy.gui.gridvision.interfaces.IbisConcept>> linkedSiblings = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, HashSet<ibis.deploy.gui.gridvision.interfaces.IbisConcept>>();						
						
			for (ibis.deploy.gui.gridvision.interfaces.IbisConcept sibling : siblings) {
				HashSet<ibis.deploy.gui.gridvision.interfaces.IbisConcept> contributors = new HashSet<ibis.deploy.gui.gridvision.interfaces.IbisConcept>();
				for (ibis.deploy.gui.gridvision.interfaces.IbisConcept child : children) {
					for (ibis.deploy.gui.gridvision.interfaces.IbisConcept linkedNephew : child.getLinks()) {
						if (nephewsToSiblings.get(linkedNephew) == sibling) {									
							contributors.add(linkedNephew);
						}
					}								
				}
				if (!contributors.isEmpty()) {
					linkedSiblings.put(sibling,contributors);
				}
			}
			
			//Aggregate and save the metric values
			for (Metric metric : currentlyGatheredMetrics) {
				String key = metric.getName();
				float totalValue = 0.0f;
				int valueCount = 0;	
				
				if (compareStats(metric)) {
					for (ibis.deploy.gui.gridvision.interfaces.IbisConcept child : children) {								
						if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
							float newValue;
							try {
								newValue = child.getNodeMetricsValue(key, AVG);
								
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
								
							} catch (StatNotRequestedException e) {
								//Something went wrong. This exception is supposed to be impossible
								e.printStackTrace();
							} catch (ModeUnknownException e) {
								//Something went wrong. This exception is supposed to be impossible
								e.printStackTrace();
							}						
						} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {						
							for (Entry<ibis.deploy.gui.gridvision.interfaces.IbisConcept, HashSet<ibis.deploy.gui.gridvision.interfaces.IbisConcept>> linkedSibling : linkedSiblings.entrySet()) {
								float subtotal = 0.0f;
								int subcount = 0;
								
								for (ibis.deploy.gui.gridvision.interfaces.IbisConcept linkedNephew : linkedSibling.getValue()) {
									float newValue;
									try {
										newValue = linkedNephew.getLinkMetricsValue(child, key, AVG);
										
										if (newLinkMetricsMaxValues.get(linkedSibling).get(key) < newValue) {
											newLinkMetricsMaxValues.get(linkedSibling).put(key, newValue);
										}
										if (newLinkMetricsMinValues.get(linkedSibling).get(key) > newValue) {
											newLinkMetricsMinValues.get(linkedSibling).put(key, newValue);
										}
										
										subtotal += newValue;
										subcount++;
										
									} catch (StatNotRequestedException e) {
										//Something went wrong. This exception is supposed to be impossible
										e.printStackTrace();
									} catch (ModeUnknownException e) {
										//Something went wrong. This exception is supposed to be impossible
										e.printStackTrace();
									}								
								}
								newLinkMetricsAvgValues.get(linkedSibling).put(key, subtotal/subcount);														
							}											
						}
					}
					newNodeMetricsAvgValues.put(key, totalValue/valueCount);
					
				} else {
					throw new StatNotRequestedException();
				}
			}
			
			synchronized(this) {			
				nodeMetricsMaxValues = newNodeMetricsMaxValues;
				nodeMetricsAvgValues = newNodeMetricsAvgValues;
				nodeMetricsMinValues = newNodeMetricsMinValues;
				
				linkMetricsMaxValues = newLinkMetricsMaxValues;
				linkMetricsAvgValues = newLinkMetricsAvgValues;
				linkMetricsMinValues = newLinkMetricsMinValues;
			}
		}
	}
	
	public HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> getLinkValues(int mod) throws ModeUnknownException{
		HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> newLinkValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		if (mod == MAX) {
			newLinkValues.putAll(linkMetricsMaxValues);
		} else if (mod == AVG) {
			newLinkValues.putAll(linkMetricsAvgValues);
		} else if (mod == MIN) {
			newLinkValues.putAll(linkMetricsMinValues);
		} else {
			throw new ModeUnknownException();
		}		
		
		return newLinkValues;		
	}

	public HashMap<String, Float[]> getNodeMetricColors() {
		HashMap<String, Float[]> newNodeMetricsColors = new HashMap<String, Float[]>();
		newNodeMetricsColors.putAll(nodeMetricsColors);
		
		return newNodeMetricsColors;
	}

	public HashMap<String, Float[]> getLinkMetricColors() {
		HashMap<String, Float[]> newLinkMetricsColors = new HashMap<String, Float[]>();
		newLinkMetricsColors.putAll(linkMetricsColors);
		
		return newLinkMetricsColors;
	}
		
	public boolean isLowestConcept() {
		return !children.isEmpty();
	}
	
	public String getName() {
		return name;
	}
	
	private boolean compareStats(Metric stat) {							
		for (ibis.deploy.gui.gridvision.interfaces.IbisConcept subConcept : children) {
			if (!subConcept.getCurrentlyGatheredMetrics().contains(stat)) return false;
		}		
		return true;
	}
	

}

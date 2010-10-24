package ibis.deploy.gui.gridvision.impl;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.MetricsManager;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.deploy.gui.gridvision.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Flock implements ibis.deploy.gui.gridvision.Flock {
	protected MetricsManager mm;
	protected ManagementServiceInterface manInterface;
	protected RegistryServiceInterface regInterface;
			
	protected HashMap<String, Float> nodeMetricsMaxValues;
	protected HashMap<String, Float> nodeMetricsAvgValues;
	protected HashMap<String, Float> nodeMetricsMinValues;
		
	protected HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>> linkMetricsMaxValues;
	protected HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>> linkMetricsAvgValues;
	protected HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>> linkMetricsMinValues;
	
	private HashMap<String, Float[]> nodeMetricsColors;
	private HashMap<String, Float[]> linkMetricsColors;
	
	protected List<ibis.deploy.gui.gridvision.Flock> children;
	private   List<ibis.deploy.gui.gridvision.Flock> links;
	
	protected MetricsList currentlyGatheredMetrics;
	
	private ibis.deploy.gui.gridvision.Flock parent;
	private Location myLocation;
	
	protected String name;

	public Flock(MetricsManager mm, ibis.deploy.gui.gridvision.Flock parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics) {
		this.mm = mm;
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		
		nodeMetricsMaxValues = new HashMap<String, Float>();
		nodeMetricsAvgValues = new HashMap<String, Float>();
		nodeMetricsMinValues = new HashMap<String, Float>();
		
		linkMetricsMaxValues = new HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>>();
		linkMetricsAvgValues = new HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>>();
		linkMetricsMinValues = new HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>>();
		
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsColors = new HashMap<String, Float[]>();
		
		children = new ArrayList<ibis.deploy.gui.gridvision.Flock>();
		links    = new ArrayList<ibis.deploy.gui.gridvision.Flock>();
		
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
	
	public float getLinkMetricsValue(ibis.deploy.gui.gridvision.Flock link, String key, int mod) throws StatNotRequestedException, ModeUnknownException {
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

	public ibis.deploy.gui.gridvision.Flock[] getChildren() {
		synchronized(this) {
			Flock[] result = new Flock[children.size()];
			children.toArray(result);
			return result;
		}
	}
	
	public ibis.deploy.gui.gridvision.Flock[] getLinks() {
		synchronized(this) {
			Flock[] result = new Flock[links.size()];
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
		
		for (ibis.deploy.gui.gridvision.Flock child : children) {			
			child.setCurrentlyGatheredMetrics(newMetrics);
		}		
	}
	
	public MetricsList getCurrentlyGatheredMetrics() {
		return currentlyGatheredMetrics;
	}

	public void update() throws NoSuitableModuleException, StatNotRequestedException {		
		HashMap<String, Float> newNodeMetricsMaxValues = new HashMap<String, Float>();
		HashMap<String, Float> newNodeMetricsAvgValues = new HashMap<String, Float>();
		HashMap<String, Float> newNodeMetricsMinValues = new HashMap<String, Float>();
		
		HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>> newLinkMetricsMaxValues = new HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>>();
		HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>> newLinkMetricsAvgValues = new HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>>();
		HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>> newLinkMetricsMinValues = new HashMap<ibis.deploy.gui.gridvision.Flock, Map<String, Float>>();
		
		
		for (ibis.deploy.gui.gridvision.Flock child : children) {			
			child.update();
		}
		
		//Update the network map
		if (parent != null) {
			//First, determine the Flocks at the same level
			ibis.deploy.gui.gridvision.Flock[] siblings = parent.getChildren();
			
			//Then, ask these siblings for their children, and make a map of the close family
			HashMap<ibis.deploy.gui.gridvision.Flock, ibis.deploy.gui.gridvision.Flock> nephewsToSiblings = new HashMap<ibis.deploy.gui.gridvision.Flock, ibis.deploy.gui.gridvision.Flock>(); 
			
			for (ibis.deploy.gui.gridvision.Flock sibling : siblings) {
				ibis.deploy.gui.gridvision.Flock[] nephews = sibling.getChildren();
				for (ibis.deploy.gui.gridvision.Flock nephew : nephews) {
					nephewsToSiblings.put(nephew, sibling);
				}
			}
			
			//Now, ask the children for their links, and match them to the nephews
			HashMap<ibis.deploy.gui.gridvision.Flock, HashSet<ibis.deploy.gui.gridvision.Flock>> linkedSiblings = new HashMap<ibis.deploy.gui.gridvision.Flock, HashSet<ibis.deploy.gui.gridvision.Flock>>();						
						
			for (ibis.deploy.gui.gridvision.Flock sibling : siblings) {
				HashSet<ibis.deploy.gui.gridvision.Flock> contributors = new HashSet<ibis.deploy.gui.gridvision.Flock>();
				for (ibis.deploy.gui.gridvision.Flock child : children) {
					for (ibis.deploy.gui.gridvision.Flock linkedNephew : child.getLinks()) {
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
					for (ibis.deploy.gui.gridvision.Flock child : children) {								
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
							for (Entry<ibis.deploy.gui.gridvision.Flock, HashSet<ibis.deploy.gui.gridvision.Flock>> linkedSibling : linkedSiblings.entrySet()) {
								float subtotal = 0.0f;
								int subcount = 0;
								
								for (ibis.deploy.gui.gridvision.Flock linkedNephew : linkedSibling.getValue()) {
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
		
	public boolean isLeaf() {
		return !children.isEmpty();
	}
	
	public String getName() {
		return name;
	}

	public ibis.deploy.gui.gridvision.Flock addLeaf(IbisIdentifier ii) {
		Location ibisLocation = ii.location();
		ibisLocation.
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLeaf(ibis.deploy.gui.gridvision.Flock flockToRemove) {
		// TODO Auto-generated method stub
		
	}	
	
	
	private boolean compareStats(Metric stat) {							
		for (ibis.deploy.gui.gridvision.Flock child : children) {
			if (!child.getCurrentlyGatheredMetrics().contains(stat)) return false;
		}		
		return true;
	}
	

}

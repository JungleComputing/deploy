package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.MetricDescription.MetricType;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.MetricNotAvailableException;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;

/**
 * A representation of a location (Node, Site) in the data gathering universe
 * @author Maarten van Meersbergen
 */
public class Location extends Element implements ibis.deploy.monitoring.collection.Location {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Location");
	
	private String name;
	private Float[] color;
	
	private ArrayList<ibis.deploy.monitoring.collection.Ibis> ibises;
	private ArrayList<ibis.deploy.monitoring.collection.Location> children;
	
	public Location(String name, Float[] color) {
		super();
		this.name = name;
		this.color = new Float[3];
		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];
		
		ibises = new ArrayList<ibis.deploy.monitoring.collection.Ibis>();
		children = new ArrayList<ibis.deploy.monitoring.collection.Location>();
	}
	
	//Getters
	public String getName() {
		return name;
	}
	
	public ibis.deploy.monitoring.collection.Metric[] getMetrics() {
		ArrayList<ibis.deploy.monitoring.collection.Metric> result = new ArrayList<ibis.deploy.monitoring.collection.Metric>();
		for (ibis.deploy.monitoring.collection.Metric metric : metrics.values()) {
			if (metric.getDescription().getType() == MetricType.NODE) {
				result.add(metric);
			}
		}		
		return result.toArray(new ibis.deploy.monitoring.collection.Metric[0]);
	}
	
	public Float[] getColor() {
		return color;
	}
	
	public ArrayList<ibis.deploy.monitoring.collection.Ibis> getIbises() {
		return ibises;
	}
	
	public ArrayList<ibis.deploy.monitoring.collection.Ibis> getAllIbises() {
		ArrayList<ibis.deploy.monitoring.collection.Ibis> result = new ArrayList<ibis.deploy.monitoring.collection.Ibis>();
		result.addAll(ibises);
		
		for (ibis.deploy.monitoring.collection.Location child : children) {
			result.addAll(child.getAllIbises());
		}
		
		return result;
	}
	
	public ArrayList<ibis.deploy.monitoring.collection.Location> getChildren() {
		return children;
	}
	
	/*
	public Number getReducedValue(Reducefunction function, ibis.deploy.monitoring.collection.MetricDescription metric, MetricOutput outputmethod) {
		if (outputmethod == MetricOutput.N) {
			int result = 0;
			
			if (function == Reducefunction.LOCATIONSPECIFIC_MINIMUM) {
				result = 10000000;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					int metricvalue;
					try {
						metricvalue = (Integer) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue < result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
			} else if (function == Reducefunction.LOCATIONSPECIFIC_MAXIMUM) {
				result = 0;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					int metricvalue;
					try {
						metricvalue = (Integer) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue > result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
			} else if (function == Reducefunction.LOCATIONSPECIFIC_AVERAGE) {
				result = 0;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					int metricvalue;
					try {
						metricvalue = (Integer) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						result += metricvalue;	
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}				
				}
				if (ibises.size() > 0) {
					result = result / ibises.size();
				}
			} else if (function == Reducefunction.ALLDESCENDANTS_MINIMUM) {
				result = 10000000;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					int metricvalue;
					try {
						metricvalue = (Integer) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue < result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
				
				for (ibis.deploy.monitoring.collection.Location child : children) {
					int childvalue = (Integer) child.getReducedValue(function, metric, outputmethod);
					if (childvalue < result) {
						result = childvalue;
					}
				}
			} else if (function == Reducefunction.ALLDESCENDANTS_MAXIMUM) {
				result = 0;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					int metricvalue;
					try {
						metricvalue = (Integer) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue > result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
				
				for (ibis.deploy.monitoring.collection.Location child : children) {
					int childvalue = (Integer) child.getReducedValue(function, metric, outputmethod);
					if (childvalue > result) {
						result = childvalue;
					}
				}
			} else if (function == Reducefunction.ALLDESCENDANTS_AVERAGE) {
				result = 0;
				int numberOfIbises = ibises.size();
				
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					int metricvalue;
					try {
						metricvalue = (Integer) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						result += metricvalue;	
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}				
				}
								
				for (ibis.deploy.monitoring.collection.Location child : children) {
					result += (Integer) child.getReducedValue(function, metric, outputmethod) * child.getNumberOfDescendants();
					numberOfIbises += child.getNumberOfDescendants();
				}
				
				if (ibises.size() > 0) {
					result = result / numberOfIbises;
				}
			}
			return result;
		} else { //Any other metric is defined as a float
			float result = 0f;
			
			if (function == Reducefunction.LOCATIONSPECIFIC_MINIMUM) {
				result = 10000000f;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					float metricvalue;
					try {
						metricvalue = (Float) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue < result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
			} else if (function == Reducefunction.LOCATIONSPECIFIC_MAXIMUM) {
				result = 0f;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					float metricvalue;
					try {
						metricvalue = (Float) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue > result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
			} else if (function == Reducefunction.LOCATIONSPECIFIC_AVERAGE) {
				result = 0f;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					float metricvalue;
					try {
						metricvalue = (Float) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						result += metricvalue;	
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}				
				}
				if (ibises.size() > 0) {
					result = result / ibises.size();
				}
			} else if (function == Reducefunction.ALLDESCENDANTS_MINIMUM) {
				result = 10000000f;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					float metricvalue;
					try {
						metricvalue = (Float) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue < result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
				
				for (ibis.deploy.monitoring.collection.Location child : children) {
					float childvalue = (Float) child.getReducedValue(function, metric, outputmethod);
					if (childvalue < result) {
						result = childvalue;
					}
				}
			} else if (function == Reducefunction.ALLDESCENDANTS_MAXIMUM) {
				result = 0f;
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					float metricvalue;
					try {
						metricvalue = (Float) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						if (metricvalue > result) {
							result = metricvalue;
						}
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}					
				}
				
				for (ibis.deploy.monitoring.collection.Location child : children) {
					float childvalue = (Float) child.getReducedValue(function, metric, outputmethod);
					if (childvalue > result) {
						result = childvalue;
					}
				}
			} else if (function == Reducefunction.ALLDESCENDANTS_AVERAGE) {
				result = 0f;
				int numberOfIbises = ibises.size();
				
				for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
					float metricvalue;
					try {
						metricvalue = (Float) ibis.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
						result += metricvalue;	
					} catch (OutputUnavailableException e) {
						logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
					}				
				}
								
				for (ibis.deploy.monitoring.collection.Location child : children) {
					result += (Float) child.getReducedValue(function, metric, outputmethod) * child.getNumberOfDescendants();
					numberOfIbises += child.getNumberOfDescendants();
				}
				
				if (ibises.size() > 0) {
					result = result / numberOfIbises;
				}
			}
			return result;
		}
	}
	*/
	public ArrayList<ibis.deploy.monitoring.collection.Link> getLinks(ibis.deploy.monitoring.collection.MetricDescription metric, MetricOutput outputmethod, float minimumValue, float maximumValue) {
		ArrayList<ibis.deploy.monitoring.collection.Link> result = new ArrayList<ibis.deploy.monitoring.collection.Link>();
		
		if (outputmethod == MetricOutput.N) {
			for (Entry<Element, ibis.deploy.monitoring.collection.Link> entry : links.entrySet()) {
				Link link = ((Link)entry.getValue());
				int linkvalue;
				try {
					linkvalue = (Integer) link.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
					if (linkvalue >= minimumValue && linkvalue <= maximumValue ) {
						result.add(link);
					}
				} catch (OutputUnavailableException e) {
					logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
				} catch (MetricNotAvailableException e) {					
					logger.error("The impossible MetricNotAvailableException just happened anyway.");
				}			
			}			
		} else {
			for (Entry<Element, ibis.deploy.monitoring.collection.Link> entry : links.entrySet()) {
				Link link = ((Link)entry.getValue());
				float linkvalue;
				try {
					linkvalue = (Float) link.getMetric(metric).getValue(MetricModifier.NORM, outputmethod);
					if (linkvalue >= minimumValue && linkvalue <= maximumValue ) {
						result.add(link);
					}
				} catch (OutputUnavailableException e) {
					logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
				} catch (MetricNotAvailableException e) {					
					logger.error("The impossible MetricNotAvailableException just happened anyway.");
				}				
			}	
		}	
		return result;
	}
	
	public int getNumberOfDescendants() {
		int result = ibises.size();
		
		for (ibis.deploy.monitoring.collection.Location child : children) {
			result += ((Location)child).getNumberOfDescendants();
		}
		
		return result;
	} 
		
	//Setters
	public void addIbis(ibis.deploy.monitoring.collection.Ibis ibis) {
		ibises.add(ibis);
	}
	
	public void removeIbis(ibis.deploy.monitoring.collection.Ibis ibis) {
		ibises.remove(ibis);
	}
	
	public void addChild(ibis.deploy.monitoring.collection.Location location) {
		if (!children.contains(location)) {
			children.add(location);
		}
	}
	
	public void removeChild(ibis.deploy.monitoring.collection.Location location) {
		children.remove(location);
	}
		
	public void setMetrics(Set<ibis.deploy.monitoring.collection.MetricDescription> descriptions) {
		for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
			((Ibis)ibis).setMetrics(descriptions);
		}
		for (ibis.deploy.monitoring.collection.Location child : children) {
			((Location)child).setMetrics(descriptions);
		}
		for (ibis.deploy.monitoring.collection.Link link : links.values()) {
			((Link)link).setMetrics(descriptions);
		}
		
		//add new metrics
		for (ibis.deploy.monitoring.collection.MetricDescription md : descriptions) {
			if(!metrics.containsKey(md)) {
				Metric newMetric = (Metric) ((MetricDescription)md).getMetric(this); 
				metrics.put(md, newMetric);
			}
		}
		
		//make a snapshot of our current metrics.
		Set<ibis.deploy.monitoring.collection.MetricDescription> temp = new HashSet<ibis.deploy.monitoring.collection.MetricDescription>();		
		temp.addAll(metrics.keySet());
		
		//and loop through the snapshot to remove unwanted metrics that don't appear in the new set
		for (ibis.deploy.monitoring.collection.MetricDescription entry : temp) {
			if(!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}
	
	public void makeLinkHierarchy() {
		for (ibis.deploy.monitoring.collection.Link link : links.values()) {
			ibis.deploy.monitoring.collection.Location source = (ibis.deploy.monitoring.collection.Location) link.getSource();
			ibis.deploy.monitoring.collection.Location destination = (ibis.deploy.monitoring.collection.Location) link.getDestination();
			
			for (ibis.deploy.monitoring.collection.Location sourceChild : source.getChildren()) {
				for (ibis.deploy.monitoring.collection.Location destinationChild : destination.getChildren()) {
					ibis.deploy.monitoring.collection.Link childLink;					
					try {
						childLink = sourceChild.getLink(destinationChild);
						((Link)link).addChild(childLink);
					} catch (SelfLinkeageException ignored) {
						//ignored, because we do not want this link
					}				
				}
			}
		}
		for (ibis.deploy.monitoring.collection.Location child : children) {
			((Location)child).makeLinkHierarchy();
		}			
	}
	
	public void update() {
		//make sure the children are updated first
		for (ibis.deploy.monitoring.collection.Location child : children) {			
			((Location)child).update();
		}
		
		for (Entry<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric> data : metrics.entrySet()) {
			ibis.deploy.monitoring.collection.MetricDescription desc = data.getKey();
			Metric metric = (Metric)data.getValue();
		
			try {				
				ArrayList<MetricOutput> types = desc.getOutputTypes();
				
				for (MetricOutput outputtype : types) {
					if (outputtype == MetricOutput.PERCENT || outputtype == MetricOutput.R || outputtype == MetricOutput.RPOS) {
						float total = 0f, max = -10000000f, min = 10000000f;
						
						//First, we gather our own metrics
						for (ibis.deploy.monitoring.collection.Ibis entry : ibises) {
							Ibis ibis = (Ibis)entry;
							Metric ibisMetric = (Metric)ibis.getMetric(desc);							
							float ibisValue = (Float) ibisMetric.getValue(MetricModifier.NORM, outputtype);
							
							total += ibisValue ;
							
							if (ibisValue > max) max = ibisValue;
							if (ibisValue < min) min = ibisValue;
						}						
						
						if (outputtype == MetricOutput.PERCENT) {
							//Then we add the metric values of our child locations, 
							//multiplied by their weight.
							int childIbises = 0;
							for (ibis.deploy.monitoring.collection.Location child : children) {
								float childValue = (Float)child.getMetric(desc).getValue(MetricModifier.NORM, outputtype);
								
								childIbises += ((Location)child).getNumberOfDescendants();
								
								total += childValue * ((Location)child).getNumberOfDescendants();
								
								if (childValue > max) max = childValue;								
								if (childValue < min) min = childValue;
							}
							metric.setValue(MetricModifier.NORM, outputtype, total/(ibises.size()+childIbises));
							metric.setValue(MetricModifier.MAX, outputtype, max);
							metric.setValue(MetricModifier.MIN, outputtype, min);
						} else {							
							//Then we add the metric values of our child locations					
							for (ibis.deploy.monitoring.collection.Location child : children) {
								float childValue = (Float)child.getMetric(desc).getValue(MetricModifier.NORM, outputtype);
								
								total += childValue;
								
								if (childValue > max) max = childValue;								
								if (childValue < min) min = childValue;
							}
							metric.setValue(MetricModifier.NORM, outputtype, total);
							metric.setValue(MetricModifier.MAX, outputtype, max);
							metric.setValue(MetricModifier.MIN, outputtype, min);
						}						
					} else { //We are MetricOutput.N
						long total  = 0, max = 0, min = 1000000;
						
						//First, we gather our own metrics
						for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {							
							long ibisValue = (Long) ibis.getMetric(desc).getValue(MetricModifier.NORM, outputtype);
							
							total += ibisValue ;
							
							if (ibisValue > max) max = ibisValue;
							if (ibisValue < min) min = ibisValue;
						}
						
						//Then we add the metric values of our child locations					
						for (ibis.deploy.monitoring.collection.Location child : children) {
							long childValue = (Long) child.getMetric(desc).getValue(MetricModifier.NORM, outputtype);
							
							total += childValue;
							
							if (childValue > max) max = childValue;								
							if (childValue < min) min = childValue;
						}
						metric.setValue(MetricModifier.NORM, outputtype, total);
						metric.setValue(MetricModifier.MAX, outputtype, max);
						metric.setValue(MetricModifier.MIN, outputtype, min);
					}
				}				
			} catch (OutputUnavailableException impossible) {
				//Impossible since we tested if it was available first.
				logger.error("The impossible OutputUnavailableException just happened anyway.");
			} catch (BeyondAllowedRangeException e) {
				//Impossible unless one of the children has a value that is already bad
				logger.error("The impossible BeyondAllowedRangeException just happened anyway.");
			} catch (MetricNotAvailableException e) {					
				logger.error("The impossible MetricNotAvailableException just happened anyway.");
			}
		}
	}
	
	public String debugPrint() {
		String result = "";
		result += name + " has "+children.size()+" children. \n" ;
		result += name + " has "+links.size()+" links. \n" ;
		result += name + " has "+ibises.size()+" ibises. \n" ;
		
		result += name + " has "+metrics.size()+" metrics: ";
		
		for (Entry<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric> entry : metrics.entrySet()) {
			result += "  " + entry.getValue().getDescription().getName();
		}
		
		result += "\n";
		
		for (ibis.deploy.monitoring.collection.Link link : links.values()) {
			result += name + " "+((Link)link).debugPrint();
		}
		
		result += "\n";
		
		for (ibis.deploy.monitoring.collection.Ibis ibis : ibises) {
			result += name + " "+((Ibis)ibis).debugPrint();
		}
		
		result += "\n";
		
		for (ibis.deploy.monitoring.collection.Location child : children) {
			result += ((Location)child).debugPrint();
		}
		return result;
	}
		
}
package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.MetricDescription.MetricType;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.MetricNotAvailableException;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;

/**
 * An interface for a link between two elements that exist within the managed
 * universe.
 * 
 * @author Maarten van Meersbergen
 */
public class LinkImpl extends ElementImpl implements Link {
	private static final Logger logger = LoggerFactory.getLogger("impl.Link");

	private ArrayList<Link> children;
	private ElementImpl source;
	private ElementImpl destination;

	public LinkImpl(ElementImpl origin, Element destination) {
		super();
		this.source = origin;
		this.destination = (ElementImpl) destination;

		children = new ArrayList<Link>();
	}

	public Metric[] getMetrics() {
		ArrayList<Metric> result = new ArrayList<Metric>();
		for (Metric metric : metrics.values()) {
			if (metric.getDescription().getType() == MetricType.LINK) {
				result.add(metric);
			}
		}
		return result.toArray(new Metric[0]);
	}

	public ElementImpl getSource() {
		return source;
	}

	public ElementImpl getDestination() {
		return destination;
	}

	public int getNumberOfDescendants() {
		int result = 1;

		for (Link child : children) {
			result += ((LinkImpl) child).getNumberOfDescendants();
		}

		return result;
	}

	public void addChild(Link newChild) {
		if (!children.contains(newChild)) {
			children.add(newChild);
		}
	}

	public void update() {
		// First update all of our children (lower locations, ibises)
		for (Link child : children) {
			((LinkImpl) child).update();
		}		
		
		if (source instanceof IbisImpl && destination instanceof IbisImpl) {
			updateIbisIbisLink();
		} else if (source instanceof LocationImpl && destination instanceof LocationImpl) {
			updateLocationLocationLink();
		}
		
	}

	private void updateIbisIbisLink() {
		//Transform the Link value maps into 'normal' metrics
		for (Entry<MetricDescription, Metric> data : metrics.entrySet()) {
			MetricDescription desc = data.getKey();
			MetricImpl metric = (MetricImpl) data.getValue();
			
			ArrayList<MetricOutput> types = desc.getOutputTypes();			
			for (MetricOutput outputtype : types) {
				try {
					// First, we gather our own metrics
					MetricImpl srcMetric = (MetricImpl) source.getMetric(desc);
					MetricImpl dstMetric = (MetricImpl) destination.getMetric(desc);

					Number srcLinkValue = srcMetric.getValue(MetricModifier.NORM, outputtype, destination);
					Number dstLinkValue = dstMetric.getValue(MetricModifier.NORM, outputtype, source);
					
					if (outputtype == MetricOutput.PERCENT || outputtype == MetricOutput.R || outputtype == MetricOutput.RPOS) {
						float total = 0f, max = -10000000f, min = 10000000f;

						float srcValue = srcLinkValue.floatValue();
						float dstValue = dstLinkValue.floatValue();
						
						// TODO find a new function for this
						total += srcValue + dstValue;

						if (srcValue > max)
							max = srcValue;
						if (srcValue < min)
							min = srcValue;

						if (dstValue > max)
							max = dstValue;
						if (dstValue < min)
							min = dstValue;
						
						metric.setValue(MetricModifier.NORM, outputtype, total);
						metric.setValue(MetricModifier.MAX, outputtype, max);
						metric.setValue(MetricModifier.MIN, outputtype, min);						
					} else { // We are MetricOutput.N
						long total = 0, max = 0, min = 1000000;

						long srcValue = srcLinkValue.longValue();
						long dstValue = dstLinkValue.longValue();

						// TODO find a new function for this
						total += srcValue + dstValue;

						if (srcValue > max)
							max = srcValue;
						if (srcValue < min)
							min = srcValue;

						if (dstValue > max)
							max = dstValue;
						if (dstValue < min)
							min = dstValue;		
						
						metric.setValue(MetricModifier.NORM, outputtype, total);
						metric.setValue(MetricModifier.MAX, outputtype, max);
						metric.setValue(MetricModifier.MIN, outputtype, min);
					}					
				} catch (OutputUnavailableException impossible) {
					// Impossible since we tested if it was available first.
					logger.error("The impossible OutputUnavailableException just happened anyway.");
				} catch (BeyondAllowedRangeException e) {
					// Impossible unless one of the children has a value that is
					// already bad
					logger.error("The impossible BeyondAllowedRangeException just happened anyway.");
				} catch (MetricNotAvailableException e) {
					logger.error("The impossible MetricNotAvailableException just happened anyway.");
				}
			}
		}
	}

	private void updateLocationLocationLink() {
		for (Entry<MetricDescription, Metric> data : metrics.entrySet()) {
			MetricDescription desc = data.getKey();
			MetricImpl metric = (MetricImpl) data.getValue();

			try {
				ArrayList<MetricOutput> types = desc.getOutputTypes();
	
				for (MetricOutput outputtype : types) {
					if (outputtype == MetricOutput.PERCENT || outputtype == MetricOutput.R || outputtype == MetricOutput.RPOS) {
						float total = 0f, max = -10000000f, min = 10000000f;
	
						if (outputtype == MetricOutput.PERCENT) {
							// We add up the metric values of our child locations,
							// multiplied by their weight.
							int childLinks = 0;
							for (Link child : children) {
								float childValue = (Float) child.getMetric(desc).getValue(MetricModifier.NORM, outputtype);
	
								childLinks += ((LinkImpl) child).getNumberOfDescendants();
	
								total += childValue * ((LinkImpl) child).getNumberOfDescendants();
	
								if (childValue > max)
									max = childValue;
								if (childValue < min)
									min = childValue;
							}
							metric.setValue(MetricModifier.NORM, outputtype,
									total / (ibises.size() + childIbises));
							metric.setValue(MetricModifier.MAX, outputtype, max);
							metric.setValue(MetricModifier.MIN, outputtype, min);
						} else {
							// Then we add the metric values of our child
							// locations
							for (Location child : children) {
								float childValue = (Float) child
										.getMetric(desc)
										.getValue(MetricModifier.NORM,
												outputtype);
	
								total += childValue;
	
								if (childValue > max)
									max = childValue;
								if (childValue < min)
									min = childValue;
							}
							metric.setValue(MetricModifier.NORM, outputtype,
									total);
							metric.setValue(MetricModifier.MAX, outputtype, max);
							metric.setValue(MetricModifier.MIN, outputtype, min);
						}
					} else { // We are MetricOutput.N
						long total = 0, max = 0, min = 1000000;
	
						// First, we gather our own metrics
						for (Ibis ibis : ibises) {
							long ibisValue = (Long) ibis.getMetric(desc)
									.getValue(MetricModifier.NORM, outputtype);
	
							total += ibisValue;
	
							if (ibisValue > max)
								max = ibisValue;
							if (ibisValue < min)
								min = ibisValue;
						}
	
						// Then we add the metric values of our child locations
						for (Location child : children) {
							long childValue = (Long) child.getMetric(desc)
									.getValue(MetricModifier.NORM, outputtype);
	
							total += childValue;
	
							if (childValue > max)
								max = childValue;
							if (childValue < min)
								min = childValue;
						}
						metric.setValue(MetricModifier.NORM, outputtype, total);
						metric.setValue(MetricModifier.MAX, outputtype, max);
						metric.setValue(MetricModifier.MIN, outputtype, min);
					}
				}
			} catch (OutputUnavailableException impossible) {
				// Impossible since we tested if it was available first.
				logger.error("The impossible OutputUnavailableException just happened anyway.");
			} catch (BeyondAllowedRangeException e) {
				// Impossible unless one of the children has a value that is
				// already bad
				logger.error("The impossible BeyondAllowedRangeException just happened anyway.");
			} catch (MetricNotAvailableException e) {
				logger.error("The impossible MetricNotAvailableException just happened anyway.");
			}	
		}
	}

	public void setMetrics(Set<MetricDescription> descriptions) {
		// add new metrics
		for (MetricDescription md : descriptions) {
			if (!metrics.containsKey(md) && md.getType() == MetricType.LINK) {
				MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) md)
						.getMetric(this);
				metrics.put(md, newMetric);
			}
		}

		// make a snapshot of our current metrics.
		Set<MetricDescription> temp = new HashSet<MetricDescription>();
		temp.addAll(metrics.keySet());

		// and loop through the snapshot to remove unwanted metrics
		for (MetricDescription entry : temp) {
			if (!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}

	@Override
	public boolean equals(Object thatObject) {
		if (this == thatObject)
			return true;
		if (!(thatObject instanceof LinkImpl))
			return false;

		// cast to native object is now safe
		LinkImpl that = (LinkImpl) thatObject;

		// now a proper field-by-field evaluation can be made
		return (this.source.equals(that.source) && this.destination
				.equals(that.destination))
				|| (this.source.equals(that.destination) && this.destination
						.equals(that.source));
	}

	@Override
	public int hashCode() {
		int hashCode = source.hashCode() + destination.hashCode();
		return hashCode;
	}

	public String debugPrint() {
		String result = "";

		result += "link: bla" + "->" + " ";

		result += "has " + metrics.size() + " metrics: ";

		result += "\n";

		for (Link child : children) {
			result += "  " + ((LinkImpl) child).debugPrint();
		}

		return result;
	}
}

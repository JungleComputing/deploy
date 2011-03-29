package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;

/**
 * A representation of a location (Node, Site) in the data gathering universe
 * 
 * @author Maarten van Meersbergen
 */
public class LocationImpl extends ElementImpl implements Location {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Location");

	private String name;
	private Float[] color;

	private ArrayList<Ibis> ibises;
	private ArrayList<Location> children;

	public LocationImpl(String name, Float[] color) {
		super();
		this.name = name;
		this.color = new Float[3];
		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];

		ibises = new ArrayList<Ibis>();
		children = new ArrayList<Location>();
	}

	// Getters
	public String getName() {
		return name;
	}

	public Metric[] getMetrics() {
		ArrayList<Metric> result = new ArrayList<Metric>();
		for (Metric metric : metrics.values()) {
			if (metric.getDescription().getType() == MetricType.NODE) {
				result.add(metric);
			}
		}
		return result.toArray(new Metric[0]);
	}

	public Float[] getColor() {
		return color;
	}

	public ArrayList<Ibis> getIbises() {
		return ibises;
	}

	public ArrayList<Ibis> getAllIbises() {
		ArrayList<Ibis> result = new ArrayList<Ibis>();
		result.addAll(ibises);

		for (Location child : children) {
			result.addAll(child.getAllIbises());
		}

		return result;
	}

	public ArrayList<Location> getChildren() {
		return children;
	}

	public ArrayList<Link> getLinks(MetricDescription metric,
			MetricOutput outputmethod, float minimumValue, float maximumValue) {
		ArrayList<Link> result = new ArrayList<Link>();

		if (outputmethod == MetricOutput.N) {
			for (Entry<ElementImpl, Link> entry : links.entrySet()) {
				LinkImpl link = ((LinkImpl) entry.getValue());
				int linkvalue;
				try {
					linkvalue = (Integer) link.getMetric(metric).getValue(
							MetricModifier.NORM, outputmethod);
					if (linkvalue >= minimumValue && linkvalue <= maximumValue) {
						result.add(link);
					}
				} catch (OutputUnavailableException e) {
					logger.debug("OutputUnavailableException caught. Metric is probably undefined.");
				} catch (MetricNotAvailableException e) {
					logger.error("The impossible MetricNotAvailableException just happened anyway.");
				}
			}
		} else {
			for (Entry<ElementImpl, Link> entry : links.entrySet()) {
				LinkImpl link = ((LinkImpl) entry.getValue());
				float linkvalue;
				try {
					linkvalue = (Float) link.getMetric(metric).getValue(
							MetricModifier.NORM, outputmethod);
					if (linkvalue >= minimumValue && linkvalue <= maximumValue) {
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

		for (Location child : children) {
			result += ((LocationImpl) child).getNumberOfDescendants();
		}

		return result;
	}

	// Setters
	public void addIbis(Ibis ibis) {
		ibises.add(ibis);
	}

	public void removeIbis(Ibis ibis) {
		ibises.remove(ibis);
	}

	public void addChild(Location location) {
		if (!children.contains(location)) {
			children.add(location);
		}
	}

	public void removeChild(Location location) {
		children.remove(location);
	}

	public void setMetrics(Set<MetricDescription> descriptions) {
		for (Ibis ibis : ibises) {
			((IbisImpl) ibis).setMetrics(descriptions);
		}
		for (Location child : children) {
			((LocationImpl) child).setMetrics(descriptions);
		}
		for (Link link : links.values()) {
			((LinkImpl) link).setMetrics(descriptions);
		}

		// add new metrics
		for (MetricDescription md : descriptions) {
			if (!metrics.containsKey(md)) {
				MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) md)
						.getMetric(this);
				metrics.put(md, newMetric);
			}
		}

		// make a snapshot of our current metrics.
		Set<MetricDescription> temp = new HashSet<MetricDescription>();
		temp.addAll(metrics.keySet());

		// and loop through the snapshot to remove unwanted metrics that don't
		// appear in the new set
		for (MetricDescription entry : temp) {
			if (!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}

	public void makeLinkHierarchy() {
		for (Link link : links.values()) {
			LocationImpl source = (LocationImpl) link.getSource();
			LocationImpl destination = (LocationImpl) link.getDestination();

			for (Location sourceChild : source.getChildren()) {
				for (Location destinationChild : destination.getChildren()) {
					Link childLink;
					try {
						childLink = sourceChild.getLink(destinationChild);
						((LinkImpl) link).addChild(childLink);
					} catch (SelfLinkeageException ignored) {
						// ignored, because we do not want this link
					}
				}
			}
		}
		for (Location child : children) {
			((LocationImpl) child).makeLinkHierarchy();
		}
	}

	public void update() {
		// make sure the children are updated first
		for (Location child : children) {
			((LocationImpl) child).update();
		}

		for (Entry<MetricDescription, Metric> data : metrics.entrySet()) {
			MetricDescription desc = data.getKey();
			MetricImpl metric = (MetricImpl) data.getValue();

			try {
				ArrayList<MetricOutput> types = desc.getOutputTypes();

				for (MetricOutput outputtype : types) {
					if (outputtype == MetricOutput.PERCENT
							|| outputtype == MetricOutput.R
							|| outputtype == MetricOutput.RPOS) {
						float total = 0f, max = -10000000f, min = 10000000f;

						// First, we gather our own metrics
						for (Ibis entry : ibises) {
							IbisImpl ibis = (IbisImpl) entry;
							MetricImpl ibisMetric = (MetricImpl) ibis
									.getMetric(desc);
							float ibisValue = (Float) ibisMetric.getValue(
									MetricModifier.NORM, outputtype);

							total += ibisValue;

							if (ibisValue > max)
								max = ibisValue;
							if (ibisValue < min)
								min = ibisValue;
						}

						if (outputtype == MetricOutput.PERCENT) {
							// Then we add the metric values of our child
							// locations,
							// multiplied by their weight.
							int childIbises = 0;
							for (Location child : children) {
								float childValue = (Float) child
										.getMetric(desc)
										.getValue(MetricModifier.NORM,
												outputtype);

								childIbises += ((LocationImpl) child)
										.getNumberOfDescendants();

								total += childValue
										* ((LocationImpl) child)
												.getNumberOfDescendants();

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

		for (Link link : links.values()) {
			((LinkImpl) link).update();
		}
	}	

	public String debugPrint() {
		String result = "";
		result += name + " has " + children.size() + " children. \n";
		result += name + " has " + links.size() + " links. \n";
		result += name + " has " + ibises.size() + " ibises. \n";

		result += name + " has " + metrics.size() + " metrics: ";

		for (Entry<MetricDescription, Metric> entry : metrics.entrySet()) {
			if (entry.getValue().getDescription().getType() == MetricType.NODE) {
				result += " " + entry.getValue().getDescription().getName();
			} else {
				result += " !" + entry.getValue().getDescription().getName();
			}
		}

		result += "\n";

		for (Link link : links.values()) {
			result += name + " " + ((LinkImpl) link).debugPrint();
		}

		result += "\n";

		for (Ibis ibis : ibises) {
			result += name + " " + ((IbisImpl) ibis).debugPrint();
		}

		result += "\n";

		for (Location child : children) {
			result += ((LocationImpl) child).debugPrint();
		}

		return result;
	}

}
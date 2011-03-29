package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Link;
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
	private ElementImpl origin;
	private ElementImpl destination;

	public LinkImpl(ElementImpl origin, Element destination) {
		super();
		this.origin = origin;
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
		return origin;
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
		// First update all of our children
		for (Link child : children) {
			((LinkImpl) child).update();
		}

		System.out.println("Link metrics: " + metrics.size());
		for (Entry<MetricDescription, Metric> data : metrics.entrySet()) {
			MetricDescription desc = data.getKey();

			if (desc.getType() != MetricType.LINK) {
				break;
			}

			MetricImpl metric = (MetricImpl) data.getValue();
			ArrayList<MetricOutput> types = desc.getOutputTypes();

			System.out.println("Link types: " + types.size());
			for (MetricOutput outputtype : types) {
				try {
					// First, we gather our own metrics
					MetricImpl srcMetric = (MetricImpl) origin.getMetric(desc);
					MetricImpl dstMetric = (MetricImpl) destination
							.getMetric(desc);

					HashMap<Element, Number> srcLinkValues = srcMetric
							.getLinkValue(MetricModifier.NORM, outputtype);
					HashMap<Element, Number> dstLinkValues = dstMetric
							.getLinkValue(MetricModifier.NORM, outputtype);

					if (outputtype == MetricOutput.PERCENT
							|| outputtype == MetricOutput.R
							|| outputtype == MetricOutput.RPOS) {
						float total = 0f, max = -10000000f, min = 10000000f;
						int childLinks = 0;

						float srcValue = (Float) srcLinkValues.get(destination);
						float dstValue = (Float) dstLinkValues.get(origin);

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

						if (outputtype == MetricOutput.PERCENT) {
							// Gather the metrics of our children, and multiply
							// by their weight
							for (Link child : children) {
								float childValue = (Float) ((LinkImpl) child)
										.getMetric(desc)
										.getValue(MetricModifier.NORM,
												outputtype);

								childLinks += ((LinkImpl) child)
										.getNumberOfDescendants();

								total += childValue
										* ((LinkImpl) child)
												.getNumberOfDescendants();

								if (childValue > max)
									max = childValue;
								if (childValue < min)
									min = childValue;
							}
							System.out.println("Link total/childLinks: "
									+ total / childLinks);
							metric.setValue(MetricModifier.NORM, outputtype,
									total / childLinks);
							metric.setValue(MetricModifier.MAX, outputtype, max);
							metric.setValue(MetricModifier.MIN, outputtype, min);
						} else {
							// Then we add the metric values of our child
							// locations
							for (Link child : children) {
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

						long srcValue = (Long) srcLinkValues.get(destination);
						long dstValue = (Long) dstLinkValues.get(origin);

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

						// Then we add the metric values of our child locations
						for (Link child : children) {
							int childValue = (Integer) child.getMetric(desc)
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

	@Override
	public boolean equals(Object thatObject) {
		if (this == thatObject)
			return true;
		if (!(thatObject instanceof LinkImpl))
			return false;

		// cast to native object is now safe
		LinkImpl that = (LinkImpl) thatObject;

		// now a proper field-by-field evaluation can be made
		return (this.origin.equals(that.origin) && this.destination
				.equals(that.destination))
				|| (this.origin.equals(that.destination) && this.destination
						.equals(that.origin));
	}

	@Override
	public int hashCode() {
		int hashCode = origin.hashCode() + destination.hashCode();
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

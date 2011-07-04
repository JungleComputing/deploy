package ibis.deploy.monitoring.collection.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.collection.exceptions.MetricNotAvailableException;
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General methods for any element within the data gathering universe
 * 
 * @author Maarten van Meersbergen
 */
public abstract class ElementImpl implements Element {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Element");

	Element parent;
	HashMap<MetricDescription, Metric> metrics;
	HashMap<ElementImpl, Link> links;

	public ElementImpl() {
		metrics = new HashMap<MetricDescription, Metric>();
		links = new HashMap<ElementImpl, Link>();
	}

	// getters
	public Metric getMetric(MetricDescription desc)
			throws MetricNotAvailableException {
		if (metrics.containsKey(desc)) {
			return metrics.get(desc);
		} else {
			throw new MetricNotAvailableException();
		}
	}

	public Link getLink(Element destination) throws SelfLinkeageException {
		Link result;
		if (destination == this) {
			throw new SelfLinkeageException();
		} else if (links.containsKey(destination)) {
			result = links.get(destination);
		} else {
			result = new LinkImpl(this, destination);
			links.put(((ElementImpl) destination), result);
			((ElementImpl) destination).addLink(this, result);
		}
		return result;
	}

	public void addLink(Element destination, Link link) {
		links.put(((ElementImpl) destination), link);
	}

	public void removeLink(Element destination) {
		links.remove(destination);
	}

	public Link[] getLinks() {
		return links.values().toArray(new Link[0]);
	}
	
	public Element getParent() {
		return parent;
	} 

	// Setters
	public void setMetrics(Set<MetricDescription> descriptions) {
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

		// and loop through the snapshot to remove unwanted metrics
		for (MetricDescription entry : temp) {
			if (!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}

	public void addMetric(MetricDescription description) {
		if (!metrics.containsKey(description)) {
			MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) description)
					.getMetric(this);
			metrics.put(description, newMetric);
		}
	}

	public void removeMetric(MetricDescription description) {
		metrics.remove(description);
	}
}
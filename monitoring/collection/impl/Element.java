package ibis.deploy.monitoring.collection.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ibis.deploy.monitoring.collection.exceptions.MetricNotAvailableException;
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General methods for any element within the data gathering universe
 * @author Maarten van Meersbergen
 */
public abstract class Element implements ibis.deploy.monitoring.collection.Element {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Element");
	
	HashMap<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric> metrics;
	HashMap<Element, ibis.deploy.monitoring.collection.Link> links;
	
	public Element() {
		metrics = new HashMap<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric>();
		links	= new HashMap<Element, ibis.deploy.monitoring.collection.Link>();
	}
		
	//getters		
	public ibis.deploy.monitoring.collection.Metric getMetric(ibis.deploy.monitoring.collection.MetricDescription desc) throws MetricNotAvailableException {
		if (metrics.containsKey(desc)) {
			return metrics.get(desc);
		} else {
			throw new MetricNotAvailableException();
		}
	}
	
	public ibis.deploy.monitoring.collection.Link getLink(ibis.deploy.monitoring.collection.Element destination) throws SelfLinkeageException {
		ibis.deploy.monitoring.collection.Link result;
		if (destination == this) {
			throw new SelfLinkeageException();
		} else if (links.containsKey(destination)) {
			result = links.get(destination);
		} else {
			result = new Link(this, destination);
			result.setMetrics(metrics.keySet());
			links.put(((Element)destination), result);
			((Element)destination).addLink(this, result);			
		}
		return result;
	}
	
	public void addLink(ibis.deploy.monitoring.collection.Element destination, ibis.deploy.monitoring.collection.Link link) {
		links.put(((Element)destination), link);
	}
	
	public void removeLink(ibis.deploy.monitoring.collection.Element destination) {
		links.remove(destination);
	}
	
	public ibis.deploy.monitoring.collection.Link[] getLinks() {
		return links.values().toArray(new ibis.deploy.monitoring.collection.Link[0]);
	}
	
	//Setters
	public void setMetrics(Set<ibis.deploy.monitoring.collection.MetricDescription> descriptions) {		
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
		
		//and loop through the snapshot to remove unwanted metrics
		for (ibis.deploy.monitoring.collection.MetricDescription entry : temp) {
			if(!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}
	
	public void addMetric(ibis.deploy.monitoring.collection.MetricDescription description) {
		if(!metrics.containsKey(description)) {
			Metric newMetric = (Metric) ((MetricDescription)description).getMetric(this); 
			metrics.put(description, newMetric);
		}
	}
	
	public void removeMetric(ibis.deploy.monitoring.collection.MetricDescription description) {
		metrics.remove(description);
	}	
}
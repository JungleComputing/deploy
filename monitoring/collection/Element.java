package ibis.deploy.monitoring.collection;

import java.util.Set;
import ibis.deploy.monitoring.collection.exceptions.*;

/**
 * General interface for any element within the data gathering universe
 */
public interface Element {		
	/**
	 * Returns all of the Metrics gathered by this element. 
	 * @return
	 * 		Metrics that can be queried for their values.
	 */
	public Metric[] getMetrics();
	public Metric getMetric(MetricDescription desc) throws MetricNotAvailableException;

	/**
	 * Returns the link corresponding to the destination, or null if no link exists
	 * @param destination
	 * 		The destination element.
	 * @return
	 * 		The link to the destination, or null if none existed.
	 * @throws SelfLinkeageException 
	 */
	public Link getLink(Element destination) throws SelfLinkeageException; 
	
	public Link[] getLinks();

	//Setters
	/**
	 * Sets the group of Metrics that is to be gathered by this element 
	 * from this moment onwards.
	 * @param metrics
	 * 		The metrics that need to be gathered from now on.
	 */
	public void setMetrics(Set<MetricDescription> metrics);
	public void addMetric(MetricDescription metric);
	public void removeMetric(MetricDescription metric);
	
}
package ibis.deploy.monitoring.collection;

import ibis.deploy.monitoring.collection.exceptions.MetricNotAvailableException;

/**
 * An interface for a link between two elements that exist within the managed universe.
 * @author Maarten van Meersbergen 
 */
public interface Link extends Element {
	public static enum LinkDirection { SRC_TO_DST, DST_TO_SRC };
	
	public Element getSource();
	public Element getDestination();
	
	public Metric[] getMetrics(LinkDirection dir);
	public Metric getMetric(MetricDescription desc, LinkDirection dir) throws MetricNotAvailableException;
}
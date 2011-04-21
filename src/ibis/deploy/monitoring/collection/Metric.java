package ibis.deploy.monitoring.collection;

import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;

/**
 * The interface for metrics, used in the gathering module
 * @author Maarten van Meersbergen
 *
 */
public interface Metric {
	public enum MetricModifier { MAX, MIN, NORM };
		
	/**
	 * Returns the value(s) of a metric
	 * @param mod
	 * 		the modifier of the requested output, MAX, MIN or NORM
	 * @param outputmethod
	 * 		the output method requested by the user, as defined in MetricDescription
	 * @return
	 * 		the current value of this metric
	 * @throws OutputUnavailableException
	 * 		if the selected outputmethod is considered nonsensical or otherwise unavailable
	 */
	public Number getValue(MetricModifier mod, MetricOutput outputmethod) throws OutputUnavailableException;
	
	/**
	 * Returns the MetricDescription of this metric.
	 * @return 
	 * 		the MetricDescription of this metric
	 */
	public MetricDescription getDescription();
}
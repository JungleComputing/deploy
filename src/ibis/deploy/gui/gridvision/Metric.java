package ibis.deploy.gui.gridvision;

import ibis.deploy.gui.gridvision.MetricDescription.MetricOutput;
import ibis.deploy.gui.gridvision.exceptions.OutputUnavailableException;

/**
 * The interface for metrics, used in the gathering module
 * @author maarten
 *
 */
public interface Metric {
		
	/**
	 * Returns the value(s) of a metric
	 * @param outputmethod
	 * 		the output method requested by the user, as defined in MetricDescription
	 * @return
	 * 		the current value of this metric
	 * @throws OutputUnavailableException
	 * 		if the selected outputmethod is considered nonsensical or otherwise unavailable
	 */
	public Float[] getCurrentValue(MetricOutput outputmethod) throws OutputUnavailableException;
	
		
	/**
	 * Returns the maximum value of the metric, if a non-percentage based output is selected, 
	 * this will return the maximum value, which the current percentage output is based on. 
	 * @param outputmethod
	 * 		the output method requested by the user, as defined in MetricDescription
	 * @return
	 * 		the maximum value of this metric as seen so far
	 * @throws OutputUnavailableException
	 */
	public Float[] getMaximumValue(MetricOutput outputmethod) throws OutputUnavailableException;
	
	
}
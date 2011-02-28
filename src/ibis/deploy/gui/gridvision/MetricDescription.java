package ibis.deploy.gui.gridvision;

import ibis.ipl.support.management.AttributeDescription;

/**
 * An interface for a description of a metric, in which several properties of this metric are defined.
 * @author maarten
 * 
 * This Interface defines several public constants used as return values
 * 		METRIC_VALUE_PERCENT 	if the value is represented as a percentage value, this means the returned float is always 
 * 								between 0.0f and 1.0f
 * 		METRIC_VALUE_RPLUS		if the value is represented as a positive real value
 * 								from 0.0f to the max float value
 * 		METRIC_VALUE_R			if the value returned can be any value represented by a float
 * 								from minimum float to maximum float
 * 		METRIC_VALUE_N			if the value is a discrete positive value that can be directly casted to an int
 */

public interface MetricDescription {
	public static enum MetricOutput {
		PERCENT, RPOS, R, N
	}
	
	//Getters
	public String getName();
	
	public Float[] getColor();
		
	/**
	 * Returns an array of available output types for this metric.
	 * @return
	 * 		any of the METRIC_VALUE_ constants defined in this interface
	 */		
	public MetricOutput[] getOutputTypes();
	
	/**
	 * Returns the attributes necessary for the updating of this metric
	 * @return
	 * 		an array of AttributeDescriptions needed for this metric
	 */
	public AttributeDescription[] getNecessaryAttributes();	
	
}

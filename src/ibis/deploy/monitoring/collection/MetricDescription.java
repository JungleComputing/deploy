package ibis.deploy.monitoring.collection;

import java.nio.IntBuffer;
import java.util.ArrayList;

import ibis.deploy.monitoring.collection.exceptions.*;

/**
 * An interface for a description of a metric, in which several properties of this metric are defined.
 * @author Maarten van Meersbergen
 * 
 * This Interface defines several public constants used as return values
 * 		PERCENT 	if the value is represented as a percentage value, this means the returned float 
 * 					is always between 0.0f and 1.0f
 * 		RPOS		if the value is represented as a positive real value from 0.0f to the max float 
 * 					value
 * 		R			if the value returned can be any value represented by a float from minimum float 
 * 					to maximum float
 * 		N			if the value is a discrete positive value that can be directly casted to an int
 */

public interface MetricDescription {
	public static enum MetricType {
		NODE, LINK, DERIVED_NODE, DERIVED_LINK
	}
	public static enum MetricOutput {
		PERCENT, RPOS, R, N
	}
	
	//Getters
	public String getName();
	public String getVerboseName();
	
	public MetricType getType();
	
	public Float[] getColor();
		
	/**
	 * Returns an array of available output types for this metric.
	 * @return
	 * 		any of the MetricOutput enum constants defined in this interface
	 */		
	public ArrayList<MetricOutput> getOutputTypes();
	
	/**
	 * Function that specifies what to do with the resulting values from the attribute update
	 * @param results
	 * 		the results array returned by the update cycle and passed on by the Metric class 		
	 * @param metric
	 * 		the metric that asks for the update to be done (this metric will be updated by callback)	
	 * @throws IncorrectParametersException 
	 * 		if the given Object[] does not contain the right type(s)
	 */
	public void update(Object[] results, Metric metric) throws IncorrectParametersException;	
	
	/**
	 * Returns a new metric based on this description
	 * @param element
	 * 		the element for which the metric should count.
	 * @return
	 * 		a new instance of the Metric class
	 */
	public Metric getMetric(Element element);
	
	
	/**
	 * Function that returns the maximum value on which percentage results are based.
	 * @return
	 * 		the currently set maximum value for percentage results
	 */
	public int getMaxForPercentages();
	
	/**
	 * Function to set the maximum value on which percentage results are based.	 
	 * @param newMax
	 * 		the new maximum value to use in percentage calculations.
	 */
	public void setMaxForPercentages(int newMax);

	
}

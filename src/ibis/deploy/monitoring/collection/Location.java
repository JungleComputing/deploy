package ibis.deploy.monitoring.collection;

import java.util.ArrayList;

import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;

/**
 * The interface for a representation of a location (Node, Site) in the data gathering universe
 * @author Maarten van Meersbergen
 */
public interface Location extends Element {
	public static enum Reducefunction {
		LOCATIONSPECIFIC_MINIMUM,
		LOCATIONSPECIFIC_AVERAGE,
		LOCATIONSPECIFIC_MAXIMUM,
		ALLDESCENDANTS_MINIMUM, 
		ALLDESCENDANTS_AVERAGE, 
		ALLDESCENDANTS_MAXIMUM
	}
		
	//Getters
	public String getName();
	
	public Float[] getColor();
	
	public ArrayList<Ibis> getIbises();
	
	public ArrayList<Ibis> getAllIbises();
	
	public ArrayList<Location> getChildren();
	
	public int getRank();
	
	/**
	 * Returns a number of links that correspond to the criteria.
	 * @param metric
	 * 		The metric which is used for the evaluations.
	 * @param outputmethod
	 * 		The output method of the metric with which we compare
	 * @param minimumValue
	 * 		The minimum value for the return value of the metric.
	 * @param maximumValue
	 * 		The maximum value for the return value of the metric.
	 * @return
	 * 		The links that correspond to the minimum and maximum values given.
	 */
	public ArrayList<Link> getLinks(MetricDescription metric, MetricOutput outputmethod, float minimumValue, float maximumValue);
	
}
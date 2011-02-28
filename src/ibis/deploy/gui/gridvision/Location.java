package ibis.deploy.gui.gridvision;

/**
 * A representation of a location (Node, Site) in the data gathering universe
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
	
	public Ibis[] getIbises();	
	
	public Location[] getChildren();
	
	/**
	 * This function returns a value derived from the values of either all of the Ibises located at its descendants,
	 * or derived from only its own Ibises 
	 * @param whichValue
	 * 		One of the VALUE_ constants defined in this interface.
	 * @param metric
	 * 		The description of the metric you are interested in.
	 * @return
	 * 		A Float[] containing the values specific to the metric.
	 */
	public Float[] getReducedValue(Reducefunction function, MetricDescription metric);
	
	/**
	 * Returns a number of links that correspond to the criteria.
	 * @param metric
	 * 		The metric which is used for the evaluations.
	 * @param minimumValue
	 * 		The minimum value for the return value of the metric.
	 * @param maximumValue
	 * 		The maximum value for the return value of the metric.
	 * @return
	 * 		The links that correspond to the minimum and maximum values given.
	 */
	public Link[] getLinks(MetricDescription metric, float minimumValue, float maximumValue);
	
	public Link getLink(Element destination); 
}
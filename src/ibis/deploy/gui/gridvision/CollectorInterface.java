package ibis.deploy.gui.gridvision;

/**
 * Serves as the main interface for the data collecting module.
 */
public interface Collector {
	//Getters	
	/**
	 * Returns all of the top-level locations in the data gathering universe.
	 * @return
	 * 		The locations.
	 */
	public Location[] getLocations();
	
	/**
	 * Returns the Ibis pools present in the data gathering universe.
	 * @return
	 * 		The pools.
	 */
	public Pool[] getPools();
		
	/**
	 * Returns the Metrics that have been defined and are ready to use.
	 * @return
	 * 		The Metrics that could be gathered.
	 */
	public MetricDescription[] getAvailableMetrics();
}

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
	public Metric[] getMetric(MetricDescription desc);
	public Metric[] getMetric(String metricName);

	//Setters
	/**
	 * Sets the group of Metrics that is to be gathered by this element 
	 * from this moment onwards.
	 * @param metrics
	 * 		The metrics that need to be gathered from now on.
	 */
	public void setMetrics(MetricDescription[] metrics);
	public void addMetric(MetricDescription metric);
	public void removeMetric(MetricDescription metric);
}


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

/**
 * A representation of a seperate Ibis instance within the data gathering universe
 */
public interface Ibis extends Element {
	
	public Location getLocation();
	
	public Pool getPool();
	
	//Tryout for 
	public 
}

/**
 * A link between two elements that exist within the managed universe. 
 */
public interface Link extends Element {
	
	public Location getLocation();
	
	public Link[] getChildren();
}

/**
 * The data gathering module's representation of an Ibis Pool. 
 */
public interface Pool {
	
	public String getName();
	
	public Ibis[] getIbises();
}

/**
 * This Interface defines several public constants used as return values
 * 		METRIC_VALUE_PERCENT 	if the value is represented as a percentage value, this means the returned float is always 
 * 								between 0.0f and 1.0f
 * 		METRIC_VALUE_RPLUS		if the value is represented as a positive real value
 * 								from 0.0f to the max float value
 * 		METRIC_VALUE_R			if the value returned can be any value represented by a float
 * 								from minimum float to maximum float
 * 		METRIC_VALUE_N			if the value is a discrete positive value that can be directly casted to an int
 */
public interface Metric {
	public static enum Metricoutput {
		PERCENT, RPOS, R, N
	}
	
	//Getters
	public String getName();
	
	public Float[] getColor();
		
	/**
	 * Returns a constant defined in the MetricInterface which defines how the return value of this metric should be interpreted.
	 * @return
	 * 		any of the METRIC_VALUE_ constants defined in this interface
	 */		
	public int getOutputType();
	
	/**
	 * Returns the group in which this metric belongs, as defined in this interface
	 * @return
	 * 		any of the METRIC_GROUP_ constants in this interface.
	 */
	public int getMetricGroup();
	
	/**
	 * Returns the value(s) of a node-sepcific metric
	 * @return
	 * 		the value
	 */
	public Float[] getValue();	
	
	//Operational
	public float getNecessaryAttributes();
}
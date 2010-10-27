package ibis.deploy.gui.gridvision;

import ibis.ipl.IbisIdentifier;

public interface CollectorInterface {
	//Getters	
	public LocInterface[] getLocations();
	
	public MetricInterface[] getAvailableMetrics();
}


public interface LocInterface {
	public static final int VALUE_MINIMUM_LOCATIONSPECIFIC	= 7001;
	public static final int VALUE_AVERAGE_LOCATIONSPECIFIC	= 7002;
	public static final int VALUE_MAXIMUM_LOCATIONSPECIFIC	= 7003;
	
	public static final int VALUE_MINIMUM_ALLCHILDREN		= 7004;	
	public static final int VALUE_AVERAGE_ALLCHILDREN		= 7005;	
	public static final int VALUE_MAXIMUM_ALLCHILDREN		= 7006;
	
	//Getters
	public String getName();
	
	public Float[] getColor();
	
	public LocInterface[] getChildren();
	
	public Link[] getFilteredLinks(MetricInterface metric, float minimumValue, float maximumValue);
	
	public ManagedIbisInterface[] getIbises();
	
	public MetricInterface[] getGatheredMetrics();
	
	public float getCompoundedValue(int whichValue, String metricName);
	
	public float getCompoundedValue(int whichValue, LocInterface destination, String metricName);
	
	//Setters
	public void setGatheredMetrics(MetricInterface[] metrics);
}


public interface ManagedIbisInterface {
	//Getters
	public LocInterface getLocation();
	
	public Link getLink(ManagedIbisInterface destination);
	
	public Link[] getFilteredLinks(MetricInterface metric, float minimumValue, float maximumValue);
	
	public MetricInterface[] getGatheredMetrics();
	
	//Setters
	public void setGatheredMetrics(MetricInterface[] metrics);	
}

/**
 * A link between two entities that exist within the managed universe.
 * This may mean a link between two distinct ManagedIbises, or a link between two Locations 
 */
public interface Link {
	public LocInterface getLocation();
		
	public MetricInterface[] getGatheredMetrics();
	
	//Setters
	public void setGatheredMetrics(MetricInterface[] metrics);
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
 * 								
 * 		METRIC_GROUP_NODE		if this metric is node-specific, for instance CPU load or memory usage
 * 		
 * 		METRIC_GROUP_LINK		if this metric is link-specific, for instance bytes sent or received 
 */
public interface MetricInterface {
	public final static int METRIC_VALUE_PERCENT = 9001;
	public final static int METRIC_VALUE_RPLUS	 = 9002;
	public final static int METRIC_VALUE_R		 = 9003;
	public final static int METRIC_VALUE_N		 = 9004;
	
	public final static int METRIC_GROUP_NODE	 = 8001;
	public final static int METRIC_GROUP_LINK	 = 8002;
	
	//Getters
	public String getName();
	
	public Float[] getColor();
		
	/**
	 * Returns a constant defined in the MetricInterface which defines how the return value of this metric should be interpreted.
	 * @return
	 * 		any of the METRIC_VALUE_ constants defined in this interface
	 */		
	public int getValueMeaning();
	
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
	 * @throws WrongMetricGroupException
	 * 		if this method is called on a metric with group METRIC_GROUP_LINK
	 */
	public Float[] getValue() throws WrongMetricGroupException;
	
	/**
	 * Returns the value(s) of a link-specific metric
	 * @param destination
	 * 		The destination Ibis to which the host node has a link
	 * @return
	 * 		the value, if no link exists to the destination, 0.0f is returned.
	 * @throws WrongMetricGroupException
	 * 		if this method is called on a metric with group METRIC_GROUP_NODE
	 */
	public Float[] getValue(ManagedIbisInterface destination) throws WrongMetricGroupException;
	
	
	
	//Operational
	public float getNecessaryAttributes();
}
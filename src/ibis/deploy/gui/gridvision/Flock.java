package ibis.deploy.gui.gridvision;

import java.util.ArrayList;
import java.util.HashMap;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;
import ibis.smartsockets.virtual.NoSuitableModuleException;

/**
 * The Generic data-holding class for the Flocks. 
 * @author maarten
 *
 */
public interface Flock {
	public static final int MAX = 2350;
	public static final int AVG = 2351;
	public static final int MIN = 2352;
	
	/**
	 * Returns the value of the requested Metric, as identified by its name (String).
	 * @param key 
	 * 		The name of the metric.
	 * @param mod
	 * 		The modifier, defined as MAX, MIN or AVG constants in this interface
	 * @return
	 * 		a float, giving the maximum, average or minimum value of the requested metric.
	 * @throws StatNotRequestedException
	 * 		is thrown in case the requested metric was not enabled for gathering.
	 * @throws ModeUnknownException
	 * 		is thrown in case the modifier does not correspond to any of the constants in this interface.
	 */
	public float getNodeMetricsValue(String key, int mod) throws StatNotRequestedException, ModeUnknownException;
	
	/**
	 * Returns the value of the requested link Metric.
	 * @param link
	 * 		The Flock to which the link was established.
	 * @param key
	 * 		The name of the metric value to be returned.
	 * @param mod
	 * 		The modifier, defined as MAX, MIN or AVG constants in this interface
	 * @return
	 * 	a float, giving the maximum, average or minimum value of the requested metric.
	 * @throws StatNotRequestedException
	 * 		is thrown in case the requested metric was not enabled for gathering.
	 * @throws ModeUnknownException
	 * 		is thrown in case the modifier does not correspond to any of the constants in this interface.
	 */	 
	public float getLinkMetricsValue(Flock link, String key, int mod) throws StatNotRequestedException, ModeUnknownException;
	
	/**
	 * Returns the children of this particular Flock.
	 * @return
	 * 		The children.
	 */
	public Flock[] getChildren();	
	
	/**
	 * Returns the network links to other flocks of this level.
	 * @return
	 * 		The flocks which this flock has network links to.
	 */
	public Flock[] getLinks();
	
	/**
	 * Returns the Set of currently monitored node-based metrics.
	 * @return
	 * 		The set of currently monitored node-based metrics.
	 */
	public ArrayList<String> getMonitoredNodeMetrics();
	
	/**
	 * Returns the Set of currently monitored link-based metrics.
	 * @return
	 * 		The set of currently monitored link-based metrics.
	 */
	public ArrayList<String> getMonitoredLinkMetrics();
	
	/**
	 * Sets the currently monitored metrics to be equal to those in the list provided. 
	 * @param newMetrics
	 * 		The list of desired metrics to be monitored.
	 */
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics);
		
	/**
	 * Returns the MetricsList of currently gathered metrics.
	 * @return
	 * 		a MetricsList containing all of the metrics that are gathered in this Flock.
	 */
	public MetricsList getCurrentlyGatheredMetrics();
		
	/**
	 * Updates the values of all the currently monitored metrics by asking the ibisConcepts 
	 * down the chain to update first, and then updating its own values.
	 * @throws NoSuitableModuleException
	 * 		In case the network connection to one of the children was not yet established.
	 * @throws StatNotRequestedException 
	 * 		if the children do not all have the same list of gathered metrics. This should be avoidable.
	 */
	public void update() throws NoSuitableModuleException, StatNotRequestedException;
	
	/**
	 * Returns the colors of the visual representations of the node-based metrics.
	 * @return
	 * 		A map of the string names of the node-based metrics, coupled with the colors in Float[3] format.
	 */
	public HashMap<String, Float[]> getNodeMetricColors();
	
	/**
	 * Returns the colors of the visual representations of the link-based metrics.
	 * @return
	 * 		A map of the string names of the link-based metrics, coupled with the colors in Float[3] format.
	 */
	public HashMap<String, Float[]> getLinkMetricColors();
	
	/**
	 * Returns the string representation of this Flock
	 * @return
	 * 		a String representation of this Concept.
	 */
	public String getName();
	
	/**
	 * Returns the location of this flock.
	 * @return
	 * 		the location of this flock.
	 */
	public Location getLocation();
	
	/**
	 * Returns the parent of this flock.
	 * @return
	 * 		the parent flock.
	 */
	public Flock getParent();
	
	/**
	 * Returns whether this flock is root of the tree.
	 * @return
	 * 		true if this flock is root, false if not.
	 */
	public boolean isRoot();
	
	/**
	 * Returns whether this is the lowest flock in the tree.
	 * @return
	 * 		true if this is the lowest flock, false if there are children
	 */
	public boolean isLeaf();
	
	/**
	 * Adds a child to this flock. 
	 * @param ii
	 * 		the IbisIdentifier of the child to add.
	 * @param newLocation
	 * 		the location of the new flock.
	 * @return
	 * 		the newly created flock node corresponding to the child.
	 */
	public Flock addChild(IbisIdentifier ii, Location newLocation);
	
	/**
	 * Removes a child from the flock.
	 * @param child
	 * 		the child that needs to be removed.
	 */
	public void removeChild(Flock child);
}

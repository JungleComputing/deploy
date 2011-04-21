package ibis.deploy.monitoring.collection;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Serves as the main interface for the data collecting module.
 */
public interface Collector extends Runnable {
	//Getters	
	/**
	 * Returns the root of the top-level locations in the data gathering universe.
	 * @return
	 * 		The locations root.
	 */
	public Location getRoot();
	
	/**
	 * Returns the Ibis pools present in the data gathering universe.
	 * @return
	 * 		The pools.
	 */
	public ArrayList<Pool> getPools();
		
	/**
	 * Returns the Metrics that have been defined and are ready to use.
	 * @return
	 * 		The Metrics that could be gathered.
	 */
	public HashSet<MetricDescription> getAvailableMetrics();
	
	/**
	 * Returns whether there was any change in the universe.
	 */
	public boolean change();
	
	/**
	 * Resets the data gathering module, is automatically done if the universe changes.
	 */
	public void initUniverse();
	
	
	//Tryout for interface updates.
	/**
	 * Gets the current minimum refreshrate for the updating of metrics. 
	 * @return
	 * 		the current refreshrate.
	 */
	public int getRefreshrate();
	
	/**
	 * Sets the minimum refreshrate for the updating of metrics. 
	 * @param newInterval
	 */
	public void setRefreshrate(int newInterval);

	
}
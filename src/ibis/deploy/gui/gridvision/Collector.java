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
	
	//Tryout for interface updates.
	/**
	 * Sets the minimum refreshrate for the updating of metrics. 
	 * @param newInterval
	 */
	public void setRefreshrate(int newInterval);
}
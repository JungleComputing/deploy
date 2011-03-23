package ibis.deploy.monitoring.collection;

/**
 * A representation of a seperate Ibis instance within the data gathering universe
 */
public interface Ibis extends Element {
	
	public Location getLocation();
	
	public Pool getPool();
	
	//Tryout for steering
	public void kill();
}
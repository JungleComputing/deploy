package ibis.deploy.monitoring.collection;

import java.util.ArrayList;


/**
 * The interface for the data gathering module's representation of an Ibis Pool. 
 * @author Maarten van Meersbergen
 */
public interface Pool {
	
	public String getName();
	
	public ArrayList<Ibis> getIbises();
}
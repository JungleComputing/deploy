package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data gathering module's representation of an Ibis Pool. 
 * @author Maarten van Meersbergen
 */
public class Pool implements ibis.deploy.monitoring.collection.Pool {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Pool");
	
	private String name;
	private ArrayList<ibis.deploy.monitoring.collection.Ibis> ibises;
	
	public Pool(String name) {
		this.name = name;
		
		ibises = new ArrayList<ibis.deploy.monitoring.collection.Ibis>();
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<ibis.deploy.monitoring.collection.Ibis> getIbises() {
		return ibises;
	}
	
	public void addIbis(ibis.deploy.monitoring.collection.Ibis newIbis) {
		ibises.add(newIbis);
	}
	
	public void removeIbis(ibis.deploy.monitoring.collection.Ibis newIbis) {
		ibises.remove(newIbis);
	}
}
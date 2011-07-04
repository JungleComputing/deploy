package ibis.deploy.monitoring.collection.impl;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Pool;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data gathering module's representation of an Ibis Pool.
 * 
 * @author Maarten van Meersbergen
 */
public class PoolImpl implements Pool {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Pool");

	private String name;
	private ArrayList<Ibis> ibises;

	public PoolImpl(String name) {
		this.name = name;

		ibises = new ArrayList<Ibis>();
	}

	public String getName() {
		return name;
	}

	public ArrayList<Ibis> getIbises() {
		return ibises;
	}

	public void addIbis(Ibis newIbis) {
		ibises.add(newIbis);
	}

	public void removeIbis(Ibis newIbis) {
		ibises.remove(newIbis);
	}
}
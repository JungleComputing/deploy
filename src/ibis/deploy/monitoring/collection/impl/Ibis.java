package ibis.deploy.monitoring.collection.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.ipl.IbisIdentifier;
import ibis.ipl.NoSuchPropertyException;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.MetricDescription.MetricType;
import ibis.deploy.monitoring.collection.Pool;

/**
 * A representation of a seperate Ibis instance within the data gathering universe
 * @author Maarten van Meersbergen
 */
public class Ibis extends Element implements ibis.deploy.monitoring.collection.Ibis {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Ibis");
	
	ManagementServiceInterface manInterface;
	IbisIdentifier ibisid;
	private Pool pool;
	private Location location;
	
	public Ibis(ManagementServiceInterface manInterface, IbisIdentifier ibisid, Pool pool, Location location) {
		super();
		this.manInterface = manInterface;
		this.ibisid = ibisid;
		this.pool = pool;
		this.location = location;		
	}
	
	public ibis.deploy.monitoring.collection.Metric[] getMetrics() {
		ArrayList<ibis.deploy.monitoring.collection.Metric> result = new ArrayList<ibis.deploy.monitoring.collection.Metric>();
		for (ibis.deploy.monitoring.collection.Metric metric : metrics.values()) {
			if (metric.getDescription().getType() == MetricType.NODE) {
				result.add(metric);
			}
		}		
		return result.toArray(new ibis.deploy.monitoring.collection.Metric[0]);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Pool getPool() {
		return pool;
	}
	
	public void setMetrics(Set<ibis.deploy.monitoring.collection.MetricDescription> descriptions) {
		for (ibis.deploy.monitoring.collection.Link link : links.values()) {
			((Link)link).setMetrics(descriptions);
		}
		
		//add new metrics
		for (ibis.deploy.monitoring.collection.MetricDescription md : descriptions) {
			if(!metrics.containsKey(md)) {
				Metric newMetric = (Metric) ((MetricDescription)md).getMetric(this); 
				metrics.put(md, newMetric);
			}
		}
		
		//make a snapshot of our current metrics.
		Set<ibis.deploy.monitoring.collection.MetricDescription> temp = new HashSet<ibis.deploy.monitoring.collection.MetricDescription>();		
		temp.addAll(metrics.keySet());
		
		//and loop through the snapshot to remove unwanted metrics that don't appear in the new set
		for (ibis.deploy.monitoring.collection.MetricDescription entry : temp) {
			if(!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}
	
	public void update() throws TimeoutException {
		//Make an array of all the AttributeDescriptions needed to update this Ibis' metrics.
		ArrayList<AttributeDescription> requestList = new ArrayList<AttributeDescription>();
		for (Entry<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric> entry : metrics.entrySet()) {
			Metric metric = (Metric)entry.getValue();
			requestList.addAll(((MetricDescription)metric.getDescription()).getNecessaryAttributes());
		}
		
		AttributeDescription[] requestArray = (AttributeDescription[]) requestList.toArray(new AttributeDescription[0]);
		
		try {
			//Then, pass this array to the management service interface, and receive an array of result objects in the same order
			Object[] results = manInterface.getAttributes(ibisid, requestArray);
			
			//Split the result objects into partial arrays depending on the amount needed per metric
			int j=0;			
			for (Entry<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric> entry : metrics.entrySet()) {
				Metric metric = ((Metric)entry.getValue());
				Object[] partialResults = new Object[((MetricDescription)metric.getDescription()).getNecessaryAttributes().size()];				
				for (int i=0; i < partialResults.length ; i++) {
					partialResults[i] = results[j];	
					j++;
				}
				
				//And pass them to the individual metrics to be updated.
				metric.update(partialResults);
			}
		} catch (IOException e) {
			throw new TimeoutException();
		} catch (NoSuchPropertyException e) {
			logger.error("Ibis "+ibisid+" got exception while updating metrics: "+ e.getMessage());
		} catch (Exception e) {
			logger.error("Ibis "+ibisid+" got exception while updating metrics: "+ e.getMessage());
		}		
	}	
	
	//Tryout for steering
	public void kill() {
		//TODO implement
	}
	
	public String debugPrint() {
		String result = ibisid+" metrics: ";
				
		for (Entry<ibis.deploy.monitoring.collection.MetricDescription, ibis.deploy.monitoring.collection.Metric> entry : metrics.entrySet()) {
			if (entry.getValue().getDescription().getType() == MetricType.NODE) {
				result += " " + entry.getValue().getDescription().getName();
			} else {
				result += " !" + entry.getValue().getDescription().getName();
			}
		}
		
		result += "\n";
		
		for (ibis.deploy.monitoring.collection.Link link : links.values()) {
			result += ibisid + " "+((Link)link).debugPrint();
		}

		result += "\n";
		
		return result;
	}
}
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
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.collection.MetricDescription.MetricType;
import ibis.deploy.monitoring.collection.Pool;

/**
 * A representation of a seperate Ibis instance within the data gathering
 * universe
 * 
 * @author Maarten van Meersbergen
 */
public class IbisImpl extends ElementImpl implements Ibis {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Ibis");

	ManagementServiceInterface manInterface;
	IbisIdentifier ibisid;
	private Pool pool;
	private Location location;

	public IbisImpl(ManagementServiceInterface manInterface,
			IbisIdentifier ibisid, Pool pool, Location location) {
		super();
		this.manInterface = manInterface;
		this.ibisid = ibisid;
		this.pool = pool;
		this.location = location;
	}

	public Metric[] getMetrics() {
		ArrayList<Metric> result = new ArrayList<Metric>();
		for (Metric metric : metrics.values()) {
			if (metric.getDescription().getType() == MetricType.NODE) {
				result.add(metric);
			}
		}
		return result.toArray(new Metric[0]);
	}

	public Location getLocation() {
		return location;
	}

	public Pool getPool() {
		return pool;
	}

	public void setMetrics(Set<MetricDescription> descriptions) {
		for (Link link : links.values()) {
			((LinkImpl) link).setMetrics(descriptions);
		}

		// add new metrics
		for (MetricDescription md : descriptions) {
			if (!metrics.containsKey(md)) {
				MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) md)
						.getMetric(this);
				metrics.put(md, newMetric);
			}
		}

		// make a snapshot of our current metrics.
		Set<MetricDescription> temp = new HashSet<MetricDescription>();
		temp.addAll(metrics.keySet());

		// and loop through the snapshot to remove unwanted metrics that don't
		// appear in the new set
		for (MetricDescription entry : temp) {
			if (!descriptions.contains(entry)) {
				metrics.remove(entry);
			}
		}
	}

	public void update() throws TimeoutException {
		// Make an array of all the AttributeDescriptions needed to update this
		// Ibis' metrics.
		ArrayList<AttributeDescription> requestList = new ArrayList<AttributeDescription>();
		for (Entry<MetricDescription, Metric> entry : metrics.entrySet()) {
			MetricImpl metric = (MetricImpl) entry.getValue();
			requestList
					.addAll(((MetricDescriptionImpl) metric.getDescription())
							.getNecessaryAttributes());
		}

		AttributeDescription[] requestArray = (AttributeDescription[]) requestList
				.toArray(new AttributeDescription[0]);

		try {
			// Then, pass this array to the management service interface, and
			// receive an array of result objects in the same order
			Object[] results = manInterface.getAttributes(ibisid, requestArray);

			// Split the result objects into partial arrays depending on the
			// amount needed per metric
			int j = 0;
			for (Entry<MetricDescription, Metric> entry : metrics.entrySet()) {
				MetricImpl metric = ((MetricImpl) entry.getValue());
				Object[] partialResults = new Object[((MetricDescriptionImpl) metric
						.getDescription()).getNecessaryAttributes().size()];
				for (int i = 0; i < partialResults.length; i++) {
					partialResults[i] = results[j];
					j++;
				}

				// And pass them to the individual metrics to be updated.
				metric.update(partialResults);
			}
		} catch (IOException e) {
			throw new TimeoutException();
		} catch (NoSuchPropertyException e) {
			logger.error("Ibis " + ibisid
					+ " got exception while updating metrics: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error("Ibis " + ibisid
					+ " got exception while updating metrics: ");
			e.printStackTrace();
		}

		for (Link link : links.values()) {
			((LinkImpl) link).update();
		}
	}

	// Tryout for steering
	public void kill() {
		// TODO implement
	}

	public String debugPrint() {
		String result = ibisid + " metrics: ";

		for (Entry<MetricDescription, Metric> entry : metrics.entrySet()) {
			if (entry.getValue().getDescription().getType() == MetricType.NODE) {
				result += " " + entry.getValue().getDescription().getName();
			} else {
				result += " !" + entry.getValue().getDescription().getName();
			}
		}

		result += "\n";

		for (Link link : links.values()) {
			result += ibisid + " " + ((LinkImpl) link).debugPrint();
		}

		result += "\n";

		return result;
	}
}
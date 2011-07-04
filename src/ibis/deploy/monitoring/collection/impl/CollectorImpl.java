package ibis.deploy.monitoring.collection.impl;

import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.collection.Pool;
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.metrics.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as the main class for the data collecting module.
 */
public class CollectorImpl implements Collector, Runnable {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Collector");
	private static final ibis.ipl.Location universe = new ibis.ipl.impl.Location(
			new String[0]);
	private static final int workercount = 8;

	private static CollectorImpl ref = null;	
	private boolean skipParent = false, forceUpdate = false;

	// Interfaces to the IPL
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;

	// Map to hold the status quo
	private Map<String, Integer> poolSizes;

	// Refreshrate for the status updates
	int refreshrate;

	// Queue and Worker threads
	private LinkedList<Element> jobQueue;
	ArrayList<Worker> workers;
	private int waiting = 0;

	// Internal lists
	private HashMap<String, Location> locations;
	private HashMap<Element, Location> parents;
	private HashMap<String, Pool> pools;
	private HashMap<IbisIdentifier, Ibis> ibises;
	private HashSet<Link> links;

	// Externally available
	private Location root;
	private HashSet<MetricDescription> availableDescriptions;
	private HashSet<MetricDescription> descriptions;
	
	private boolean change = false;
	

	private CollectorImpl(ManagementServiceInterface manInterface,
			RegistryServiceInterface regInterface) {
		this.manInterface = manInterface;
		this.regInterface = regInterface;

		// Initialize all of the lists and hashmaps needed
		poolSizes = new HashMap<String, Integer>();
		locations = new HashMap<String, Location>();
		parents = new HashMap<Element, Location>();
		pools = new HashMap<String, Pool>();
		availableDescriptions = new HashSet<MetricDescription>();
		descriptions = new HashSet<MetricDescription>();
		ibises = new HashMap<IbisIdentifier, Ibis>();
		links = new HashSet<Link>();
		jobQueue = new LinkedList<Element>();
		workers = new ArrayList<Worker>();

		// Create a universe (location root)
		Float[] color = { 0f, 0f, 0f };
		root = new LocationImpl("root", color);

		// Set the default refreshrate
		refreshrate = 1000;

		// Set the default metrics
		MetricDescription cpuDesc = new CPUUsage();
		MetricDescription load = new Load();
		MetricDescription sysmemDesc = new SystemMemory();
		MetricDescription heapmemDesc = new HeapMemory();
		MetricDescription nonheapmemDesc = new NonHeapMemory();
		MetricDescription bytespersecDesc = new BytesSentPerSecond();
		
		descriptions.add(cpuDesc);
		descriptions.add(sysmemDesc);
		descriptions.add(heapmemDesc);
		descriptions.add(nonheapmemDesc);
		// descriptions.add(new ThreadsMetric());
		//descriptions.add(new BytesReceivedPerSecond());
		descriptions.add(bytespersecDesc);
		// descriptions.add(new BytesReceived());
		// descriptions.add(new BytesSent());
		
		//Set the available metrics
		availableDescriptions.add(cpuDesc);
		availableDescriptions.add(load);
		availableDescriptions.add(sysmemDesc);
		availableDescriptions.add(heapmemDesc);
		availableDescriptions.add(nonheapmemDesc);
		availableDescriptions.add(bytespersecDesc);
	}

	private void initWorkers() {
		// workers.clear();
		waiting = 0;

		// Create and start worker threads for the metric updates
		for (int i = 0; i < workercount; i++) {
			Worker worker = new Worker();
			workers.add(worker);
		}

		for (Worker w : workers) {
			w.start();
		}
	}

	public static CollectorImpl getCollector(
			ManagementServiceInterface manInterface,
			RegistryServiceInterface regInterface) {
		if (ref == null) {
			ref = new CollectorImpl(manInterface, regInterface);
			ref.initWorkers();
			// ref.initUniverse();
		}
		return ref;
	}

	public static CollectorImpl getCollector()
			throws SingletonObjectNotInstantiatedException {
		if (ref != null) {
			return ref;
		} else {
			throw new SingletonObjectNotInstantiatedException();
		}
	}

	public void initUniverse() {
		// Check if there was a change in the pool sizes
		boolean change = initPools();

		if (change || forceUpdate) {			
			// Rebuild the world
			initLocations();
			initLinks();
			initMetrics();

			if (logger.isDebugEnabled()) {
				logger.debug("world rebuilt");
				// System.out.println("--------------------------");
				// System.out.println(((Location)root).debugPrint());
				// System.out.println("--------------------------");
				// logger.debug(((Location)root).debugPrint());
			}

			// once all updating is finished, signal the visualizations that a
			// change has occurred.
			this.change = true;
			this.forceUpdate = false;
		}
	}

	private boolean initPools() {
		boolean change = false;
		Map<String, Integer> newSizes = new HashMap<String, Integer>();

		try {
			newSizes = regInterface.getPoolSizes();
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Could not get pool sizes from registry.");
			}
		}

		// clear the pools list, if warranted
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
			String poolName = entry.getKey();
			int newSize = entry.getValue();

			if (!poolSizes.containsKey(poolName)
					|| newSize != poolSizes.get(poolName)) {
				pools.clear();
				change = true;

				poolSizes = newSizes;
			}
		}

		if (change) {
			// reinitialize the pools list
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
				String poolName = entry.getKey();
				int newSize = entry.getValue();

				if (newSize > 0) {
					pools.put(poolName, new PoolImpl(poolName));
				}
			}
		}

		return change;
	}

	private void initLocations() {
		ibises.clear();
		locations.clear();
		parents.clear();		
		
		Float[] color = { 0f, 0f, 0f };
		root = new LocationImpl("root", color);
		parents.put(root, null);

		// For all pools
		for (Entry<String, Pool> entry : pools.entrySet()) {
			String poolName = entry.getKey();

			// Get the members of this pool
			IbisIdentifier[] poolIbises;
			try {
				poolIbises = regInterface.getMembers(poolName);

				// for all ibises
				for (IbisIdentifier ibisid : poolIbises) {
					// Get the lowest location
					ibis.ipl.Location ibisLocation;
					if (skipParent) {
						ibisLocation = ibisid.location().getParent();
					} else {
						ibisLocation = ibisid.location();
					}
					String locationName = ibisLocation.toString();

					Location current;
					if (locations.containsKey(locationName)) {
						current = locations.get(locationName);
					} else {
						current = new LocationImpl(locationName, color);
						locations.put(locationName, current);
					}

					// And add the ibis to that location
					IbisImpl ibis = new IbisImpl(manInterface, ibisid, entry.getValue(), current);
					((LocationImpl) current).addIbis(ibis);
					parents.put(ibis, current);
					ibises.put(ibisid, ibis);

					// for all location levels, get parent
					ibis.ipl.Location parentIPLLocation = ibisLocation.getParent();
					while (!parentIPLLocation.equals(universe)) {
						String name = parentIPLLocation.toString();

						// Make a new location if we have not encountered the
						// parent
						Location parent;
						if (locations.containsKey(name)) {
							parent = locations.get(name);
						} else {
							parent = new LocationImpl(name, color);
							locations.put(name, parent);
						}						

						// And add the current location as a child of the parent
						((LocationImpl) parent).addChild(current);
						parents.put(current, parent);
						
						current = parent;

						parentIPLLocation = parentIPLLocation.getParent();
					}

					// Finally, add the top-level location to the root location,
					// it will only add if it is not already there
					((LocationImpl) root).addChild(current);
					parents.put(current, root);
				}
			} catch (IOException e1) {
				if (logger.isErrorEnabled()) {
					logger.error("Could not get Ibises from pool: " + poolName);
				}
			}
		}
	}

	private void initMetrics() {
		((LocationImpl) root).setMetrics(descriptions);
	}

	private void initLinks() {
		links.clear();
		
		// pre-make the location-location links
		for (Location source : locations.values()) {
			for (Location destination : locations.values()) {
				try {
					if (!isAncestorOf(source, destination) && !isAncestorOf(destination, source)) {
						LinkImpl newLink = (LinkImpl) source.getLink(destination);
						links.add(newLink);
					}
				} catch (SelfLinkeageException ignored) {
					// ignored, because we do not want this link
				}
			}
		}
		
		// pre-make the ibis-location links
		for (Ibis source : ibises.values()) {
			for (Location destination : locations.values()) {
				try {
					if (!isAncestorOf(source, destination) && !isAncestorOf(destination, source)) {
						LinkImpl newLink = (LinkImpl) source.getLink(destination);
						links.add(newLink);
					}
				} catch (SelfLinkeageException ignored) {
					// ignored, because we do not want this link
				}
			}
		}

		// pre-make the ibis-ibis links
		for (Ibis source : ibises.values()) {
			for (Ibis destination : ibises.values()) {
				try {
					LinkImpl newLink = (LinkImpl) source.getLink(destination);
					links.add(newLink);
				} catch (SelfLinkeageException ignored) {
					// ignored, because we do not want this link
				}
			}
		}
		logger.info("Collector created "+links.size()+" links.");
		((LocationImpl) root).makeLinkHierarchy();
	}
	
	private void setlinksNotUpdated() {
		for (Link link : links) {
			((LinkImpl) link).setNotUpdated();
		}
	}

	// Getters
	public Location getRoot() {
		return root;
	}

	public ArrayList<Pool> getPools() {
		ArrayList<Pool> result = new ArrayList<Pool>();
		for (Pool pool : pools.values()) {
			result.add(pool);
		}
		return result;
	}

	public HashSet<MetricDescription> getAvailableMetrics() {
		return availableDescriptions;
	}

	public boolean change() {
		boolean temp = change;
		change = false;
		return temp;
	}

	// Tryout for interface updates.	
	public int getRefreshrate() {
		return refreshrate;
	}
	
	public void setRefreshrate(int newInterval) {
		refreshrate = newInterval;
	}
		
	public void toggleMetrics(MetricDescription[] myDescs) {
		synchronized (jobQueue) {
			HashSet<MetricDescription> newDescriptions = new HashSet<MetricDescription>();
			
			//First, add all current metrics
			for (MetricDescription desc : descriptions) {
				newDescriptions.add(desc);
			} 
			
			//Then deselect the ones that match the parameters, and add the ones that were not there yet
			for (MetricDescription desc : myDescs) {
				if (newDescriptions.contains(desc)) {
					newDescriptions.remove(desc);
				} else {
					newDescriptions.add(desc);
				}
			}		
			
			descriptions = newDescriptions;
			
			initMetrics();
			
			this.change = true;
		}
	}
		
	public void toggleParentSkip() {
		skipParent = !skipParent;
		forceUpdate = true;
	}

	// Getters for the worker threads
	public Element getWork(Worker w) throws InterruptedException {
		Element result = null;

		synchronized (jobQueue) {
			while (jobQueue.isEmpty()) {
				waiting += 1;
				// logger.debug("waiting: "+waiting);
				jobQueue.wait();
			}
			result = jobQueue.removeFirst();
		}

		return result;
	}

	public Ibis getIbis(IbisIdentifier ibisid) {
		return ibises.get(ibisid);
	}
	
	public Element getParent(Element child) {
		return parents.get(child);
	}
	
	public boolean isAncestorOf(Element child, Element ancestor) {		
		Element current = parents.get(child);
		while (current != null) {
			if (current == ancestor) return true;
			current = parents.get(current);
		}
		return false;
	}

	public void run() {
		// int iterations = 0;

		while (true) {
			// Clear the queue for a new round, and make sure every worker is
			// waiting
			synchronized (jobQueue) {
				waiting = 0;
				jobQueue.clear();
				for (Worker w : workers) {
					w.interrupt();
				}
			}

			// Add stuff to the queue and notify
			synchronized (jobQueue) {
				initUniverse();
				setlinksNotUpdated();
				
				jobQueue.addAll(ibises.values());
				jobQueue.add(root);
				waiting = 0;
				jobQueue.notifyAll();
			}

			// sleep for the refreshrate
			try {
				Thread.sleep(refreshrate);
			} catch (InterruptedException e) {
				if (logger.isErrorEnabled()) {
					logger.error("Interrupted, this should be ignored.");
				}
			}

			// and then see if our workers have done their jobs
			synchronized (jobQueue) {
				if (waiting == workercount) {
					if (!jobQueue.isEmpty()) {
						logger.debug("workers idling while jobqueue not empty.");
					}
					logger.debug("Succesfully finished queue.");
				} else {
					// If they have not, give warning, and try again next turn.
					logger.debug("Workers still working: "
							+ (workercount - waiting));
					logger.debug("Ibises left in queue: " + jobQueue.size()
							+ " / " + ibises.size());
					logger.debug("Consider increasing the refresh time.");
				}
			}
			// iterations++;
		}
	}	
}
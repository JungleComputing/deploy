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

import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.metrics.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as the main class for the data collecting module.
 */
public class Collector implements ibis.deploy.monitoring.collection.Collector, Runnable {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Collector");
	private static final ibis.ipl.Location universe = new ibis.ipl.impl.Location(new String[0]);
	private static final int workercount = 8;
	
	private static Collector ref = null;
	
	//Interfaces to the IPL
	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	
	//Map to hold the status quo
	private Map<String, Integer> poolSizes;
	
	//Refreshrate for the status updates
	int refreshrate;
	
	//Queue and Worker threads
	private LinkedList<Element> jobQueue;
	ArrayList<Worker> workers;
	private int waiting = 0;

	//Internal lists
	private HashMap<String, ibis.deploy.monitoring.collection.Location> locations;
	private HashMap<String, ibis.deploy.monitoring.collection.Pool> pools;	
	private HashMap<IbisIdentifier, ibis.deploy.monitoring.collection.Ibis> ibises;
	
	//Externally available 
	private ibis.deploy.monitoring.collection.Location root;
	private HashSet<ibis.deploy.monitoring.collection.MetricDescription> descriptions;
	private boolean change = false;
	
	private Collector(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {		
		this.manInterface = manInterface;
		this.regInterface = regInterface;
		
		//Initialize all of the lists and hashmaps needed
		poolSizes = new HashMap<String, Integer>();		
		locations = new HashMap<String, ibis.deploy.monitoring.collection.Location>();
		pools = new HashMap<String, ibis.deploy.monitoring.collection.Pool>();
		descriptions = new HashSet<ibis.deploy.monitoring.collection.MetricDescription>();
		ibises = new HashMap<IbisIdentifier, ibis.deploy.monitoring.collection.Ibis>();
		jobQueue = new LinkedList<ibis.deploy.monitoring.collection.Element>();
		workers = new ArrayList<Worker>();
		
		//Create a universe (location root)
		Float[] color = {0f,0f,0f};
		root = new Location("root", color);
		
		//Set the default refreshrate
		refreshrate = 1000;
		
		//Set the default metrics
		descriptions.add(new CPUUsage());
		descriptions.add(new HeapMemory());
		descriptions.add(new NonHeapMemory());
		//descriptions.add(new ThreadsMetric());
		descriptions.add(new BytesReceivedPerSecond());
		descriptions.add(new BytesSentPerSecond());
	}		
		
	private void initWorkers() {
		//workers.clear();
		waiting = 0;
		
		//Create and start worker threads for the metric updates
		for (int i=0; i<workercount; i++) {
			Worker worker = new Worker();
			workers.add(worker);			
		}
		
		for (Worker w : workers) {		
			w.start();
		}
	}
		
	public static Collector getCollector(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface) {
		if (ref == null) {
			ref = new Collector(manInterface, regInterface);
			ref.initWorkers();
			//ref.initUniverse();
		}
		return ref;		
	}
	
	public static Collector getCollector() throws SingletonObjectNotInstantiatedException {
		if (ref != null) {
			return ref;
		} else {
			throw new SingletonObjectNotInstantiatedException();
		}
	}
	
	public void initUniverse() {		
		//Check if there was a change in the pool sizes
		boolean change = initPools();
		
		if (change) {
			//Rebuild the world		
			initLocations();
			initLinks();
			initMetrics();
			
			if (logger.isDebugEnabled()) {
				logger.debug("world rebuilt");
				//System.out.println("--------------------------");
				//System.out.println(((Location)root).debugPrint());
				//System.out.println("--------------------------");
				//logger.debug(((Location)root).debugPrint());
			}
			
			//once all updating is finished, signal the visualizations that a change has occurred.
			this.change = true;
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

		//clear the pools list, if warranted
		for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
			String poolName = entry.getKey();
	        int newSize = entry.getValue();

	        if (!poolSizes.containsKey(poolName) || newSize != poolSizes.get(poolName)) {
	         	pools.clear();
	         	change = true;	 
	         	
	         	poolSizes = newSizes;
	        }
		}
		
		if (change) {
			//reinitialize the pools list
			for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
				String poolName = entry.getKey();
		        int newSize = entry.getValue();
			        
		        if (newSize > 0) {
			       	pools.put(poolName, new Pool(poolName));
			    }		        
			}			
		}
		
		return change;
	}	
	
	private void initLocations() {
		ibises.clear();
		locations.clear();
		Float[] color = {0f,0f,0f};
		root = new Location("root", color);
		
		//For all pools
		for (Entry<String, ibis.deploy.monitoring.collection.Pool> entry : pools.entrySet()) {
			String poolName = entry.getKey();
			
			//Get the members of this pool
			IbisIdentifier[] poolIbises;
			try {
				poolIbises = regInterface.getMembers(poolName);
				
				//for all ibises
				for (IbisIdentifier ibisid : poolIbises) {									
					//Get the lowest location, skip the lowest (ibis) location
					ibis.ipl.Location ibisLocation = ibisid.location().getParent();
					String locationName = ibisLocation.toString();
															
					ibis.deploy.monitoring.collection.Location current;
					if (locations.containsKey(locationName)) {
						current = locations.get(locationName);
					} else {
						current = new Location(locationName, color);
						locations.put(locationName, current);
					}
					
					//And add the ibis to that location
					Ibis ibis = new Ibis(manInterface, ibisid, entry.getValue(), current);
					((Location)current).addIbis(ibis);
					ibises.put(ibisid, ibis);
										
					//for all location levels, get parent
					ibis.ipl.Location parentIPLLocation = ibisLocation.getParent();
					while (!parentIPLLocation.equals(universe)) {
						String name = parentIPLLocation.toString();
						
						//Make a new location if we have not encountered the parent
						ibis.deploy.monitoring.collection.Location parent;
						if (locations.containsKey(name)) {
							parent = locations.get(name);
						} else {
							parent = new Location(name, color);
							locations.put(name, parent);
						}
						
						//And add the current location as a child of the parent
						((Location)parent).addChild(current);
						
						current = parent;
						
						parentIPLLocation = parentIPLLocation.getParent();
					}
					
					//Finally, add the top-level location to the root location, 
					//it will only add if it is not already there					
					((Location)root).addChild(current);
				}
			} catch (IOException e1) {	
				if (logger.isErrorEnabled()) {
					logger.error("Could not get Ibises from pool: " + poolName);
				}
			}
		}
	}
	
	private void initMetrics() {
		((Location)root).setMetrics(descriptions);		
	}
	
	private void initLinks() {
		//pre-make the location-location links
		for (ibis.deploy.monitoring.collection.Location source : locations.values()) {
			for (ibis.deploy.monitoring.collection.Location destination : locations.values()) {
				try {
					source.getLink(destination);
				} catch (SelfLinkeageException ignored) {
					//ignored, because we do not want this link
				}
			}
		}
		
		//pre-make the ibis-ibis links
		for (ibis.deploy.monitoring.collection.Ibis source : ibises.values()) {
			for (ibis.deploy.monitoring.collection.Ibis destination : ibises.values()) {
				try {
					source.getLink(destination);
				} catch (SelfLinkeageException ignored) {
					//ignored, because we do not want this link
				}
			}
		}
				
		((Location)root).makeLinkHierarchy();
	}
		
	//Getters	
	public ibis.deploy.monitoring.collection.Location getRoot() {
		return root;
	}
	
	public ArrayList<ibis.deploy.monitoring.collection.Pool> getPools() {
		ArrayList<ibis.deploy.monitoring.collection.Pool> result = new ArrayList<ibis.deploy.monitoring.collection.Pool>();
		for (ibis.deploy.monitoring.collection.Pool pool : pools.values()) {
			result.add(pool);
		}
		return result;
	}
	
	public HashSet<ibis.deploy.monitoring.collection.MetricDescription> getAvailableMetrics() {
		return descriptions;
	}
	
	public boolean change() {
		boolean temp = change;
		change = false;
		return temp;
	}
	
	//Tryout for interface updates.
	public void setRefreshrate(int newInterval) {
		refreshrate = newInterval;
	}	
	
	//Getters for the worker threads
	public ibis.deploy.monitoring.collection.Element getWork(Worker w) throws InterruptedException {
		ibis.deploy.monitoring.collection.Element result = null;
		
		synchronized(jobQueue) {
			while (jobQueue.isEmpty()) {				
				waiting += 1;
				//logger.debug("waiting: "+waiting);
				jobQueue.wait();				
			}
			result = jobQueue.removeFirst();
		}
		
		return result;
	}
	
	public ibis.deploy.monitoring.collection.Ibis getIbis(IbisIdentifier ibisid) {
		return ibises.get(ibisid);
	}
		
	public void run() {
		int iterations = 0;
		
		
		
		while (true) {
			//Clear the queue for a new round, and make sure every worker is waiting 
			synchronized(jobQueue) {
				waiting = 0;
				jobQueue.clear();
				for (Worker w : workers) {
					w.interrupt();
				}
			}
			
			//Add stuff to the queue and notify
			synchronized(jobQueue) {				
				initUniverse();
				jobQueue.addAll(ibises.values());
				jobQueue.add(root);
				waiting = 0;
				jobQueue.notifyAll();
			}
			
			//sleep for the refreshrate 
			try {		
				Thread.sleep(refreshrate);				
			} catch (InterruptedException e) {
				if (logger.isErrorEnabled()) {
					logger.error("Interrupted, this should be ignored.");
				}
			}
			
			//and then see if our workers have done their jobs			
			synchronized(jobQueue) {
				if (waiting == workercount) {
					if (!jobQueue.isEmpty()) {
						logger.error("workers idling while jobqueue not empty.");
					}					
					logger.debug("Succesfully finished queue.");
				} else {
					//If they have not, give warning, and try again next turn.	
					logger.warn("Workers still working: "+(workercount-waiting));
					logger.warn("Ibises left in queue: "+jobQueue.size()+" / "+ ibises.size());	
					logger.warn("Consider increasing the refresh time.");
				}
			}
			iterations++;
		}
	}
}
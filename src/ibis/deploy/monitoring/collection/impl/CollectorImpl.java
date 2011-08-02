package ibis.deploy.monitoring.collection.impl;

import gov.nasa.worldwind.geom.LatLon;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;
import ibis.deploy.vizFramework.persistence.PersistenceManagementService;
import ibis.deploy.vizFramework.persistence.PersistenceRegistryService;
import ibis.deploy.vizFramework.persistence.XMLExporter;
import ibis.deploy.vizFramework.persistence.XMLImporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import ibis.deploy.gui.GUI;
import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.collection.Pool;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.collection.exceptions.SelfLinkeageException;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.metrics.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as the main class for the data collecting module.
 */
public class CollectorImpl implements Collector, Runnable {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.monitoring.collection.impl.Collector");
    private static final ibis.ipl.Location universe = new ibis.ipl.impl.Location(
            new String[0]);
    private static final int workercount = 1;

    private static CollectorImpl ref = null;

    private static GUI gui = null;

    // Interfaces to the IPL
    private ManagementServiceInterface manInterface, realManInterface;
    private RegistryServiceInterface regInterface, realRegInterface;

    // interfaces to the persistence module
    private RegistryServiceInterface persistenceRegInterface = new PersistenceRegistryService();
    private ManagementServiceInterface persistenceManInterface = new PersistenceManagementService(
            regInterface);

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
    private HashSet<MetricDescription> descriptions;
    private boolean change = false;

    private XMLExporter xmlConvertor = XMLExporter.getInstance();
    private boolean writingToFile = false;

    private CollectorImpl(ManagementServiceInterface manInterface,
            RegistryServiceInterface regInterface) {
        this.manInterface = realManInterface = manInterface;
        this.regInterface = realRegInterface = regInterface;

        // Initialize all of the lists and hashmaps needed
        poolSizes = new HashMap<String, Integer>();
        locations = new HashMap<String, Location>();
        parents = new HashMap<Element, Location>();
        pools = new HashMap<String, Pool>();
        descriptions = new HashSet<MetricDescription>();
        ibises = new HashMap<IbisIdentifier, Ibis>();
        links = new HashSet<Link>();
        jobQueue = new LinkedList<Element>();
        workers = new ArrayList<Worker>();

        // Create a universe (location root)
        Float[] color = { 0f, 0f, 0f };

        if (locations.get("root") == null) {
            LatLon temp = Utils.generateLatLon(false, null);
            root = new LocationImpl("root", color, temp.getLatitude().degrees,
                    temp.getLongitude().degrees);

            locations.put("root", root);
        } else {
            root = locations.get("root");
        }

        // Set the default refreshrate
        refreshrate = 1000;

        // Set the default metrics
        descriptions.add(new CPUUsage());
        descriptions.add(new SystemMemory());
        descriptions.add(new HeapMemory());
        descriptions.add(new NonHeapMemory());
        // descriptions.add(new ThreadsMetric());
        // descriptions.add(new BytesReceivedPerSecond());
        descriptions.add(new BytesSentPerSecond());
        // descriptions.add(new BytesReceived());
        // descriptions.add(new BytesSent());
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
        return getCollector(manInterface, regInterface, null);
    }

    public static CollectorImpl getCollector(
            ManagementServiceInterface manInterface,
            RegistryServiceInterface regInterface, GUI gui) {
        if (ref == null) {
            ref = new CollectorImpl(manInterface, regInterface);
            ref.initWorkers();
            Utils.setGUI(gui);
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

        if (change) {
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
        }
    }

    private boolean initPools() {
        boolean change = false;
        Map<String, Integer> newSizes = new HashMap<String, Integer>();

        try {
            newSizes = regInterface.getPoolSizes();
            // if (writingToFile) {
            // xmlConvertor.poolsToXML(newSizes);
            // }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Could not get pool sizes from registry.");
            }
        }

        // check if there were pools that disappeared
        for (Map.Entry<String, Integer> entry : poolSizes.entrySet()) {
            String oldPoolName = entry.getKey();
            if (!newSizes.containsKey(oldPoolName)) {
                change = true;
                break;
            }
        }

        // clear the pools list, if warranted
        for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
            String poolName = entry.getKey();
            int newSize = entry.getValue();

            if (!poolSizes.containsKey(poolName)
                    || newSize != poolSizes.get(poolName)) {
                change = true;
                break;
            }
        }

        if (change) {
            pools.clear();
            poolSizes.clear();
            poolSizes.putAll(newSizes); // copy instead of assign --> if the
                                        // newSizes map is reused, things go
                                        // wrong

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

        LatLon temp;
        Utils.resetIndex(); // to reuse the same random locations
        if (locations.get("root") == null) {
            temp = Utils.generateLatLon(false, null);
            root = new LocationImpl("root", color, temp.getLatitude().degrees,
                    temp.getLongitude().degrees);
            locations.put("root", root);
        } else {
            root = locations.get("root");
        }

        parents.put(root, null);
        xmlConvertor.clearBuffer();

        // For all pools
        for (Entry<String, Pool> entry : pools.entrySet()) {
            String poolName = entry.getKey();

            // Get the members of this pool
            IbisIdentifier[] poolIbises;
            try {
                poolIbises = regInterface.getMembers(poolName);

                if (writingToFile) {
                    xmlConvertor.poolToXML(poolName, poolIbises);
                } else {
                    xmlConvertor.cachePoolContents(poolName, poolIbises);
                }

                // for all ibises
                for (IbisIdentifier ibisid : poolIbises) {
                    // Get the lowest location
                    ibis.ipl.Location ibisLocation = ibisid.location(); // .getParent();
                    String locationName = ibisLocation.toString();

                    Location current;
                    if (locations.containsKey(locationName)) {
                        current = locations.get(locationName);
                    } else {
                        temp = // Utils.generateLatLon(!locationName.contains("@"),
                               // locationName);
                        Utils.generateLatLon(
                                locationName.startsWith("cluster"),
                                locationName);
                        current = new LocationImpl(locationName, color,
                                temp.getLatitude().degrees,
                                temp.getLongitude().degrees);
                        locations.put(locationName, current);
                    }

                    // And add the ibis to that location
                    IbisImpl ibis = new IbisImpl(manInterface, ibisid,
                            entry.getValue(), current);
                    ((LocationImpl) current).addIbis(ibis);
                    parents.put(ibis, current);
                    ibises.put(ibisid, ibis);

                    // for all location levels, get parent
                    ibis.ipl.Location parentIPLLocation = ibisLocation
                            .getParent();
                    while (!parentIPLLocation.equals(universe)) {
                        String name = parentIPLLocation.toString();

                        // Make a new location if we have not encountered the
                        // parent
                        Location parent;
                        if (locations.containsKey(name)) {
                            parent = locations.get(name);
                        } else {
                            temp = // Utils.generateLatLon(!name.contains("@"),
                                   // name);
                            Utils.generateLatLon(name.startsWith("cluster"),
                                    name); // TODO better Location assign
                            parent = new LocationImpl(name, color,
                                    temp.getLatitude().degrees,
                                    temp.getLongitude().degrees);
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
                    if (!isAncestorOf(source, destination)
                            && !isAncestorOf(destination, source)) {
                        LinkImpl newLink = (LinkImpl) source
                                .getLink(destination);
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
                    if (!isAncestorOf(source, destination)
                            && !isAncestorOf(destination, source)) {
                        LinkImpl newLink = (LinkImpl) source
                                .getLink(destination);
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
        System.out.println("Collector created " + links.size() + " links.");
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
        return descriptions;
    }

    public boolean change() {
        boolean temp = change;
        change = false;
        return temp;
    }

    // Tryout for interface updates.
    public void setRefreshrate(int newInterval) {
        refreshrate = newInterval;
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
        // a bit more inefficient, but sometimes we have different
        // FakeIbisIdentifier instances for the same ibis ...
        for (IbisIdentifier ibis : ibises.keySet()) {
            if (ibis.name().equals(ibisid.name())
                    && ibis.location().equals(ibisid.location())) {
                return ibises.get(ibis);
            }
        }
        return null;

        // return ibises.get(ibisid);
    }

    public Element getParent(Element child) {
        return parents.get(child);
    }

    public boolean isAncestorOf(Element child, Element ancestor) {
        Element current = parents.get(child);
        while (current != null) {
            if (current == ancestor)
                return true;
            current = parents.get(current);
        }
        return false;
    }

    public void run() {
        int iterations = 0;

        while (true) {
            // Clear the queue for a new round, and make sure every worker is
            // waiting
            synchronized (jobQueue) {
                waiting = 0;
                jobQueue.clear();
                for (Worker w : workers) {
                    w.interrupt();
                    // w.setNumIbises(ibises.values().size());
                }
            }

            // Add stuff to the queue and notify
            synchronized (jobQueue) {
                if (writingToFile) {
                    xmlConvertor.startUpdate();
                    // System.out.println("------------------------------");
                }
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

                // TODO - see if this is the right place to do this
                if (writingToFile) {
                    for (Ibis ibis : ibises.values()) {
                        IbisImpl source = (IbisImpl) ibis;
                        Metric[] linkMetrics = source.getLinkMetrics();
                        for (int i = 0; i < linkMetrics.length; i++) {
                            MetricImpl metric = (MetricImpl) linkMetrics[i];
                            HashMap<Element, Number> values = (HashMap<Element, Number>) metric.linkValues
                                    .get(MetricOutput.PERCENT);

                            if (values != null) {
                                for (Element elem : values.keySet()) {
                                    IbisImpl dest = (IbisImpl) elem;

                                    xmlConvertor.linkMetricToXML(dest.getPool()
                                            .getName(), source.getName(), dest
                                            .getName(), values.get(elem)
                                            .floatValue(), metric
                                            .getDescription().getName());
                                }
                            }
                        }

                        Metric[] nodeMetrics = source.getMetrics();
                        String subtype;
                        ArrayList<AttributeDescription> attributes;
                        long subvalue = 0;

                        for (int i = 0; i < nodeMetrics.length; i++) {
                            MetricImpl metric = (MetricImpl) nodeMetrics[i];

                            if (metric.getDescription().getName().equals(CPUUsage.CPU)) {
                                attributes = ((CPUUsage) metric
                                        .getDescription())
                                        .getNecessaryAttributes();
                                for (AttributeDescription attr : attributes) {
                                    subtype = attr.getAttribute();
                                    subvalue = 0;
                                    
                                    if (subtype
                                            .equals(CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_TIME)) {
                                        subvalue = metric
                                                .getHelperVariable(
                                                        CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_TIME)
                                                .longValue();
                                    } else if (subtype
                                            .equals(CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_UPTIME)) {
                                        subvalue = metric
                                                .getHelperVariable(
                                                        CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_UPTIME)
                                                .longValue();
                                    } else {
                                        subvalue = metric
                                                .getHelperVariable(
                                                        CPUUsage.ATTRIBUTE_NAME_PROCESS_AVAILABLE_PROCESSORS)
                                                .longValue();
                                    }
                                    xmlConvertor.cpuMetricToXML(source
                                            .getPool().getName(), source
                                            .getName(), subvalue,
                                            metric.getDescription().getName(),
                                            subtype);
                                }
                            } else if (metric.getDescription().getName().equals(SystemMemory.MEM_SYS)) {
                                
                                attributes = ((SystemMemory) metric
                                        .getDescription())
                                        .getNecessaryAttributes();
                                for (AttributeDescription attr : attributes) {
                                    subtype = attr.getAttribute();
                                    subvalue = 0;
                                    if (subtype
                                            .equals(SystemMemory.ATTRIBUTE_TOTAL_PHYSICAL_MEMORY_SIZE)) {
                                        subvalue = metric
                                                .getHelperVariable(
                                                        SystemMemory.ATTRIBUTE_TOTAL_PHYSICAL_MEMORY_SIZE)
                                                .longValue();
                                    } else if (subtype
                                            .equals(SystemMemory.ATTRIBUTE_FREE_PHYSICAL_MEMORY_SIZE)) {
                                        subvalue = metric
                                                .getHelperVariable(
                                                        SystemMemory.ATTRIBUTE_FREE_PHYSICAL_MEMORY_SIZE)
                                                .longValue();
                                    } 
                                    xmlConvertor.cpuMetricToXML(source
                                            .getPool().getName(), source
                                            .getName(), subvalue,
                                            metric.getDescription().getName(),
                                            subtype);
                                }
                            } else { 
                                
//                                try {
//                                    normalMemory = metric.getValue(
//                                            MetricModifier.NORM,
//                                            MetricOutput.RPOS).floatValue();
//                                    maxMemory = metric.getValue(
//                                            MetricModifier.MAX,
//                                            MetricOutput.RPOS).floatValue();
//                                } catch (OutputUnavailableException e) {
//                                    e.printStackTrace();
//                                }
//                                xmlConvertor.cpuMetricToXML(source.getPool()
//                                        .getName(), source.getName(),
//                                        percentMemory, normalMemory, maxMemory,
//                                        metric.getDescription().getName());
                            }
                            // System.out.println(metric.getDescription()
                            // .getName() + " " + percentMemory + metric);
                        }
                    }

                    xmlConvertor.endUpdate();
                }
            }

            iterations++;
        }
    }

    @Override
    public int getRefreshRate() {
        return refreshrate;
    }

    public void toXML() {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter("out.xml"));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            writer.write("<graph>\n");
            writer.write(((LocationImpl) root).toXML());
            writer.write("</graph>\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startWritingToFile() {
        xmlConvertor.openFile("out.xml");
    }

    public void stopWritingToFile() {
        xmlConvertor.closeFile();
    }

    public boolean isWritingToFile() {
        return writingToFile;
    }

    public void setWritingToFile(boolean state) {
        writingToFile = state;
    }

    public void startImport() {
        manInterface = persistenceManInterface;
        regInterface = persistenceRegInterface;

        initUniverse();

        XMLImporter.getImporter().openFile("out.xml");
    }
}
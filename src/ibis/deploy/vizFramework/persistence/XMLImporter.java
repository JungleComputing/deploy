package ibis.deploy.vizFramework.persistence;

import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.impl.CollectorImpl;
import ibis.deploy.monitoring.collection.metrics.BytesSentPerSecond;
import ibis.deploy.monitoring.collection.metrics.CPUUsage;
import ibis.deploy.monitoring.collection.metrics.HeapMemory;
import ibis.deploy.monitoring.collection.metrics.NonHeapMemory;
import ibis.deploy.monitoring.collection.metrics.SystemMemory;
import ibis.deploy.monitoring.simulator.FakeIbisIdentifier;
import ibis.deploy.vizFramework.VisualizationPanel;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;
import ibis.ipl.IbisIdentifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Ana Vinatoru
 * 
 */

public class XMLImporter {

    private static XMLImporter importer;
    private static final int DOING_NOTHING = 0;
    private static final int READING_FROM_FILE = 1;
    private static final int PAUSED = 2;

    private Integer state = DOING_NOTHING;
    private XMLStreamReader xmlStreamReader;

    private HashMap<String, Integer> poolSizes;
    private HashMap<String, HashMap<String, IbisIdentifier>> ibises;

    private HashMap<String, HashMap<IbisIdentifier, Long>> bytesSentMetric;
    private HashMap<String, HashMap<String, Long>> cpuUsageMetric,
            memSysMetric, heapMetric, nonHeapMetric;

    private File file;

    private XMLImporter() {
        poolSizes = new HashMap<String, Integer>();
        ibises = new HashMap<String, HashMap<String, IbisIdentifier>>();

        bytesSentMetric = new HashMap<String, HashMap<IbisIdentifier, Long>>();
        cpuUsageMetric = new HashMap<String, HashMap<String, Long>>();
        memSysMetric = new HashMap<String, HashMap<String, Long>>();
        heapMetric = new HashMap<String, HashMap<String, Long>>();
        nonHeapMetric = new HashMap<String, HashMap<String, Long>>();
    }

    private void clear() {
        poolSizes.clear();
        ibises.clear();
        clearMetrics();
    }

    private void clearMetrics() {
        bytesSentMetric.clear();
        cpuUsageMetric.clear();
        memSysMetric.clear();
        heapMetric.clear();
        nonHeapMetric.clear();
    }

    public static XMLImporter getImporter() {
        if (importer == null) {
            importer = new XMLImporter();
        }
        return importer;
    }

    public synchronized void openFile(File file) {
        try {
            state = PAUSED;
            this.file = file;
            clear(); // we want to remove all residual data
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream input = new FileInputStream(file);
            xmlStreamReader = inputFactory.createXMLStreamReader(input);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public synchronized void toggleReading(boolean pause) {
        if (pause) {
            state = PAUSED;
        } else {
            if (state == PAUSED) {
                state = READING_FROM_FILE;
            }
        }
    }

    private synchronized void readFromFile() {
        if (state == READING_FROM_FILE) {
            try {
                boolean endOfUpdateFound = false;
                String elementName;
                String poolName = "";

                while (xmlStreamReader != null && xmlStreamReader.hasNext()
                        && !endOfUpdateFound) {
                    int event = xmlStreamReader.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        elementName = xmlStreamReader.getLocalName();
                        if (elementName.equals("Update")) {
                            clearMetrics();
                        } else if (elementName.equals("Pool")) {
                            poolName = readPool();
                        } else if (elementName.equals("Ibis")) {
                            readIbis(poolName);
                        } else if (elementName.equals("Metric")) {
                            readMetric();
                        }
                    }

                    if (event == XMLStreamConstants.START_DOCUMENT) {

                    }
                    if (event == XMLStreamConstants.END_DOCUMENT) {

                        VisualizationPanel panel = VisualizationPanel
                                .getVisualizationPanel(null);
                        if (panel != null) {
                            panel.resetPlayButton();
                            // reopen the file to make sure we can replay again
                            try {
                                CollectorImpl.getCollector().prepareForImport(
                                        file);
                            } catch (SingletonObjectNotInstantiatedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                    if (event == XMLStreamConstants.END_ELEMENT) {
                        if (xmlStreamReader.getLocalName().equals("Update")) {
                            endOfUpdateFound = true;
                        }
                    }
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

    }

    private synchronized String readPool() {
        String attributeName, attributeValue, poolName = "";
        int poolSize;

        if (state == READING_FROM_FILE) {
            poolName = "";
            poolSize = 0;

            for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                attributeName = xmlStreamReader.getAttributeLocalName(i);
                attributeValue = xmlStreamReader.getAttributeValue(i);

                if (attributeName.equals("name")) {
                    poolName = attributeValue;
                } else if (attributeName.equals("size")) {
                    poolSize = Integer.parseInt(attributeValue);
                }
            }
            if (poolName.length() > 0) {
                poolSizes.put(poolName, poolSize);
                if (ibises.get(poolName) != null) {
                    ibises.clear();
                } else {
                    ibises.put(poolName, new HashMap<String, IbisIdentifier>());
                }
            }
        }

        return poolName;
    }

    private synchronized void readIbis(String poolName) {
        String attributeName, attributeValue;
        String ibisLocation = "", ibisId = "", completeIbisName = "";
        HashMap<String, IbisIdentifier> ibisesInPool;

        if (state == READING_FROM_FILE) {
            for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                attributeName = xmlStreamReader.getAttributeLocalName(i);
                attributeValue = xmlStreamReader.getAttributeValue(i);

                if (attributeName.equals("location")) {
                    ibisLocation = attributeValue;
                } else if (attributeName.equals("id")) {
                    ibisId = attributeValue;
                }
            }

            if (ibisLocation.length() > 0) {
                IbisIdentifier fakeibis = new FakeIbisIdentifier(ibisLocation,
                        poolName, ibisId);

                completeIbisName = Utils
                        .extractFullNameFromIbisIdentifier(fakeibis);

                ibisesInPool = ibises.get(poolName);
                if (ibisesInPool == null) {
                    ibisesInPool = new HashMap<String, IbisIdentifier>();
                    ibises.put(poolName, ibisesInPool);
                }
                ibisesInPool.put(completeIbisName, fakeibis);
            }
        }
    }

    private synchronized void readMetric() {
        String source = "", destination = "", attributeName, attributeValue, poolName = "", type = "", subtype = "";
        float value = 0, maxValue = 0;
        int i;
        if (state == READING_FROM_FILE) {
            for (i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                attributeName = xmlStreamReader.getAttributeLocalName(i);
                attributeValue = xmlStreamReader.getAttributeValue(i);

                if (attributeName.equals("source")) {
                    source = attributeValue;
                } else if (attributeName.equals("type")) {
                    type = attributeValue;
                } else if (attributeName.equals("subtype")) {
                    subtype = attributeValue;
                } else if (attributeName.equals("poolName")) {
                    poolName = attributeValue;
                } else if (attributeName.equals("destination")) {
                    destination = attributeValue;
                } else if (attributeName.equals("value")) {
                    value = Float.parseFloat(attributeValue);
                } else if (attributeName.equals("maxValue")) {
                    maxValue = Float.parseFloat(attributeValue);
                }
            }

            if (type.equals(BytesSentPerSecond.BYTES_SENT_PER_SEC)) {
                readLinkMetric(source, destination, poolName, value);
            } else { // it's a node metric
                readNodeMetric(source, poolName, type, subtype, (long) value,
                        (long) maxValue);
            }

        }
    }

    private synchronized void readNodeMetric(String source, String poolName,
            String type, String subtype, long value, long maxValue) {
        HashMap<String, IbisIdentifier> ibisesInPool;
        HashMap<String, Long> helperIbisMap = null;
        HashMap<String, HashMap<String, Long>> originalMap = null;
        boolean hasMax = false;
        if (state == READING_FROM_FILE) {
            if (source.length() > 0 && poolName.length() > 0) {
                ibisesInPool = ibises.get(poolName);
                IbisIdentifier sourceIbis = null;

                if (ibisesInPool != null) {
                    sourceIbis = ibisesInPool.get(source);
                    if (sourceIbis != null) {
                        if (type.equals(CPUUsage.CPU)) {
                            helperIbisMap = cpuUsageMetric.get(source);
                            originalMap = cpuUsageMetric;
                        } else if (type.equals(SystemMemory.MEM_SYS)) {
                            helperIbisMap = memSysMetric.get(source);
                            originalMap = memSysMetric;
                        } else if (type.equals(HeapMemory.MEM_HEAP)) {
                            helperIbisMap = heapMetric.get(source);
                            originalMap = heapMetric;
                            subtype = HeapMemory.USED;
                            hasMax = true;
                        } else if (type.equals(NonHeapMemory.MEM_NON_HEAP)) {
                            helperIbisMap = nonHeapMetric.get(source);
                            originalMap = nonHeapMetric;
                            subtype = NonHeapMemory.USED;
                            hasMax = true;
                        }

                        // if there is no entry for this ibis in the map, create
                        // it
                        if (helperIbisMap == null) {
                            helperIbisMap = new HashMap<String, Long>();
                            if (originalMap != null) {
                                originalMap.put(source, helperIbisMap);
                            }
                        }

                        if (subtype.length() > 0) {
                            helperIbisMap.put(subtype, value);
                            if (hasMax) {
                                helperIbisMap.put(HeapMemory.MAX, maxValue);
                            }
                        }
                    }
                }
            }
        }
    }

    private synchronized void readLinkMetric(String source, String destination,
            String poolName, float value) {
        HashMap<String, IbisIdentifier> ibisesInPool;
        HashMap<IbisIdentifier, Long> metricsPerIbis;

        if (state == READING_FROM_FILE) {
            if (source.length() > 0 && destination.length() > 0
                    && poolName.length() > 0) {
                ibisesInPool = ibises.get(poolName);
                if (ibisesInPool != null) {
                    IbisIdentifier sourceIbis = ibisesInPool.get(source);
                    IbisIdentifier destIbis = ibisesInPool.get(destination);

                    if (sourceIbis != null && destIbis != null) {
                        metricsPerIbis = bytesSentMetric.get(source);
                        if (metricsPerIbis == null) {
                            metricsPerIbis = new HashMap<IbisIdentifier, Long>();
                            bytesSentMetric.put(source, metricsPerIbis);
                        }
                        metricsPerIbis.put(destIbis, (long) value);
                    }
                }
            }
        }
    }

    public synchronized IbisIdentifier[] getMembers(String poolName) {
        if (ibises.get(poolName) == null) {
            return new IbisIdentifier[0];
        }
        return ibises.get(poolName).values().toArray(new IbisIdentifier[0]);
    }

    public synchronized Map<String, Integer> getPoolSizes() throws IOException {
        return poolSizes;
    }

    public synchronized Map<IbisIdentifier, Long> getLinkMetric(
            String poolName, String ibisName) {
        if (state == READING_FROM_FILE) {
            if (bytesSentMetric.get(ibisName) == null) {
                return new HashMap<IbisIdentifier, Long>();
            }
            return bytesSentMetric.get(ibisName);
        }
        return new HashMap<IbisIdentifier, Long>();
    }

    public synchronized long getNodeMetric(String ibis, String type,
            String subtype) {
        HashMap<String, HashMap<String, Long>> currentMap = null;

        if (state == READING_FROM_FILE) {
            if (type.equals(CPUUsage.CPU)) {
                currentMap = cpuUsageMetric;
            } else if (type.equals(SystemMemory.MEM_SYS)) {
                currentMap = memSysMetric;
            } else if (type.equals(HeapMemory.MEM_HEAP)) {
                currentMap = heapMetric;
            } else if (type.equals(NonHeapMemory.MEM_NON_HEAP)) {
                currentMap = nonHeapMetric;
            }
            if (currentMap != null && currentMap.get(ibis) != null
                    && currentMap.get(ibis).get(subtype) != null) {
                return currentMap.get(ibis).get(subtype);
            }
        }
        return 0;
    }

    public synchronized void setFile(File file) {
        this.file = file;
    }

    // call this from the Collector
    public synchronized void doRun() {
        switch (state) {
        case DOING_NOTHING:
            break;
        case PAUSED:
            break;
        case READING_FROM_FILE:
            readFromFile();
        }
    }
}

package ibis.deploy.vizFramework.persistence;

import ibis.deploy.monitoring.collection.metrics.BytesSentPerSecond;
import ibis.deploy.monitoring.collection.metrics.CPUUsage;
import ibis.deploy.monitoring.collection.metrics.SystemMemory;
import ibis.deploy.monitoring.simulator.FakeIbisIdentifier;
import ibis.deploy.monitoring.simulator.FakeRegistryService.State;
import ibis.deploy.util.PoolSizePrinter;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;
import ibis.ipl.IbisIdentifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLImporter implements Runnable {

    private static XMLImporter importer;
    private static final int DOING_NOTHING = 0;
    private static final int READING_FROM_FILE = 1;
    private static final String SENT_BYTES = "sentBytes";
    private static final String MEM_HEAP = "MEM_HEAP";
    private static final String MEM_NONHEAP = "MEM_NONHEAP";

    private int state = DOING_NOTHING;
    private XMLStreamReader xmlStreamReader;

    private HashMap<String, Integer> poolSizes;
    private HashMap<String, HashMap<String, IbisIdentifier>> ibises;

    private HashMap<String, Object> metrics;

    private HashMap<String, HashMap<IbisIdentifier, Long>> bytesSentMetric;
    private HashMap<String, HashMap<String, Long>> cpuUsageMetric;
    private HashMap<String, HashMap<String, Long>> memSysMetric;

    private XMLImporter() {
        poolSizes = new HashMap<String, Integer>();
        ibises = new HashMap<String, HashMap<String, IbisIdentifier>>();
        metrics = new HashMap<String, Object>();
        metrics.put(SENT_BYTES,
                new HashMap<String, HashMap<IbisIdentifier, Long>>());

        bytesSentMetric = new HashMap<String, HashMap<IbisIdentifier, Long>>();
        cpuUsageMetric = new HashMap<String, HashMap<String, Long>>();
        memSysMetric = new HashMap<String, HashMap<String, Long>>();
    }

    public void clear() {
        poolSizes.clear();
        ibises.clear();
        clearMetrics();
    }

    private void clearMetrics() {
        bytesSentMetric.clear();
        cpuUsageMetric.clear();
        memSysMetric.clear();
    }

    public static XMLImporter getImporter() {
        if (importer == null) {
            importer = new XMLImporter();
            new Thread(importer).start();

        }
        return importer;
    }

    public void openFile(String file) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream input = new FileInputStream(new File("out.xml"));
            xmlStreamReader = inputFactory.createXMLStreamReader(input);
            state = READING_FROM_FILE;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void readFromFile() {
        try {
            boolean endOfUpdateFound = false;
            String elementName;
            String poolName = "";

            while (xmlStreamReader.hasNext() && !endOfUpdateFound) {
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

                if (event == XMLStreamConstants.END_DOCUMENT) {
                    clear();
                    state = DOING_NOTHING;
                }
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (xmlStreamReader.getLocalName().equals("Update")) {
                        endOfUpdateFound = true;
//                        System.out.println(cpuUsageMetric);
//                        System.out.println(memSysMetric);
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private String readPool() {
        String attributeName, attributeValue, poolName = "";
        int poolSize;

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

        return poolName;
    }

    private void readIbis(String poolName) {
        String attributeName, attributeValue;
        String ibisLocation = "", ibisId = "", completeIbisName = "";
        HashMap<String, IbisIdentifier> ibisesInPool;

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

    private void readMetric() {
        String source = "", destination = "", attributeName, attributeValue, poolName = "", type = "", subtype = "";
        float value = 0;
        int i;

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
            }
        }

        if (type.equals("bytesSent")) { // extract the remaining attributes
            readBytesSent(source, destination, poolName, value);
        } else { // it's a memory metric
            readMemoryMetric(source, poolName, type, subtype, (long) value);
        }

    }

    private void readMemoryMetric(String source, String poolName, String type,
            String subtype, long value) {
        HashMap<String, IbisIdentifier> ibisesInPool;
        HashMap<String, Long> helperIbisMemoryMap = null;
        HashMap<String, HashMap<String, Long>> originalMap = null;

        if (source.length() > 0 && poolName.length() > 0) {
            ibisesInPool = ibises.get(poolName);
            IbisIdentifier sourceIbis = null;

            if (ibisesInPool != null) {
                sourceIbis = ibisesInPool.get(source);
                if (sourceIbis != null) {
                    if (type.equals(CPUUsage.CPU)) {
                        helperIbisMemoryMap = cpuUsageMetric.get(source);
                        originalMap = cpuUsageMetric;
                    } else if (type.equals(SystemMemory.MEM_SYS)) {
                        helperIbisMemoryMap = memSysMetric.get(source);
                        originalMap = memSysMetric;
                    }
                    
                    //if there is no entry for this ibis in the map, create it now
                    if (helperIbisMemoryMap == null) {
                        helperIbisMemoryMap = new HashMap<String, Long>();
                        if (originalMap != null) {
                            originalMap.put(source, helperIbisMemoryMap);
                        }
                    }

                    if (subtype.length() > 0) {
                        helperIbisMemoryMap.put(subtype, value);
                    }

                }
            }
        }
    }

    private void readBytesSent(String source, String destination,
            String poolName, float value) {
        HashMap<String, IbisIdentifier> ibisesInPool;
        HashMap<IbisIdentifier, Long> metricsPerIbis;

        if (source.length() > 0 && destination.length() > 0
                && poolName.length() > 0) {
            ibisesInPool = ibises.get(poolName);
            IbisIdentifier sourceIbis = ibisesInPool.get(source);
            IbisIdentifier destIbis = ibisesInPool.get(destination);

            if (sourceIbis != null && destIbis != null) {
                metricsPerIbis = bytesSentMetric.get(source);
                if (metricsPerIbis == null) {
                    metricsPerIbis = new HashMap<IbisIdentifier, Long>();
                    bytesSentMetric.put(source, metricsPerIbis);
                }
                metricsPerIbis.put(destIbis,
                        (long) (value * BytesSentPerSecond.MAX));
            }
        }
    }

    public IbisIdentifier[] getMembers(String poolName) {
        if (ibises.get(poolName) == null) {
            return new IbisIdentifier[0];
        }
        return ibises.get(poolName).values().toArray(new IbisIdentifier[0]);
    }

    public Map<String, Integer> getPoolSizes() throws IOException {
        return poolSizes;
    }

    public Map<IbisIdentifier, Long> getSentBytesPerIbis(String poolName,
            String ibisName) {
        if (bytesSentMetric.get(ibisName) == null) {
            return new HashMap<IbisIdentifier, Long>();
        }
        return bytesSentMetric.get(ibisName);
    }

    public long getCPUorSystemMetric(String ibis, String type, String subtype) {
        if (type.equals(CPUUsage.CPU)) {
            if (cpuUsageMetric.get(ibis) != null) {
                return cpuUsageMetric.get(ibis).get(subtype);
            }
        }
        return 0;
    }

    @Override
    public void run() {
        while (true) {
            switch (state) {
            case DOING_NOTHING:
                break;
            case READING_FROM_FILE:
                readFromFile();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO - when the last update takes place make sure to empty all the data
    // structures
}

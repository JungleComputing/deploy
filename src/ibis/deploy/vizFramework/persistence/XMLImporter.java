package ibis.deploy.vizFramework.persistence;

import ibis.deploy.monitoring.collection.metrics.BytesSentPerSecond;
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
    private static final String SENT_BYTES = "sentBytesPerIbis";

    private int state = DOING_NOTHING;
    private XMLStreamReader xmlStreamReader;

    private HashMap<String, Integer> poolSizes;
    private HashMap<String, HashMap<String, IbisIdentifier>> ibises;

    private HashMap<String, HashMap<String, HashMap<IbisIdentifier, Long>>> metrics;

    private XMLImporter() {
        poolSizes = new HashMap<String, Integer>();
        ibises = new HashMap<String, HashMap<String, IbisIdentifier>>();
        metrics = new HashMap<String, HashMap<String, HashMap<IbisIdentifier, Long>>>();
        metrics.put(SENT_BYTES,
                new HashMap<String, HashMap<IbisIdentifier, Long>>());
    }

    public void clear() {
        poolSizes.clear();
        ibises.clear();
        metrics.get(SENT_BYTES).clear();
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
            String elementName, attributeName, attributeValue, poolName = ""; 
            String ibisLocation, ibisId, source, destination, completeIbisName;
            int poolSize;
            double value;
            HashMap<String, IbisIdentifier> ibisesInPool;
            HashMap<IbisIdentifier, Long> metricsPerIbis;

            while (xmlStreamReader.hasNext() && !endOfUpdateFound) {
                int event = xmlStreamReader.next();
                // if (event == XMLStreamConstants.START_DOCUMENT) {
                // System.out.println("Event Type:START_DOCUMENT");
                // }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    elementName = xmlStreamReader.getLocalName();
                    if (elementName.equals("Update")) {
                        metrics.get(SENT_BYTES).clear();
                    } else if (elementName.equals("Pool")) {

                        poolName = "";
                        poolSize = 0;

                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            attributeName = xmlStreamReader
                                    .getAttributeLocalName(i);
                            attributeValue = xmlStreamReader
                                    .getAttributeValue(i);

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
                                ibises.put(poolName,
                                        new HashMap<String, IbisIdentifier>());
                            }
                        }
                    } else if (elementName.equals("Ibis")) {
                        ibisLocation = "";
                        ibisId = "";

                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            attributeName = xmlStreamReader
                                    .getAttributeLocalName(i);
                            attributeValue = xmlStreamReader
                                    .getAttributeValue(i);

                            if (attributeName.equals("location")) {
                                ibisLocation = attributeValue;
                            } else if (attributeName.equals("id")) {
                                ibisId = attributeValue;
                            }
                        }

                        if (ibisLocation.length() > 0) {
                            IbisIdentifier fakeibis = new FakeIbisIdentifier(
                                    ibisLocation, poolName, ibisId);
                            
                            completeIbisName = Utils.extractFullNameFromIbisIdentifier(fakeibis);

                            ibisesInPool = ibises.get(poolName);
                            if (ibisesInPool == null) {
                                ibisesInPool = new HashMap<String, IbisIdentifier>();
                                ibises.put(poolName, ibisesInPool);
                            }
                            ibisesInPool.put(completeIbisName, fakeibis);
                        }
                    } else if (elementName.equals("Metric")) {
                        source = destination = "";
                        value = 0;
                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            attributeName = xmlStreamReader
                                    .getAttributeLocalName(i);
                            attributeValue = xmlStreamReader
                                    .getAttributeValue(i);

                            if (attributeName.equals("source")) {
                                source = attributeValue;
                            } else if (attributeName.equals("destination")) {
                                destination = attributeValue;
                            } else if (attributeName.equals("value")) {
                                value = Float.parseFloat(attributeValue);
                            } else if (attributeName.equals("poolName")) {
                                poolName = attributeValue;
                            }
                        }

                        if (source.length() > 0 && destination.length() > 0
                                && poolName.length() > 0) {
                            ibisesInPool = ibises.get(poolName);
                            IbisIdentifier sourceIbis = ibisesInPool
                                    .get(source);
                            IbisIdentifier destIbis = ibisesInPool
                                    .get(destination);

                            if (sourceIbis != null && destIbis != null) {
                                metricsPerIbis = metrics.get(SENT_BYTES).get(
                                        source);
                                if (metricsPerIbis == null) {
                                    metricsPerIbis = new HashMap<IbisIdentifier, Long>();
                                    metrics.get(SENT_BYTES).put(source,
                                            metricsPerIbis);
                                }
                                metricsPerIbis.put(destIbis,
                                        (long) (value * BytesSentPerSecond.MAX));
                            }
                        }
                    }

                    for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                        // System.out.println("Attribute Local Name:"
                        // + xmlStreamReader.getAttributeLocalName(i));
                        // System.out.println("Attribute Value:"
                        // + xmlStreamReader.getAttributeValue(i));
                    }

                }
                if (event == XMLStreamConstants.ATTRIBUTE) {
                    // System.out.println("*Event Type:ATTRIBUTE");
                }

                // if (event == XMLStreamConstants.CHARACTERS) {
                // System.out.println("Event Type: CHARACTERS");
                // System.out.println("Text:" + xmlStreamReader.getText());
                // }

                if (event == XMLStreamConstants.END_DOCUMENT) {
                    clear();
                    state = DOING_NOTHING;
                }
                if (event == XMLStreamConstants.END_ELEMENT) {
                    // System.out.println("Event Type: END_ELEMENT");
                    if (xmlStreamReader.getLocalName().equals("Update")) {
                        endOfUpdateFound = true;
                        System.out.println("End of update");
                        //System.out.println(metrics.toString());
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
    
    public IbisIdentifier[] getMembers(String poolName){
        if(ibises.get(poolName) == null){
            return new IbisIdentifier[0];
        }
        return ibises.get(poolName).values().toArray(new IbisIdentifier[0]);
    }
    
    public Map<String, Integer> getPoolSizes() throws IOException {
        return poolSizes;
    }
    
    public Map<IbisIdentifier, Long> getSentBytesPerIbis(String poolName, String ibisName){
        if(metrics.get(SENT_BYTES).get(ibisName) == null){
            return new HashMap<IbisIdentifier, Long>();
        }
        return metrics.get(SENT_BYTES).get(ibisName);
    }

    @Override
    public void run() {
        while (true) {
            switch (state) {
            case DOING_NOTHING:
                break;
            case READING_FROM_FILE:
                System.out.println("Starting update: "
                        + System.currentTimeMillis());
                readFromFile();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    //TODO - when the last update takes place make sure to empty all the data structures
}

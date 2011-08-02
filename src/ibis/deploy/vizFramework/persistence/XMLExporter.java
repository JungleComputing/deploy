package ibis.deploy.vizFramework.persistence;

import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.impl.CollectorImpl;
import ibis.deploy.monitoring.collection.impl.LocationImpl;
import ibis.deploy.monitoring.collection.metrics.CPUUsage;
import ibis.deploy.monitoring.collection.metrics.SystemMemory;
import ibis.ipl.IbisIdentifier;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XMLExporter {

    BufferedWriter writer;
    private String cachedPoolsContents;
    private int state = OUTSIDE_UPDATE_CYCLE;

    private static XMLExporter instance = null;
    private static int OUTSIDE_UPDATE_CYCLE = 0;
    private static int IN_UPDATE_CYCLE = 1;
    private static int WAITING_FOR_CLOSE = 2;

    public XMLExporter() {
    }

    public synchronized void openFile(String file) {
        // System.out.println("open");
        try {
            CollectorImpl.getCollector().setWritingToFile(true);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            writer.write("<Graph>\n");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SingletonObjectNotInstantiatedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void poolsToXML(Map<String, Integer> poolSizes) {
        if (state == IN_UPDATE_CYCLE) {
            // System.out.println("pool");
            String result = "<Update id =\"" + System.currentTimeMillis()
                    + "\">\n";
            for (String pool : poolSizes.keySet()) {
                result += "<Pool name = \"" + pool + "\" size = \""
                        + poolSizes.get(pool) + "\"/>\n";
            }
            try {
                writer.write(result);
                writer.write("</Update>\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void startUpdate() {
        if (state == OUTSIDE_UPDATE_CYCLE) {
            // System.out.println("start");
            try {
                state = IN_UPDATE_CYCLE;
                writer.write("<Update id =\"" + System.currentTimeMillis()
                        + "\">\n");
                if (cachedPoolsContents.length() > 0) { // we have some data
                                                        // saved up, we need to
                                                        // write it
                    writer.write(cachedPoolsContents);
                }
                cachedPoolsContents = "";
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public synchronized void endUpdate() {
        try {
            if (state == IN_UPDATE_CYCLE) {
                // System.out.println("end updaate");
                writer.write("</Update>\n");
            } else if (state == WAITING_FOR_CLOSE) {
                // System.out.println("close");
                writer.write("</Update>\n");
                writer.write("</Graph>\n");
                writer.flush();
                writer.close();
                CollectorImpl.getCollector().setWritingToFile(false);
            }
            state = OUTSIDE_UPDATE_CYCLE;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SingletonObjectNotInstantiatedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void poolToXML(String poolName, IbisIdentifier[] ibises) {

        if (state == IN_UPDATE_CYCLE) {
            // System.out.println("pool");
            String result = "";
            result += "<Pool name = \"" + poolName + "\" size = \""
                    + ibises.length + "\">\n";
            for (IbisIdentifier ibis : ibises) {
                result += "<Ibis location = \"" + ibis.location().toString()
                        + "\" id = \"" + ibis.name() + "\" />\n";
            }
            result += "</Pool>";
            try {
                writer.write(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void cachePoolContents(String poolName,
            IbisIdentifier[] ibises) {
        if (state == OUTSIDE_UPDATE_CYCLE) {
            String result = "";
            result += "<Pool name = \"" + poolName + "\" size = \""
                    + ibises.length + "\">\n";
            for (IbisIdentifier ibis : ibises) {
                result += "<Ibis location = \"" + ibis.location().toString()
                        + "\" id = \"" + ibis.name() + "\" />\n";
            }
            result += "</Pool>\n";

            cachedPoolsContents += result;
        }
    }

    public synchronized void linkMetricToXML(String poolName, String source,
            String destination, float value, String type) {
        if (state == IN_UPDATE_CYCLE) {
            String result = "";

            if (type.equals("Bytes_Sent_Per_Sec")) {
                result = "<Metric type = \"" + "bytesSent" + "\" poolName = \""
                        + poolName + "\" source = \"" + source
                        + "\" destination = \"" + destination + "\" value = \""
                        + value + "\"/>\n";
            }
            try {
                writer.write(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void cpuMetricToXML(String poolName, String source,
            long value, String type, String subtype) {
        if (state == IN_UPDATE_CYCLE) {
            String result = "";
            if (type.equals(CPUUsage.CPU)) {
                result = "<Metric type = \"" + CPUUsage.CPU + "\" subtype = \"" + subtype  + "\" poolName = \""
                        + poolName + "\" source = \"" + source
                        + "\" value = \"" + value + "\"/>\n";
            } else if (type.equals(SystemMemory.MEM_SYS)) {
                result = "<Metric type = \"" + SystemMemory.MEM_SYS + "\" subtype = \"" + subtype  + "\" poolName = \""
                + poolName + "\" source = \"" + source
                + "\" value = \"" + value + "\"/>\n";
            } 

            try {
                writer.write(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public synchronized void clearBuffer() {
        cachedPoolsContents = "";
    }

    public synchronized void closeFile() {
        state = WAITING_FOR_CLOSE;
    }

    public static XMLExporter getInstance() {
        if (instance == null) {
            instance = new XMLExporter();
        }
        return instance;
    }
}

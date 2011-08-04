package ibis.deploy.vizFramework.persistence;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import ibis.ipl.IbisIdentifier;
import ibis.ipl.NoSuchPropertyException;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import ibis.deploy.monitoring.collection.metrics.CPUUsage;
import ibis.deploy.monitoring.collection.metrics.HeapMemory;
import ibis.deploy.monitoring.collection.metrics.NonHeapMemory;
import ibis.deploy.monitoring.collection.metrics.SystemMemory;
import ibis.deploy.monitoring.simulator.FakeService;
import ibis.deploy.monitoring.simulator.FakeRegistryService.State;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ana Vinatoru
 *
 */

public class PersistenceManagementService implements
        ManagementServiceInterface, FakeService {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.vizFramework.persistence.PersistenceManagementService");

    private XMLImporter xmlImporter;
    RegistryServiceInterface reg;

    int currentIteration = 0;

    public PersistenceManagementService(RegistryServiceInterface reg) {
        xmlImporter = XMLImporter.getImporter();
        this.reg = reg;
    }

    public Object[] getAttributes(IbisIdentifier id,
            AttributeDescription... desc) throws IOException,
            NoSuchPropertyException {
        synchronized (this) {

            Object[] result = new Object[desc.length];
            for (int i = 0; i < desc.length; i++) {
                // logger.debug("working on "+desc[i].getAttribute());

                if (desc[i].getBeanName().compareTo(
                        "java.lang:type=OperatingSystem") == 0
                        && desc[i].getAttribute().compareTo(
                                CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_TIME) == 0) {
                    result[i] = xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            CPUUsage.CPU,
                            CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_TIME);
                } else if (desc[i].getBeanName().compareTo(
                        "java.lang:type=Runtime") == 0
                        && desc[i].getAttribute().compareTo(
                                CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_UPTIME) == 0) {
                    result[i] = xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            CPUUsage.CPU,
                            CPUUsage.ATTRIBUTE_NAME_PROCESS_CPU_UPTIME);
                } else if (desc[i].getBeanName().compareTo(
                        "java.lang:type=OperatingSystem") == 0
                        && desc[i]
                                .getAttribute()
                                .compareTo(
                                        CPUUsage.ATTRIBUTE_NAME_PROCESS_AVAILABLE_PROCESSORS) == 0) {
                    result[i] = (int) xmlImporter
                            .getNodeMetric(
                                    Utils.extractFullNameFromIbisIdentifier(id),
                                    CPUUsage.CPU,
                                    CPUUsage.ATTRIBUTE_NAME_PROCESS_AVAILABLE_PROCESSORS);
                } else if (desc[i].getBeanName().compareTo(
                        "java.lang:type=OperatingSystem") == 0
                        && desc[i]
                                .getAttribute()
                                .compareTo(
                                        SystemMemory.ATTRIBUTE_TOTAL_PHYSICAL_MEMORY_SIZE) == 0) {
                    result[i] = (long) xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            SystemMemory.MEM_SYS,
                            SystemMemory.ATTRIBUTE_TOTAL_PHYSICAL_MEMORY_SIZE);
                } else if (desc[i].getBeanName().compareTo(
                        "java.lang:type=OperatingSystem") == 0
                        && desc[i]
                                .getAttribute()
                                .compareTo(
                                        SystemMemory.ATTRIBUTE_FREE_PHYSICAL_MEMORY_SIZE) == 0) {
                    result[i] = (long) xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            SystemMemory.MEM_SYS,
                            SystemMemory.ATTRIBUTE_FREE_PHYSICAL_MEMORY_SIZE);
                } else if (desc[i].getBeanName().compareTo(
                        "java.lang:type=Memory") == 0
                        && desc[i].getAttribute().compareTo("HeapMemoryUsage") == 0) {

                    String[] itemNames = new String[2];
                    itemNames[0] = "used";
                    itemNames[1] = "max";

                    String[] itemDescriptions = new String[2];
                    itemDescriptions[0] = "used";
                    itemDescriptions[1] = "maximum";

                    @SuppressWarnings("rawtypes")
                    OpenType itemTypes[] = new OpenType[] { SimpleType.LONG,
                            SimpleType.LONG };

                    CompositeType type = null;
                    try {
                        type = new CompositeType("dummy", "test", itemNames,
                                itemDescriptions, itemTypes);
                    } catch (OpenDataException e) {
                        logger.error("opendata exception");
                        System.exit(0);
                    }

                    HashMap<String, Long> values = new HashMap<String, Long>();
                    values.put("used", xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            HeapMemory.MEM_HEAP, HeapMemory.USED));

                    values.put("max", xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            HeapMemory.MEM_HEAP, HeapMemory.MAX));

                    CompositeData data = null;
                    try {
                        data = new CompositeDataSupport(type, values);
                    } catch (OpenDataException e) {
                        logger.error("opendata exception");
                        System.exit(0);
                    }
                    result[i] = data;
                } else if (desc[i].getBeanName().compareTo(
                        "java.lang:type=Memory") == 0
                        && desc[i].getAttribute().compareTo(
                                "NonHeapMemoryUsage") == 0) {

                    String[] itemNames = new String[2];
                    itemNames[0] = "used";
                    itemNames[1] = "max";

                    String[] itemDescriptions = new String[2];
                    itemDescriptions[0] = "used";
                    itemDescriptions[1] = "maximum";

                    @SuppressWarnings("rawtypes")
                    OpenType itemTypes[] = new OpenType[] { SimpleType.LONG,
                            SimpleType.LONG };

                    CompositeType type = null;
                    try {
                        type = new CompositeType("dummy", "test", itemNames,
                                itemDescriptions, itemTypes);
                    } catch (OpenDataException e) {
                        logger.error("opendata exception");
                        System.exit(0);
                    }

                    HashMap<String, Long> values = new HashMap<String, Long>();
                    values.put("used", xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            NonHeapMemory.MEM_NON_HEAP, NonHeapMemory.USED));

                    values.put("max", xmlImporter.getNodeMetric(
                            Utils.extractFullNameFromIbisIdentifier(id),
                            NonHeapMemory.MEM_NON_HEAP, NonHeapMemory.MAX));
                    
                    CompositeData data = null;
                    try {
                        data = new CompositeDataSupport(type, values);
                    } catch (OpenDataException e) {
                        logger.error("opendata exception");
                        System.exit(0);
                    }

                    result[i] = data;
                } else if (desc[i].getBeanName().compareTo( //TODO - this isn't used or imported / exported
                        "java.lang:type=Threading") == 0
                        && desc[i].getAttribute().compareTo("ThreadCount") == 0) {

                    result[i] = (int) (Math.random() * 100);
                } else if (desc[i].getBeanName().compareTo("ibis") == 0
                        && desc[i].getAttribute().compareTo(
                                "receivedBytesPerIbis") == 0) {
                    result[i] = xmlImporter.getLinkMetric(id.poolName(),
                            Utils.extractFullNameFromIbisIdentifier(id));
                } else if (desc[i].getBeanName().compareTo("ibis") == 0
                        && desc[i].getAttribute().compareTo("sentBytesPerIbis") == 0) {

                    result[i] = xmlImporter.getLinkMetric(id.poolName(),
                            Utils.extractFullNameFromIbisIdentifier(id));
                } else {
                    throw new NoSuchPropertyException();
                }

            }
            return result;
        }
    }

    // dummy, we already do the update in the XMLImporter
    public void doUpdate() {

    }
}

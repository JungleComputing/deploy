package ibis.deploy.vizFramework.persistence;

import ibis.deploy.monitoring.simulator.FakeService;
import ibis.ipl.IbisIdentifier;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ana Vinatoru
 * 
 */

public class PersistenceRegistryService implements
        ibis.ipl.server.RegistryServiceInterface, FakeService {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.vizFramework.persistence.PersistenceRegistryService");

    private final XMLImporter xmlImporter;

    public PersistenceRegistryService() {
        xmlImporter = XMLImporter.getImporter();
    }

    public synchronized IbisIdentifier[] getMembers(String poolName)
            throws IOException {

        return xmlImporter.getMembers(poolName);
    }

    public Map<String, Integer> getPoolSizes() throws IOException {
        return xmlImporter.getPoolSizes();
    }

    /*
     * --------------------------------------------------------------------------
     * ---------------------- The rest is unneeded by the Collector
     */

    public String[] getLocations(String arg0) throws IOException {
        // Not needed by the collector
        return null;
    }

    public String[] getPools() throws IOException {
        // Not needed by the collector
        return null;
    }

    @Override
    public void doUpdate() {
        // we do nothing here because the XMLImporter already takes care of the
        // periodic updates
    }

}

package ibis.deploy.monitoring.simulator;

import ibis.ipl.IbisIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeRegistryService implements
        ibis.ipl.server.RegistryServiceInterface, FakeService {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.monitoring.simulator.FakeRegistryService");

    public enum State {
        ALIVE, FAILING, DEAD
    };

    double CHANCE_OF_IBIS_FAILURE = 0.001;
    double CHANCE_OF_IBIS_JOIN = 0.001;
    double CHANCE_OF_IBIS_RECOVERY = 0.001;
    int MAX_FAILRATE = 10;

    final int POOLS = 1;
    final int COUNTRIES = 5;
    final int UNIVERSITIES = 1;
    final int CLUSTERS = 1;
    final int IBISES = 2;

    private HashMap<String, IbisIdentifier[]> pools;
    private HashMap<IbisIdentifier, State> ibises;
    private HashMap<IbisIdentifier, Integer> failingIbises;

    public FakeRegistryService() {
        pools = new HashMap<String, IbisIdentifier[]>();
        ibises = new HashMap<IbisIdentifier, State>();
        failingIbises = new HashMap<IbisIdentifier, Integer>();

        for (int p = 0; p < POOLS; p++) {
            String poolName = "pool" + p;
            ArrayList<IbisIdentifier> poolIbises = new ArrayList<IbisIdentifier>();

            for (int c = 0; c < COUNTRIES; c++) {
                String countryName = "country" + c;

                for (int u = 0; u < UNIVERSITIES; u++) {
                    String universityName = "university" + u;

                    for (int s = 0; s < CLUSTERS; s++) {
                        String clusterName = "cluster" + s;

                        for (int i = 0; i < IBISES; i++) {
                            IbisIdentifier fakeibis = new FakeIbisIdentifier(i
                                    + "_" + poolName + "@" + clusterName + "@"
                                    + universityName + "@" + countryName,
                                    poolName);
                            poolIbises.add(fakeibis);
                            ibises.put(fakeibis, State.ALIVE);
                        }
                    }
                }
            }

            if (poolIbises.size() > 0) {
                pools.put(poolName, poolIbises.toArray(new IbisIdentifier[0]));
            }
        }

        if (logger.isInfoEnabled()) {
            System.out.println("FakeRegistry has created " + ibises.size()
                    + " ibises.");
            logger.info("FakeRegistry has created " + ibises.size()
                    + " ibises.");
            logger.info("in " + COUNTRIES + " countries, " + UNIVERSITIES
                    + " universities and " + CLUSTERS + " clusters");
            logger.info("and divided among " + pools.size() + " pools.");
        }

        // Start an update timer for the list mutations
        UpdateTimer timer = new UpdateTimer(this);
        new Thread(timer).start();
    }

    public IbisIdentifier[] getMembers(String poolName) throws IOException {
        synchronized (ibises) {
            return pools.get(poolName);
        }
    }

    public Map<String, Integer> getPoolSizes() throws IOException {
        HashMap<String, Integer> newMap = new HashMap<String, Integer>();
        for (Map.Entry<String, IbisIdentifier[]> pool : pools.entrySet()) {
            newMap.put(pool.getKey(), pool.getValue().length);
        }
        return newMap;
    }

    public HashMap<IbisIdentifier, State> getIbises() {
        synchronized (ibises) {
            HashMap<IbisIdentifier, State> snapshot = new HashMap<IbisIdentifier, State>();
            snapshot.putAll(ibises);
            return snapshot;
        }
    }

    /*
     * --------------------------------------------------------------------------
     * ---------------------- Functions needed by the timer
     */
    public void doUpdate() {
        synchronized (ibises) {
            // Add new ibises to the pools
            if (Math.random() < CHANCE_OF_IBIS_JOIN) {
                // Select a random ibis to clone
                IbisIdentifier iArray[] = ibises.keySet().toArray(
                        new IbisIdentifier[0]);
                if (iArray.length > 0) {
                    IbisIdentifier toCloneIbis = iArray[(int) Math.random()
                            * ibises.size()];
                    String poolName = toCloneIbis.poolName();
                    IbisIdentifier newIbis = new FakeIbisIdentifier(toCloneIbis
                            .location().toString(), poolName);

                    // Add it to the pools
                    IbisIdentifier[] poolIbises = pools.get(poolName);
                    ArrayList<IbisIdentifier> newPoolIbises = new ArrayList<IbisIdentifier>();
                    for (IbisIdentifier id : poolIbises) {
                        newPoolIbises.add(id);
                    }
                    newPoolIbises.add(newIbis);
                    pools.put(poolName,
                            newPoolIbises.toArray(new IbisIdentifier[0]));

                    // And update the state.
                    ibises.put(newIbis, State.ALIVE);

                    logger.debug("A new ibis was added to the universe.");
                }
            }

            // Put new ibises in failure mode.
            if (Math.random() < CHANCE_OF_IBIS_FAILURE) {
                // Select a random ibis to put into failmode
                IbisIdentifier iArray[] = ibises.keySet().toArray(
                        new IbisIdentifier[0]);
                if (iArray.length > 0) {
                    IbisIdentifier failingIbis = iArray[(int) Math.random()
                            * ibises.size()];

                    int failrate = 0;
                    if (failingIbises.containsKey(failingIbis)) {
                        failrate = failingIbises.get(failingIbis);
                    }
                    failingIbises.put(failingIbis, failrate);

                    ibises.put(failingIbis, State.FAILING);
                    logger.debug("Put ibis " + failingIbis + " in fail mode.");
                }
            }

            // Update the list of failing ibises, give them a chance to recover
            // or make them
            // disappear if they have failed for too long.
            HashMap<IbisIdentifier, Integer> failingIbisesSnapshot = new HashMap<IbisIdentifier, Integer>();
            failingIbisesSnapshot.putAll(failingIbises);
            for (Entry<IbisIdentifier, Integer> entry : failingIbisesSnapshot
                    .entrySet()) {
                IbisIdentifier failingIbis = entry.getKey();
                int failrate = entry.getValue();

                if (Math.random() < CHANCE_OF_IBIS_RECOVERY) {
                    failingIbises.remove(failingIbis);
                    ibises.put(failingIbis, State.ALIVE);
                    logger.debug("Ibis " + failingIbis + " is alive!");
                } else {
                    if (failrate > MAX_FAILRATE) {
                        // Remove the failing ibis from the pool first
                        String poolName = failingIbis.poolName();
                        IbisIdentifier[] poolIbises = pools.get(poolName);
                        ArrayList<IbisIdentifier> newPoolIbises = new ArrayList<IbisIdentifier>();
                        for (IbisIdentifier id : poolIbises) {
                            if (!id.equals(failingIbis)) {
                                newPoolIbises.add(id);
                            }
                        }
                        pools.put(poolName,
                                newPoolIbises.toArray(new IbisIdentifier[0]));

                        // And then remove it from the internal lists
                        ibises.remove(failingIbis);
                        failingIbises.remove(failingIbis);

                        logger.debug("Ibis " + failingIbis
                                + " failed too long and has been removed.");
                    } else {
                        failingIbises.put(failingIbis, failrate + 1);
                    }
                }
            }
        }
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

}

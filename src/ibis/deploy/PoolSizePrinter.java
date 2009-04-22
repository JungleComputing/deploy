package ibis.deploy;

import java.util.HashMap;
import java.util.Map;

import ibis.util.ThreadPool;

/**
 * Prints pool statistics when they change.
 */
class PoolSizePrinter implements Runnable {

    private final Deploy deploy;

    private boolean ended;

    public PoolSizePrinter(Deploy deploy) {
        this.deploy = deploy;

        ThreadPool.createNew(this, "pool size printer");
    }

    /**
     * Prints pool statistics when they change.
     */
    public void run() {
        Map<String, Integer> poolSizes = new HashMap<String, Integer>();

        while (!isEnded()) {
            try {
                Map<String, Integer> newSizes = deploy.poolSizes();

                for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
                    String poolName = entry.getKey();
                    int poolSize = entry.getValue();

                    if (!poolSizes.containsKey(poolName)
                            || poolSize != poolSizes.get(poolName)) {
                        System.err.printf(
                                "%tT DEPLOY: Size of pool \"%s\" now %d\n",
                                System.currentTimeMillis(), poolName, poolSize);
                    }
                }

                poolSizes = newSizes;

            } catch (Exception e) {
                // IGNORED
            }
            synchronized (this) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    // IGNORED
                }
            }
        }
    }

    public synchronized void end() {
        ended = true;
        notifyAll();
    }

    public synchronized boolean isEnded() {
        return ended;
    }

}

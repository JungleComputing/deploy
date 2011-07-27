package ibis.deploy.monitoring.collection.impl;

import java.util.concurrent.TimeoutException;

import ibis.deploy.monitoring.collection.exceptions.MetricNotAvailableException;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker extends Thread {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.monitoring.collection.impl.Worker");

    private CollectorImpl c;
    ibis.deploy.monitoring.collection.Element element;
//    int numIbises = 0;
//    int currentCount = 0;

    public Worker() {
        try {
            this.c = CollectorImpl.getCollector();
        } catch (SingletonObjectNotInstantiatedException e) {
            logger.error("Collector not instantiated properly.");
        }
    }

//    public void setNumIbises(int numIbises) {
//        this.numIbises = numIbises;
//        currentCount = 0;
//    }

    public void run() {
//        long startTime = 0;
        while (true) {
            try {
//                if(currentCount == 0){
//                    startTime = System.currentTimeMillis();
//                }
                element = c.getWork(this);

                if (element instanceof LocationImpl) {
                    ((LocationImpl) element).update();
                } else if (element instanceof IbisImpl) {
                    try {
                        ((IbisImpl) element).update();
                    } catch (TimeoutException e) {
                        logger.debug("timed out.");
                    }
                } else {
                    logger.error("Wrong type in work queue.");
                }
//                currentCount++;
//                if (currentCount == numIbises) {
//                    System.out.println("Total time for update: " + (System.currentTimeMillis() - startTime));
//                    currentCount = 0;
//                }
            } catch (InterruptedException e1) {
                // try again
            }
            element = null;
        }
    }
}

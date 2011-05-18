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

	public Worker() {
		try {
			this.c = CollectorImpl.getCollector();
		} catch (SingletonObjectNotInstantiatedException e) {
			logger.error("Collector not instantiated properly.");
		}
	}

	public void run() {
		while (true) {
			try {
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
			} catch (InterruptedException e1) {
				// try again
			}
			element = null;
		}
	}
}

package ibis.deploy.monitoring.collection.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;
import ibis.ipl.support.management.AttributeDescription;

public class ThreadsCount extends ibis.deploy.monitoring.collection.impl.MetricDescriptionImpl implements ibis.deploy.monitoring.collection.MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.metrics.ThreadsCount");

	public ThreadsCount() {
		super();

		name = "THREADS";
		type = MetricType.NODE;

		color[0] = 0.5f;
		color[1] = 0.5f;
		color[2] = 0.5f;

		necessaryAttributes.add(new AttributeDescription("java.lang:type=Threading", "ThreadCount"));

		outputTypes.add(MetricOutput.N);
	}

	public void update(Object[] results, Metric metric) throws IncorrectParametersException {
		ibis.deploy.monitoring.collection.impl.MetricImpl castMetric = ((ibis.deploy.monitoring.collection.impl.MetricImpl)metric);
		if (results[0] instanceof Integer) {
			int num_threads		= (Integer) results[0];		
	
			try {
				castMetric.setValue(MetricModifier.NORM, MetricOutput.N, num_threads);
			} catch (BeyondAllowedRangeException e) {
				logger.debug(name +" metric failed trying to set value out of bounds.");
			}
		} else {
			logger.error("Parameter is not of the required type.");
			throw new IncorrectParametersException();
		}
	}
}

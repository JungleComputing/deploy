package ibis.deploy.monitoring.collection.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;

public class Random extends ibis.deploy.monitoring.collection.impl.MetricDescriptionImpl implements ibis.deploy.monitoring.collection.MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.metrics.Random");

	public Random() {
		super();

		name = "RANDOM";
		type = MetricType.NODE;

		color[0] = (float)Math.random();
		color[1] = (float)Math.random();
		color[2] = (float)Math.random();

		outputTypes.add(MetricOutput.PERCENT);
	}

	public void update(Object[] results, Metric metric) throws IncorrectParametersException {
		ibis.deploy.monitoring.collection.impl.MetricImpl castMetric = ((ibis.deploy.monitoring.collection.impl.MetricImpl)metric);
		if (results[0] == null) {
			float currentValue;
			try {
				currentValue = (Float) metric.getValue(MetricModifier.NORM, MetricOutput.PERCENT);
				if (Math.random() > 0.5) {
					currentValue += Math.random()/10;
				} else {
					currentValue -= Math.random()/10;
				}
	
				currentValue = Math.max(0.0f, currentValue);
				currentValue = Math.min(1.0f, currentValue);
	
				try {
					castMetric.setValue(MetricModifier.NORM, MetricOutput.PERCENT, currentValue);
				} catch (BeyondAllowedRangeException e) {
					logger.debug(name +" metric failed trying to set value out of bounds.");
				}
	
			} catch (OutputUnavailableException e) {
				//This shouldn't happen if the metric is well defined
				e.printStackTrace();
			}
		} else {
			logger.error("Parameter is not of the required type.");
			throw new IncorrectParametersException();
		}
	}
}

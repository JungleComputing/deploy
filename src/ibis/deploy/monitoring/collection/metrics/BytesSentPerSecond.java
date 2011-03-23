package ibis.deploy.monitoring.collection.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.IbisIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;

public class BytesSentPerSecond extends ibis.deploy.monitoring.collection.impl.MetricDescription implements ibis.deploy.monitoring.collection.MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.metrics.BytesSentPerSecond");
		
	public BytesSentPerSecond() {
		super();
		
		name = "Bytes_Sent";		
		type = MetricType.LINK;
		
		color[0] = 0.0f;
		color[1] = 0.5f;
		color[2] = 0.5f;
				
		necessaryAttributes.add(new AttributeDescription("ibis", "sentBytesPerIbis"));
		
		outputTypes.add(MetricOutput.PERCENT);
	}
		
	public void update(Object[] results, Metric metric)  throws IncorrectParametersException {
		ibis.deploy.monitoring.collection.impl.Metric castMetric = ((ibis.deploy.monitoring.collection.impl.Metric)metric);
		HashMap<IbisIdentifier, Number> result = new HashMap<IbisIdentifier, Number>();
		long total = 0;
		
		if (results[0] instanceof Map<?, ?>) {
			for (Map.Entry<?,?> incoming : ((Map<?, ?>) results[0]).entrySet()) {
				if (incoming.getKey() instanceof IbisIdentifier && incoming.getValue() instanceof Long) {
					@SuppressWarnings("unchecked") //we've just checked it!
					Map.Entry<IbisIdentifier, Long> sent = (Entry<IbisIdentifier, Long>) incoming;				
				
					long time_now = System.currentTimeMillis();
					long time_elapsed = time_now - (Long)castMetric.getHelperVariable("time_prev");
					castMetric.setHelperVariable("time_prev", time_now);
					
					float time_seconds = (float)time_elapsed / 1000.0f;
		
					long value = sent.getValue();
					result.put(sent.getKey(), (value / time_seconds));
					total += value;
				} else {
					logger.error("Wrong types for map in parameter.");
					throw new IncorrectParametersException();
				}
			}
		} else {
			logger.error("Parameter is not a map.");
			throw new IncorrectParametersException();
		}
		
		try {
			castMetric.setValue(MetricModifier.NORM, MetricOutput.N, total);
			castMetric.setValue(MetricModifier.NORM, MetricOutput.RPOS, result);
		} catch (BeyondAllowedRangeException e) {
			logger.debug(name +" metric failed trying to set value out of bounds.");
		}
	}
}

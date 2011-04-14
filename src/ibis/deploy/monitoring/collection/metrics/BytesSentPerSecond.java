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

public class BytesSentPerSecond extends ibis.deploy.monitoring.collection.impl.MetricDescriptionImpl implements ibis.deploy.monitoring.collection.MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.metrics.BytesSentPerSecond");
	
	private static final float MAX = 1024; //1/2 GB
	
	public BytesSentPerSecond() {
		super();
		
		name = "Bytes_Sent_Per_Sec";		
		type = MetricType.LINK;
		
		color[0] =  64f/255f;
		color[1] = 156f/255f;
		color[2] = 255f/255f;
				
		necessaryAttributes.add(new AttributeDescription("ibis", "sentBytesPerIbis"));
		
		//outputTypes.add(MetricOutput.N);
		outputTypes.add(MetricOutput.RPOS);
		outputTypes.add(MetricOutput.PERCENT);
	}
		
	public void update(Object[] results, Metric metric)  throws IncorrectParametersException {
		ibis.deploy.monitoring.collection.impl.MetricImpl castMetric = ((ibis.deploy.monitoring.collection.impl.MetricImpl)metric);
		HashMap<IbisIdentifier, Number> result = new HashMap<IbisIdentifier, Number>();
		HashMap<IbisIdentifier, Number> percentResult = new HashMap<IbisIdentifier, Number>();
		long total = 0L;
		
		if (results[0] instanceof Map<?, ?>) {
			long time_now = System.currentTimeMillis();
			long time_elapsed = time_now - (Long)castMetric.getHelperVariable(this.name+"_time_prev");					
			castMetric.setHelperVariable(this.name+"_time_prev", time_now);
			
			for (Map.Entry<?,?> incoming : ((Map<?, ?>) results[0]).entrySet()) {
				if (incoming.getKey() instanceof IbisIdentifier && incoming.getValue() instanceof Long) {
					@SuppressWarnings("unchecked") //we've just checked it!
					Map.Entry<IbisIdentifier, Long> received = (Entry<IbisIdentifier, Long>) incoming;				
														
					if (time_elapsed != 0) {
						float time_seconds = (float)time_elapsed / 1000.0f;
						
						long sent_now = received.getValue();
						long sent = sent_now - (Long)castMetric.getHelperVariable(this.name+"_sent_prev");					
						castMetric.setHelperVariable(this.name+"_sent_prev", sent_now);
						
						total += sent;
						
						long perSec = (long) ((float)sent / time_seconds);
						if (perSec < 0) perSec = 0;
						result.put(received.getKey(), perSec);
						
						/*
						long max_prev = (Long)castMetric.getHelperVariable(this.name+"_max_prev");
						long max = Math.max(max_prev, perSec);
						if (max > max_prev) {
							castMetric.setHelperVariable(this.name+"_max_prev", max);
						} else {
							max = max_prev;
						}
						*/
						
						float percent = (float)perSec/MAX; //(float)max;
						if (percent > 1f) percent = 1f;
						
						percentResult.put(received.getKey(), percent);
					}				
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
			//castMetric.setValue(MetricModifier.NORM, MetricOutput.N, total);
			castMetric.setValue(MetricModifier.NORM, MetricOutput.RPOS, result);
			castMetric.setValue(MetricModifier.NORM, MetricOutput.PERCENT, percentResult);
		} catch (BeyondAllowedRangeException e) {
			logger.debug(name +" metric failed trying to set value out of bounds.");
		}
	}
}

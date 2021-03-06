package ibis.deploy.monitoring.collection.metrics;

import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;
import ibis.ipl.support.management.AttributeDescription;

public class HeapMemory extends ibis.deploy.monitoring.collection.impl.MetricDescriptionImpl implements ibis.deploy.monitoring.collection.MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.metrics.HeapMemory");
	
	public HeapMemory() {
		super();
		
		name = "MEM_HEAP";	
		verboseName = "Java Heap Memory Usage";
		type = MetricType.NODE;
				
		color[0] = 255f/255f;
		color[1] = 255f/255f;
		color[2] =   0f/255f;
		
		necessaryAttributes.add(new AttributeDescription("java.lang:type=Memory", "HeapMemoryUsage"));
		
		outputTypes.add(MetricOutput.PERCENT);
		outputTypes.add(MetricOutput.RPOS);
	}
	
	public void update(Object[] results, Metric metric) throws IncorrectParametersException {
		ibis.deploy.monitoring.collection.impl.MetricImpl castMetric = ((ibis.deploy.monitoring.collection.impl.MetricImpl)metric);
		if (results[0] instanceof CompositeData) {
			CompositeData received	= (CompositeData) results[0];
			
			long mem_max  = (Long) received.get("max");
			if (mem_max < 0) mem_max = 0;
			long mem_used = (Long) received.get("used");
			if (mem_used < 0) mem_used = 0;
			
			try {			 
				castMetric.setValue(MetricModifier.NORM, MetricOutput.PERCENT, (float) mem_used / (float) mem_max);
				castMetric.setValue(MetricModifier.NORM, MetricOutput.RPOS, (float) mem_used);
				castMetric.setValue(MetricModifier.MAX, MetricOutput.RPOS, (float) mem_max);
			} catch (BeyondAllowedRangeException e) {
				logger.debug(name +" metric failed trying to set value out of bounds.");
			}
		} else {
			logger.error("Parameter is not of the required type.");
			throw new IncorrectParametersException();
		}
	}
}

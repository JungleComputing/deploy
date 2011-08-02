package ibis.deploy.monitoring.collection.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;
import ibis.ipl.support.management.AttributeDescription;

public class CPUUsage extends ibis.deploy.monitoring.collection.impl.MetricDescriptionImpl implements ibis.deploy.monitoring.collection.MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.metrics.CPUUsage");
	
	public static final String CPU_PREV = "cpu_prev";
	public static final String UPT_PREV = "upt_prev";
	public static final String CPU = "CPU";
	
	public static final String ATTRIBUTE_NAME_PROCESS_CPU_TIME = "ProcessCpuTime";
	public static final String ATTRIBUTE_NAME_PROCESS_CPU_UPTIME = "Uptime";
	public static final String ATTRIBUTE_NAME_PROCESS_AVAILABLE_PROCESSORS = "AvailableProcessors";
		
	public CPUUsage() {
		super();
		
		name = CPU;
		type = MetricType.NODE;
				
		color[0] = 255f/255f;
		color[1] =   0f/255f;
		color[2] =   0f/255f;
				
		necessaryAttributes.add(new AttributeDescription("java.lang:type=OperatingSystem", ATTRIBUTE_NAME_PROCESS_CPU_TIME));
		necessaryAttributes.add(new AttributeDescription("java.lang:type=Runtime", ATTRIBUTE_NAME_PROCESS_CPU_UPTIME));
		necessaryAttributes.add(new AttributeDescription("java.lang:type=OperatingSystem", ATTRIBUTE_NAME_PROCESS_AVAILABLE_PROCESSORS));
		
		outputTypes.add(MetricOutput.PERCENT);
	}
	
	public void update(Object[] results, Metric metric) throws IncorrectParametersException {
		ibis.deploy.monitoring.collection.impl.MetricImpl castMetric = ((ibis.deploy.monitoring.collection.impl.MetricImpl)metric);
		if (results[0] instanceof Long && results[1] instanceof Long &&	results[2] instanceof Integer) {
			long cpu_elapsed 	= (Long)	results[0] - (Long) castMetric.getHelperVariable(CPU_PREV);
			long upt_elapsed	= (Long)	results[1] - (Long) castMetric.getHelperVariable(UPT_PREV);
			int num_cpus		= (Integer) results[2];
			
			// Found at http://forums.sun.com/thread.jspa?threadID=5305095 to be the correct calculation for CPU usage
			float cpuUsage = Math.min(99F, cpu_elapsed / (upt_elapsed * 10000F * num_cpus));
			
			castMetric.setHelperVariable(CPU_PREV, cpu_elapsed);
			castMetric.setHelperVariable(UPT_PREV, upt_elapsed);
			
			// we need this for data import / export
			castMetric.setHelperVariable(ATTRIBUTE_NAME_PROCESS_CPU_TIME, (Long)results[0]);
			castMetric.setHelperVariable(ATTRIBUTE_NAME_PROCESS_CPU_UPTIME, (Long)results[1]);
			castMetric.setHelperVariable(ATTRIBUTE_NAME_PROCESS_AVAILABLE_PROCESSORS, (Integer)results[2]);
			
			try {			 
				castMetric.setValue(MetricModifier.NORM, MetricOutput.PERCENT, (cpuUsage / 100));
			} catch (BeyondAllowedRangeException e) {
				logger.debug(name +" metric failed trying to set value out of bounds.");
			}
			
		} else {
			logger.error("Parameter is not of the required type.");
			throw new IncorrectParametersException();
		}
	}
}

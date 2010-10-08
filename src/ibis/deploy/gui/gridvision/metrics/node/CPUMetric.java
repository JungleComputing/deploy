package ibis.deploy.gui.gridvision.metrics.node;

import ibis.deploy.gui.gridvision.metrics.MetricInterface;
import ibis.ipl.support.management.AttributeDescription;

public class CPUMetric extends NodeMetricsObject implements MetricInterface {
	public static final String NAME = "CPU";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 3;
	public static final Float[] COLOR = {1.0f, 0.0f, 0.0f};
	
	private long cpu_prev, upt_prev;
	
	public CPUMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("java.lang:type=OperatingSystem", "ProcessCpuTime");
		necessaryAttributes[1] = new AttributeDescription("java.lang:type=Runtime", "Uptime");
		necessaryAttributes[2] = new AttributeDescription("java.lang:type=OperatingSystem", "AvailableProcessors");		
	}
	
	public void update(Object[] results) {
		long cpu_elapsed 	= (Long)	results[0] - cpu_prev;
		long upt_elapsed	= (Long)	results[1] - upt_prev;
		int num_cpus		= (Integer) results[2];
		
		// Found at http://forums.sun.com/thread.jspa?threadID=5305095 to be the correct calculation for CPU usage
		float cpuUsage = Math.min(99F, cpu_elapsed / (upt_elapsed * 10000F * num_cpus));
		
		cpu_prev = cpu_elapsed;
		upt_prev = upt_elapsed;
		
		value = (cpuUsage / 100);
	}
	
	public CPUMetric clone() {
		CPUMetric clone = new CPUMetric();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

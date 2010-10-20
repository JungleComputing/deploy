package ibis.deploy.gui.gridvision.metrics.node;

import ibis.deploy.gui.gridvision.metrics.MetricInterface;
import ibis.ipl.support.management.AttributeDescription;

import javax.management.openmbean.CompositeData;

public class HeapMemMetric extends NodeMetricsObject implements MetricInterface {
	public static final String NAME = "MEM_HEAP";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 1.0f, 0.0f};
	
	public HeapMemMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("java.lang:type=Memory", "HeapMemoryUsage");
	}
	
	public void update(Object[] results) {
		CompositeData mem_heap_recvd	= (CompositeData) results[0];
		
		Long mem_heap_max 	= (Long) mem_heap_recvd.get("max");
		Long mem_heap_used 	= (Long) mem_heap_recvd.get("used");
				
		value = (float) mem_heap_used / (float) mem_heap_max;
	}
	
	public HeapMemMetric clone() {
		HeapMemMetric clone = new HeapMemMetric();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

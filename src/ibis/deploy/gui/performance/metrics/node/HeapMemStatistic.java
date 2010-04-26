package ibis.deploy.gui.performance.metrics.node;

import ibis.deploy.gui.performance.metrics.MetricsObjectInterface;
import ibis.ipl.support.management.AttributeDescription;

import javax.management.openmbean.CompositeData;

public class HeapMemStatistic extends NodeMetricsObject implements MetricsObjectInterface {
	public static final String NAME = "MEM";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 1.0f, 0.0f};
	
	public HeapMemStatistic() {
		super();
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
	}
	
	public void update(Object[] results) {
		CompositeData mem_heap_recvd	= (CompositeData) results[0];
		
		Long mem_heap_max 	= (Long) mem_heap_recvd.get("max");
		Long mem_heap_used 	= (Long) mem_heap_recvd.get("used");
				
		value = (float) mem_heap_used / (float) mem_heap_max;
	}
}

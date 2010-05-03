package ibis.deploy.gui.performance.metrics.node;

import ibis.deploy.gui.performance.metrics.MetricsObjectInterface;
import ibis.ipl.support.management.AttributeDescription;

import javax.management.openmbean.CompositeData;

public class NonHeapMemStatistic extends NodeMetricsObject implements MetricsObjectInterface {
	public static final String NAME = "MEM_NONHEAP";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.5f, 1.0f, 0.0f};
	
	public NonHeapMemStatistic() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
	}
	
	public void update(Object[] results) {
		CompositeData mem_nonheap_recvd	= (CompositeData) results[0];
				
		Long mem_nonheap_max 	= (Long) mem_nonheap_recvd.get("max");
		Long mem_nonheap_used 	= (Long) mem_nonheap_recvd.get("used");
		
		value = (float) mem_nonheap_used / (float) mem_nonheap_max;
	}
	
	public NonHeapMemStatistic clone() {
		NonHeapMemStatistic clone = new NonHeapMemStatistic();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

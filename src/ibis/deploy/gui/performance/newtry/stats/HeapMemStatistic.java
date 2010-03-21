package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;

import javax.management.openmbean.CompositeData;

public class HeapMemStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "MEM";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	
	public HeapMemStatistic() {
		super();

		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[1] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
	}
	
	public void update(Object[] results) {
		CompositeData mem_heap_recvd	= (CompositeData) results[0];
		
		Long mem_heap_max 	= (Long) mem_heap_recvd.get("max");
		Long mem_heap_used 	= (Long) mem_heap_recvd.get("used");
				
		value = (float) mem_heap_used / (float) mem_heap_max;
	}
}

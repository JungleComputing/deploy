package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;

import javax.management.openmbean.CompositeData;

public class MEMStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "MEM";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 2;
	public static final int VALUES_COUNT = 2;
	
	public MEMStatistic() {
		super();

		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("java.lang:type=Memory", "HeapMemoryUsage");
		necessaryAttributes[1] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
		values = new Float[VALUES_COUNT];
	}
	
	public void update(Object[] results) {
		CompositeData mem_heap_recvd	= (CompositeData) results[0];	
		CompositeData mem_nonheap_recvd	= (CompositeData) results[1];
		
		Long mem_heap_max 	= (Long) mem_heap_recvd.get("max");
		Long mem_heap_used 	= (Long) mem_heap_recvd.get("used");
				
		Long mem_nonheap_max 	= (Long) mem_nonheap_recvd.get("max");
		Long mem_nonheap_used 	= (Long) mem_nonheap_recvd.get("used");
				
		values[0] = (float) mem_heap_used / (float) mem_heap_max;
		values[1] = (float) mem_nonheap_used / (float) mem_nonheap_max;
	}
}

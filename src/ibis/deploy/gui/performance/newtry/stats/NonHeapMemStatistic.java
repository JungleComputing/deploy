package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;

import javax.management.openmbean.CompositeData;

public class NonHeapMemStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "MEM";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final float[] COLOR = {0.5f, 1.0f, 0.0f};
	
	public NonHeapMemStatistic() {
		super();

		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[1] = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
	}
	
	public void update(Object[] results) {
		CompositeData mem_nonheap_recvd	= (CompositeData) results[0];
				
		Long mem_nonheap_max 	= (Long) mem_nonheap_recvd.get("max");
		Long mem_nonheap_used 	= (Long) mem_nonheap_recvd.get("used");
		
		value = (float) mem_nonheap_used / (float) mem_nonheap_max;
	}
}

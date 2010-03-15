package ibis.deploy.gui.performance.stats;

import javax.management.openmbean.CompositeData;

import ibis.deploy.gui.performance.PerfVis;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;

public class MemUsage extends SingleStat {

	public MemUsage(PerfVis perfvis, IbisIdentifier ibis, Float[] colors) {
		super(perfvis, ibis, colors);
		
		this.name = "MEM_Usage";
	}
	
	@Override
	public void update() throws Exception {	
		AttributeDescription mem_heap  = new AttributeDescription("java.lang:type=Memory", "HeapMemoryUsage");		
		AttributeDescription mem_nonheap  = new AttributeDescription("java.lang:type=Memory", "NonHeapMemoryUsage");
				
		//fetch data
		CompositeData mem_heap_recvd	= (CompositeData) manInterface.getAttributes(ibis, mem_heap)[0];	
		CompositeData mem_nonheap_recvd	= (CompositeData) manInterface.getAttributes(ibis, mem_nonheap)[0];
		
		Long mem_heap_max 	= (Long) mem_heap_recvd.get("max");
		Long mem_heap_used 	= (Long) mem_heap_recvd.get("used");
				
		Long mem_nonheap_max 	= (Long) mem_nonheap_recvd.get("max");
		Long mem_nonheap_used 	= (Long) mem_nonheap_recvd.get("used");
		
		float heapMemUsage 		= (float) mem_heap_used / (float) mem_heap_max;
		float nonHeapMemUsage 	= (float) mem_nonheap_used / (float) mem_nonheap_max;
				
		value = heapMemUsage;
		
		if (perfvis.getSelection() == glName) {
			if (zoomLevel == PerfVis.ZOOM_NODES && zoomStat == PerfVis.STAT_MEM) {
				perfvis.setValue(value);
			}
		}
	}	
}

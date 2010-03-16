package ibis.deploy.gui.performance.hierarchy.stats;

import ibis.deploy.gui.performance.PerfVis;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class HlinkUsage extends Hsinglestat {
	private long cpu_prev;
	private long upt_prev;
	
	

	public HlinkUsage(PerfVis perfvis, IbisIdentifier fromIbis, IbisIdentifier toIbis, Float[] colors) {
		super(perfvis, fromIbis, colors);
	}
	
	@Override
	public void update() throws Exception {	
		AttributeDescription processCpuTime = new AttributeDescription("java.lang:type=OperatingSystem", "ProcessCpuTime");
		AttributeDescription upTime 		= new AttributeDescription("java.lang:type=Runtime", "Uptime");
		AttributeDescription cpus 			= new AttributeDescription("java.lang:type=OperatingSystem", "AvailableProcessors");
		
		//fetch data
		long cpu_elapsed 	= (Long)	manInterface.getAttributes(ibis, processCpuTime)[0] - cpu_prev;
		long upt_elapsed	= (Long)	manInterface.getAttributes(ibis, upTime)[0]		 	- upt_prev;
		int num_cpus		= (Integer) manInterface.getAttributes(ibis, cpus)[0];
					
		// Found at http://forums.sun.com/thread.jspa?threadID=5305095 to be the correct calculation for CPU usage
		float cpuUsage = Math.min(99F, cpu_elapsed / (upt_elapsed * 10000F * num_cpus));
		
		cpu_prev = cpu_elapsed;
		upt_prev = upt_elapsed;
		
		value = cpuUsage / 100;
	}	
}

package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;

public class CPUStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "CPU";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 3;	
	public static boolean SUPPPORTS_AVERAGING = true;
	
	private long cpu_prev, upt_prev;
	
	public CPUStatistic() {
		super();
		
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
}

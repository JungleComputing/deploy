package ibis.deploy.gui.performance.newtry;

import java.util.HashMap;

import javax.management.openmbean.CompositeData;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class IbisManager {
	//Variables needed for the operation of this class
	private PerfVis perfvis;
	private IbisIdentifier ibis;
	private long cpu_prev, upt_prev, sent_prev, sent_max;
	
	//Variables available to the StatsManager
	private IbisIdentifier[] connections;
	private HashMap<String, Float> statValues;
	private float coordinates[];	
	
	public IbisManager(PerfVis perfvis, IbisIdentifier ibis, HashMap<String, AttributeDescription[]> statisticsDescriptions) {
		this.perfvis = perfvis;
		this.ibis = ibis;
		
		statValues = new HashMap<String, Float>();
		coordinates = new float[3];
		
		sent_max = 0;
	}
	
	public void update(String[] statsToUpdate) {
		try {			
			statValues.clear();
			
			AttributeDescription[] descriptionsToUse;
					
			Object[] results = perfvis.getManInterface().getAttributes(ibis, descriptionsToUse);
			
			if (perfvis.getCurrentStat() == PerfVis.STAT_ALL || perfvis.getCurrentStat() == PerfVis.STAT_CPU) {				
				updateCPU(results);
			}
			if (perfvis.getCurrentStat() == PerfVis.STAT_ALL || perfvis.getCurrentStat() == PerfVis.STAT_MEM) {
				updateMEM(results);
			}
			if (perfvis.getCurrentStat() == PerfVis.STAT_ALL || perfvis.getCurrentStat() == PerfVis.STAT_COORDS) {
				updateCoordinates(results);
			}
			if (perfvis.getCurrentStat() == PerfVis.STAT_ALL || perfvis.getCurrentStat() == PerfVis.STAT_LINKS) {
				updateLinks(results);
			}
			
		} catch (Exception e) {					
			e.printStackTrace();
		}
	}
	
	private void updateCPU(Object[] results) {
		long cpu_elapsed 	= (Long)	results[0] - cpu_prev;
		long upt_elapsed	= (Long)	results[1] - upt_prev;
		int num_cpus		= (Integer) results[2];
		
		// Found at http://forums.sun.com/thread.jspa?threadID=5305095 to be the correct calculation for CPU usage
		float cpuUsage = Math.min(99F, cpu_elapsed / (upt_elapsed * 10000F * num_cpus));
		
		cpu_prev = cpu_elapsed;
		upt_prev = upt_elapsed;
		
		statValues.put("CPU", cpuUsage / 100);
	}
	
	private void updateMEM(Object[] results) {
		CompositeData mem_heap_recvd	= (CompositeData) results[0];	
		CompositeData mem_nonheap_recvd	= (CompositeData) results[1];
		
		Long mem_heap_max 	= (Long) mem_heap_recvd.get("max");
		Long mem_heap_used 	= (Long) mem_heap_recvd.get("used");
				
		Long mem_nonheap_max 	= (Long) mem_nonheap_recvd.get("max");
		Long mem_nonheap_used 	= (Long) mem_nonheap_recvd.get("used");
				
		statValues.put("MEM_heap", (float) mem_heap_used / (float) mem_heap_max);
		statValues.put("MEM_nonheap", (float) mem_nonheap_used / (float) mem_nonheap_max);
	}
	
	private void updateCoordinates(Object[] results) {
		Coordinates coord 				= (Coordinates) 	results[0];
		
		double[] unUsableCoords = coord.getCoordinates();
		coordinates[0] = (float) unUsableCoords[0];
		coordinates[1] = (float) unUsableCoords[1];
		coordinates[2] = (float) unUsableCoords[2];
	}
	
	private void updateConnections(Object[] results) {
		this.connections = (IbisIdentifier[])results[0];	
	}
	
	private void updateLinks(Object[] results) {				
		Long bytesSent = (Long) results[0] - sent_prev;
			
		sent_prev = (Long) results[7];
		
		sent_max = Math.max(sent_max, bytesSent);
		statValues.put("BytesSent", (float)bytesSent/(float)sent_max);
	}
		
	public float getValue(String key) throws StatNotRequestedException {
		if (statValues.containsKey(key)) {
			return statValues.get(key);
		} else {
			throw new StatNotRequestedException();
		}
	}
	
	public IbisIdentifier[] getConnections() throws StatNotRequestedException {
		if (statValues.containsKey("BytesSent")) {
			return connections;
		} else {
			throw new StatNotRequestedException();
		}
	}
}
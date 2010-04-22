package ibis.deploy.gui.performance.newtry.dataobjects;

import java.util.HashMap;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.ipl.server.ManagementServiceInterface;

public class IbisConcept {
	protected ManagementServiceInterface manInterface;
	
	protected IbisConcept[] subConcepts;
	protected HashMap<String, Float> nodeMetricsValues;
	protected HashMap<String, Float> linkMetricsValues;
	protected HashMap<String, Float[]> nodeMetricsColors;
	protected HashMap<String, Float[]> linkMetricsColors;	
	
	public IbisConcept(ManagementServiceInterface manInterface) {
		this.manInterface = manInterface;
		
		nodeMetricsValues = new HashMap<String, Float>();
	}
	
	public float getValue(String key) throws StatNotRequestedException {
		if (nodeMetricsValues.containsKey(key))	{
			return nodeMetricsValues.get(key);
		} else {
			throw new StatNotRequestedException();
		}
	}
	
	public IbisConcept[] getSubConcepts() {
		return subConcepts;
	}
	
	public HashMap<String, Float> getMonitoredNodeMetrics() {
		return nodeMetricsValues;
	}
	
	public HashMap<String, Float> getMonitoredLinkMetrics() {
		return linkMetricsValues;
	}
	
	public HashMap<String, Float[]> getMetricsColors() {
		return nodeMetricsColors;
	}
	
	public HashMap<String, Float[]> getLinkColors() {		
		return linkMetricsColors;
	}	
}

package ibis.deploy.gui.performance.dataholders;

import java.util.HashMap;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.ipl.server.ManagementServiceInterface;

public class IbisConcept {
	protected ManagementServiceInterface manInterface;

	protected HashMap<String, Float> nodeMetricsValues;
	protected HashMap<String, Float> linkMetricsValues;
	protected HashMap<String, Float[]> nodeMetricsColors;
	protected HashMap<String, Float[]> linkMetricsColors;	
	
	public IbisConcept(ManagementServiceInterface manInterface) {
		this.manInterface = manInterface;
		
		nodeMetricsValues = new HashMap<String, Float>();
		linkMetricsValues = new HashMap<String, Float>();
	}
	
	public float getValue(String key) throws StatNotRequestedException {
		if (nodeMetricsValues.containsKey(key))	{
			return nodeMetricsValues.get(key);
		} else {
			System.out.println(key +" was not requested.");
			throw new StatNotRequestedException();
		}
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

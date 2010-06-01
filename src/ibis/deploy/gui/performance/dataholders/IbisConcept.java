package ibis.deploy.gui.performance.dataholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;

public class IbisConcept {
	protected ManagementServiceInterface manInterface;

	protected HashMap<String, Float> nodeMetricsValues;
	protected HashMap<IbisIdentifier, Map<String, Float>> linkMetricsValues;
	protected HashMap<String, Float[]> nodeMetricsColors;
	protected HashMap<String, Float[]> linkMetricsColors;	
	protected HashSet<IbisIdentifier> connections;
	
	public IbisConcept(ManagementServiceInterface manInterface) {
		this.manInterface = manInterface;
		
		nodeMetricsValues = new HashMap<String, Float>();
		nodeMetricsColors = new HashMap<String, Float[]>();
		linkMetricsValues = new HashMap<IbisIdentifier, Map<String, Float>>();
		linkMetricsColors = new HashMap<String, Float[]>();
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
	
	public Set<String> getMonitoredLinkMetrics() {
		HashSet<String> newSet = new HashSet<String>(); 
		for (Entry<IbisIdentifier, Map<String, Float>> entry : linkMetricsValues.entrySet()) {
			for (Entry<String, Float> entry2 : entry.getValue().entrySet()) {
				newSet.add(entry2.getKey());
			}
		}
		return newSet;
	}
	
	public Set<IbisIdentifier> getConnections() {
		return connections;
	}
		
	public HashMap<IbisIdentifier, Map<String, Float>> getLinkValues() {		
		return linkMetricsValues;
	}
	
	public HashMap<String, Float[]> getMetricsColors() {
		return nodeMetricsColors;
	}
	
	public HashMap<String, Float[]> getLinkColors() {		
		return linkMetricsColors;
	}	
}

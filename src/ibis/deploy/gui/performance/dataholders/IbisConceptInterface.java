package ibis.deploy.gui.performance.dataholders;

import java.util.HashMap;
import java.util.Set;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;

public interface IbisConceptInterface {
	public float getValue(String key) throws StatNotRequestedException;
	
	public IbisConcept[] getSubConcepts();
	
	public HashMap<String, Float> getMonitoredNodeMetrics();
	
	public Set<String> getMonitoredLinkMetrics();
}

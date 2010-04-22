package ibis.deploy.gui.performance.newtry.dataobjects;

import java.util.HashMap;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;

public interface IbisConceptInterface {
	public float getValue(String key) throws StatNotRequestedException;
	
	public IbisConcept[] getSubConcepts();
	
	public HashMap<String, Float> getMonitoredNodeMetrics();
}

package ibis.deploy.gui.gridvision.dataholders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.ipl.IbisIdentifier;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public interface IbisConceptInterface {
	public float getValue(String key) throws StatNotRequestedException;
	
	public IbisConceptInterface[] getSubConcepts();
	
	public HashMap<String, Float> getMonitoredNodeMetrics();
	
	public Set<String> getMonitoredLinkMetrics();
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics);
	
	public void update() throws NoSuitableModuleException, StatNotRequestedException;
			
	public HashMap<IbisIdentifier, Map<String, Float>> getLinkValues();
	
	public HashMap<String, Float[]> getMetricsColors();
	
	public HashMap<String, Float[]> getLinkColors();
}

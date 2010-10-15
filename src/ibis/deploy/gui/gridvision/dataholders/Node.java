package ibis.deploy.gui.gridvision.dataholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.deploy.gui.gridvision.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Node extends ibis.deploy.gui.gridvision.dataholders.IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {	
	private IbisIdentifier name;
	
	public Node(ibis.deploy.gui.gridvision.interfaces.IbisConcept parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, String siteName, IbisIdentifier name) {
		super(parent, manInterface, regInterface, initialMetrics);		
		this.name = name;
	}
	
	public void update() throws NoSuitableModuleException {		
		HashMap<String, Float> newNodeMetricsValues = new HashMap<String, Float>();
		HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> newLinkMetricsValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		HashSet<IbisIdentifier> newConnections = new HashSet<IbisIdentifier>();		
		
		try {
			int size = 0;
			for (Metric metric : currentlyGatheredMetrics) {
				size += metric.getAttributesCountNeeded();
			}
			
			AttributeDescription[] requestArray = new AttributeDescription[size]; 
			int j=0;
			for (Metric metric : currentlyGatheredMetrics) {
				AttributeDescription[] tempArray = metric.getNecessaryAttributes();
				for (int i=0; i < tempArray.length; i++) {
					requestArray[j] = tempArray[i];
					j++;
				}
			}
			
			Object[] results = manInterface.getAttributes(name, requestArray);
			
			j=0;			
			for (Metric metric : currentlyGatheredMetrics) {
				Object[] partialResults = new Object[metric.getAttributesCountNeeded()];				
				for (int i=0; i < partialResults.length ; i++) {
					partialResults[i] = results[j];	
					j++;
				}
				metric.update(partialResults);
								
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					newNodeMetricsValues.put(metric.getName(), metric.getValue());
				
				} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
					Map<IbisIdentifier, Float> values = ((LinkMetricsMap) metric).getValues();
										
					for (IbisIdentifier ibis : values.keySet()) {
						if (!newLinkMetricsValues.containsKey(ibis)) {								
							newLinkMetricsValues.put(ibis, new HashMap<String, Float>());				
						}
						newLinkMetricsValues.get(ibis).put(metric.getName(), values.get(ibis));
						newConnections.add(ibis);
					}
				}
			}
		} catch (Exception e) {			
			e.printStackTrace();
		} 
		
		synchronized(this) {			
			nodeMetricsAvgValues = newNodeMetricsValues;
			linkMetricsAvgValues = newLinkMetricsValues;
		}
	}
}

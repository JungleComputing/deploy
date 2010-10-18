package ibis.deploy.gui.gridvision.dataholders;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.MetricsManager;
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
	private IbisIdentifier ii;
	
	public Node(MetricsManager mm, ibis.deploy.gui.gridvision.interfaces.IbisConcept parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, String siteName, IbisIdentifier ii) {
		super(mm, parent, manInterface, regInterface, initialMetrics);		
		this.ii = ii;
		mm.registerIbis(ii, this);
	}
	
	@Override
	public void update() throws NoSuitableModuleException, StatNotRequestedException {
		HashMap<String, Float> newNodeMetricsValues = new HashMap<String, Float>();
		HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>> newLinkMetricsValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Map<String, Float>>();
		
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
			
			Object[] results = manInterface.getAttributes(ii, requestArray);
			
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
					HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Float> transformedValues = new HashMap<ibis.deploy.gui.gridvision.interfaces.IbisConcept, Float>();
					
					Map<IbisIdentifier, Float> values = ((LinkMetricsMap) metric).getValues();
					for (IbisIdentifier ibis : values.keySet()) {
						transformedValues.put(mm.getConcept(ibis),values.get(ibis));						
					}
					
					for (ibis.deploy.gui.gridvision.interfaces.IbisConcept ic : transformedValues.keySet()) {
						if (!newLinkMetricsValues.containsKey(ic)) {								
							newLinkMetricsValues.put(ic, new HashMap<String, Float>());				
						}
						newLinkMetricsValues.get(ic).put(metric.getName(), transformedValues.get(ic));
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

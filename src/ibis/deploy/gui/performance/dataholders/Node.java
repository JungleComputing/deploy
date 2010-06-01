package ibis.deploy.gui.performance.dataholders;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.MetricsList;
import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.Metric;
import ibis.deploy.gui.performance.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.performance.metrics.node.NodeMetricsObject;
import ibis.deploy.gui.performance.metrics.special.ConnStatistic;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Node extends IbisConcept implements IbisConceptInterface {
	//Variables needed for the operation of this class	
	private String siteName;
	private IbisIdentifier name;	
	
	private IbisIdentifier[] connectedIbises;
	private MetricsList metrics;
	
	public Node(ManagementServiceInterface manInterface, String siteName, IbisIdentifier name) {
		super(manInterface);
		this.siteName = siteName;
		this.name = name;
		connectedIbises = new IbisIdentifier[0];
		
		metrics = new MetricsList();
	}
	
	public void setCurrentlyGatheredMetrics(MetricsList newMetrics) {
		metrics.clear();
		for (Metric metric : newMetrics) {
			metrics.add(metric.clone());
			
			if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {			
				nodeMetricsColors.put(metric.getName(), metric.getColor());
			//} else if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {				
			//	linkMetricsColors.put(metric.getName(), metric.getColor());
			} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
				linkMetricsColors.put(metric.getName(), metric.getColor());					
			}
		}
	}
	
	public void update() throws NoSuitableModuleException {
		//First, clear the Maps with the values, to be refilled with the newly requested entries
		nodeMetricsValues.clear();
		linkMetricsValues.clear();
		try {
			int size = 0;
			for (Metric metric : metrics) {
				size += metric.getAttributesCountNeeded();
			}
			
			AttributeDescription[] requestArray = new AttributeDescription[size]; 
			int j=0;
			for (Metric metric : metrics) {
				AttributeDescription[] tempArray = metric.getNecessaryAttributes();
				for (int i=0; i < tempArray.length; i++) {
					requestArray[j] = tempArray[i];
					j++;
				}
			}
			
			Object[] results = manInterface.getAttributes(name, requestArray);
			
			j=0;			
			for (Metric metric : metrics) {
				Object[] partialResults = new Object[metric.getAttributesCountNeeded()];
				for (int i=0; i < partialResults.length ; i++) {
					partialResults[i] = results[j];	
					j++;
				}
				metric.update(partialResults);
								
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					nodeMetricsValues.put(metric.getName(), metric.getValue());
				//} else if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {
				//	linkMetricsValues.put(metric.getName(), metric.getValue());
				} else if (metric.getGroup() == LinkMetricsMap.METRICSGROUP) {
					Map<IbisIdentifier, Float> values = ((LinkMetricsMap) metric).getValues();
										
					for (IbisIdentifier ibis : values.keySet()) {
						if (!linkMetricsValues.containsKey(ibis)) {
							linkMetricsValues.put(ibis, new HashMap<String, Float>());				
						}
						linkMetricsValues.get(ibis).put(metric.getName(), values.get(ibis));
						connections.add(ibis);
					}
				} else if (metric.getName().equals(ConnStatistic.NAME)) {
					IbisIdentifier[] connections = ((ConnStatistic)metric).getIbises(); 
					connectedIbises = connections;
				}
			}
		} catch (MethodNotOverriddenException e) {
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		} 		
	}
	
	public IbisIdentifier[] getConnectedIbises() {
		return connectedIbises;
	}	

	public String getSiteName() {
		return siteName;
	}

	public IbisIdentifier getName() {
		return name;
	}
	
	public IbisConcept[] getSubConcepts() {	
		return null;
	}
	
	public float getValue(String key) throws StatNotRequestedException {
		if (nodeMetricsValues.containsKey(key))	{
			return nodeMetricsValues.get(key);
		//} else if (linkMetricsValues.containsKey(key))	{
		//	return linkMetricsValues.get(key);
		} else {			
			throw new StatNotRequestedException();
		}
	}
	
	public Map<String, Float> getLinkValueMap(IbisIdentifier ibis) {
		return linkMetricsValues.get(ibis);
	}
}

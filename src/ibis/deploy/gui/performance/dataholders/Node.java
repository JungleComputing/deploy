package ibis.deploy.gui.performance.dataholders;

import java.util.ArrayList;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.MetricsObject;
import ibis.deploy.gui.performance.metrics.link.LinkMetricsObject;
import ibis.deploy.gui.performance.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;

public class Node extends IbisConcept implements IbisConceptInterface {
	//Variables needed for the operation of this class	
	private String siteName;
	private IbisIdentifier name;	
	
	private IbisIdentifier[] connectedIbises;
	private ArrayList<MetricsObject> metrics;
	
	public Node(ManagementServiceInterface manInterface, String siteName, IbisIdentifier name) {
		super(manInterface);
		this.siteName = siteName;
		this.name = name;
		
		metrics = new ArrayList<MetricsObject>();
	}
	
	public void setCurrentlyGatheredMetrics(ArrayList<MetricsObject> newMetrics) {
		metrics.clear();
		for (MetricsObject metric : newMetrics) {
			metrics.add(metric.clone());
		}
	}
	
	public void update() {
		//First, clear the Maps with the values, to be refilled with the newly requested entries
		nodeMetricsValues.clear();
		linkMetricsValues.clear();
		try {
			int size = 0;
			for (MetricsObject metric : metrics) {
				size += metric.getAttributesCountNeeded();
			}
			
			AttributeDescription[] requestArray = new AttributeDescription[size]; 
			int j=0;
			for (MetricsObject metric : metrics) {
				AttributeDescription[] tempArray = metric.getNecessaryAttributes();
				for (int i=0; i < tempArray.length; i++) {
					requestArray[j] = tempArray[i];
					j++;
				}
			}
			
			Object[] results = manInterface.getAttributes(name, requestArray);
			
			j=0;
			for (MetricsObject metric : metrics) {
				Object[] partialResults = new Object[metric.getAttributesCountNeeded()];
				for (int i=0; i < partialResults.length ; i++) {
					partialResults[i] = results[j];	
					j++;
				}
				metric.update(partialResults);
								
				if (metric.getGroup() == NodeMetricsObject.METRICSGROUP) {
					nodeMetricsValues.put(metric.getName(), metric.getValue());
					nodeMetricsColors.put(metric.getName(), metric.getColor());
				} else if (metric.getGroup() == LinkMetricsObject.METRICSGROUP) {
					linkMetricsValues.put(metric.getName(), metric.getValue());
					linkMetricsColors.put(metric.getName(), metric.getColor());
				}
			}
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
		} else {
			System.out.println(key +" was not requested.");
			throw new StatNotRequestedException();
		}
	}
}

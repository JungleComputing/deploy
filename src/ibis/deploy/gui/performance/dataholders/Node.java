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
	
	public Node(ManagementServiceInterface manInterface, String siteName, IbisIdentifier name) {
		super(manInterface);
		this.siteName = siteName;
		this.name = name;
	}
	
	public void update(ArrayList<MetricsObject> metrics) {
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
			
			//Make an array out of the wanted statistics objects and request their values for this ibis
			/*
			ArrayList<AttributeDescription> requestList = new ArrayList<AttributeDescription>();
			HashMap<MetricsObject, Integer> attributesMap = new HashMap<MetricsObject, Integer>();
			int counter = 0;
			for (MetricsObject metric : metrics) {
				counter += metric.getAttributesCountNeeded();
				attributesMap.put(metric, counter);
				
				requestList.addAll(Arrays.asList(metric.getNecessaryAttributes()));
			}
			
			AttributeDescription[] requestArray = new AttributeDescription[requestList.size()];
			requestList.toArray(requestArray);
			
			Object[] results = manInterface.getAttributes(name, requestArray);
			
			
			
			//for each wanted statistic, make a partial results array of the needed size,
			//and pass it on to the appropriate metrics object
			for (Map.Entry<MetricsObject, Integer> entry : attributesMap.entrySet()) {
				if (entry.getKey().getName().equals(ConnStatistic.NAME)) {
					connectedIbises = ((ConnStatistic) entry.getKey()).getIbises();
				} else {
					Object[] partialResults = new Object[entry.getKey().getAttributesCountNeeded()];
					for (int i=0; i<partialResults.length; i++) {
						partialResults[i] = results[entry.getValue()+i];
					}
					entry.getKey().update(partialResults);
					
					//After this step, we have added an entry into the statValues map with the name and the value
					// of the updated metric.
					if (entry.getKey().getGroup() == NodeMetricsObject.METRICSGROUP) {
						nodeMetricsValues.put(entry.getKey().getName(), entry.getKey().getValue());
						nodeMetricsColors.put(entry.getKey().getName(), entry.getKey().getColor());
					} else if (entry.getKey().getGroup() == LinkMetricsObject.METRICSGROUP) {
						linkMetricsValues.put(entry.getKey().getName(), entry.getKey().getValue());
						linkMetricsColors.put(entry.getKey().getName(), entry.getKey().getColor());
					}
				}
			}
			*/
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

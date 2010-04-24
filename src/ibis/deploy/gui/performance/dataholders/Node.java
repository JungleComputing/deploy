package ibis.deploy.gui.performance.dataholders;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.metrics.MetricsObject;
import ibis.deploy.gui.performance.metrics.link.LinkMetricsObject;
import ibis.deploy.gui.performance.metrics.node.NodeMetricsObject;
import ibis.deploy.gui.performance.metrics.special.ConnStatistic;
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
		this.subConcepts = null;
	}
	
	public void update(HashMap<MetricsObject, Integer> attributeIndexes) {
		//First, clear the Maps with the values, to be refilled with the newly requested entries
		nodeMetricsValues.clear();
		linkMetricsValues.clear();
		try {		
			//Make an array out of the wanted statistics objects and request their values for this ibis
			AttributeDescription[] request = (AttributeDescription[]) attributeIndexes.keySet().toArray();
			Object[] results = manInterface.getAttributes(name, request);
			
			//for each wanted statistic, make a partial results array of the needed size,
			//and pass it on to the appropriate statistics object
			for (Map.Entry<MetricsObject, Integer> entry : attributeIndexes.entrySet()) {
				if (entry.getKey().getName().equals(ConnStatistic.NAME)) {
					connectedIbises = ((ConnStatistic) entry.getKey()).getIbises();
				} else {
					Object[] partialResults = new Object[entry.getKey().getAttributesCountNeeded()];
					for (int i=0; i<partialResults.length; i++) {
						partialResults[i] = results[entry.getValue()+i];
					}
					entry.getKey().update(partialResults);
					
					//After this step, we have added an entry into the statValues map with the name and the value
					// of the updated statistic.
					if (entry.getKey().getGroup() == NodeMetricsObject.METRICSGROUP) {
						nodeMetricsValues.put(entry.getKey().getName(), entry.getKey().getValue());
						nodeMetricsColors.put(entry.getKey().getName(), entry.getKey().getColor());
					} else if (entry.getKey().getGroup() == LinkMetricsObject.METRICSGROUP) {
						linkMetricsValues.put(entry.getKey().getName(), entry.getKey().getValue());
						linkMetricsColors.put(entry.getKey().getName(), entry.getKey().getColor());
					}
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
}

package ibis.deploy.gui.performance.newtry.dataobjects;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.stats.ConnStatistic;
import ibis.deploy.gui.performance.newtry.stats.StatisticsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class Node extends DataObject {
	//Variables needed for the operation of this class	
	private String siteName;
	private IbisIdentifier name;
	
	private HashMap<String, Float> statValues;
	private IbisIdentifier[] connectedIbises;
	
	public Node(PerfVis perfvis, String siteName, IbisIdentifier name) {
		super(perfvis);
		this.siteName = siteName;
		this.name = name;
		
		statValues = new HashMap<String, Float>();
	}
	
	public void update(HashMap<StatisticsObject, Integer> attributeIndexes) {
		try {		
			//Make an array out of the wanted statistics objects and request their values for this ibis
			AttributeDescription[] request = (AttributeDescription[]) attributeIndexes.keySet().toArray();
			Object[] results = perfvis.getManInterface().getAttributes(name, request);
			
			//for each wanted statistic, make a partial results array of the needed size,
			//and pass it on to the appropriate statistics object
			for (Map.Entry<StatisticsObject, Integer> entry : attributeIndexes.entrySet()) {
				if (entry.getKey().getName().equals(ConnStatistic.NAME)) {
					connectedIbises = entry.getKey().getIbises();
				} else {
					Object[] partialResults = new Object[entry.getKey().getAttributesCountNeeded()];
					for (int i=0; i<partialResults.length; i++) {
						partialResults[i] = results[entry.getValue()+i];
					}
					entry.getKey().update(partialResults);
					
					statValues.put(entry.getKey().getName(), entry.getKey().getValue());					
				}
			}
			
		} catch (Exception e) {					
			e.printStackTrace();
		}
	}
	
	public IbisIdentifier[] getConnectedIbises() {
		return connectedIbises;
	}
		
	public float getValue(String key) throws StatNotRequestedException {
		if (statValues.containsKey(key)) {
			return statValues.get(key);
		} else {
			throw new StatNotRequestedException();
		}
	}

	public String getSiteName() {
		return siteName;
	}

	public IbisIdentifier getName() {
		return name;
	}	
}

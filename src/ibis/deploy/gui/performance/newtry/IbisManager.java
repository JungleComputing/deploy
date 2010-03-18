package ibis.deploy.gui.performance.newtry;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.stats.StatisticsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class IbisManager {
	//Variables needed for the operation of this class
	private PerfVis perfvis;
	private IbisIdentifier ibis;
	
	HashMap<String, Object[]> statValues;
		
	public IbisManager(PerfVis perfvis, IbisIdentifier ibis) {
		this.perfvis = perfvis;
		this.ibis = ibis;
		
		statValues = new HashMap<String, Object[]>();
	}
	
	public void update(HashMap<StatisticsObject, Integer> attributeIndexes) {
		try {		
			//Make an array out of the wanted statistics objects and request their values for this ibis
			AttributeDescription[] request = (AttributeDescription[]) attributeIndexes.keySet().toArray();
			Object[] results = perfvis.getManInterface().getAttributes(ibis, request);
			
			//for each wanted statistic, make a partial results array of the needed size,
			//and pass it on to the appropriate statistics object
			for (Map.Entry<StatisticsObject, Integer> entry : attributeIndexes.entrySet()) {
				Object[] partialResults = new Object[entry.getKey().getAttributesCountNeeded()];
				for (int i=0; i<partialResults.length; i++) {
					partialResults[i] = results[entry.getValue()+i];
				}
				entry.getKey().update(partialResults);
				
				statValues.put(entry.getKey().getName(), entry.getKey().getValues());
			}
			
		} catch (Exception e) {					
			e.printStackTrace();
		}
	}
		
	public Object[] getValues(String key) throws StatNotRequestedException {
		if (statValues.containsKey(key)) {
			return statValues.get(key);
		} else {
			throw new StatNotRequestedException();
		}
	}
}
package ibis.deploy.gui.performance.newtry.dataobjects;

import java.util.ArrayList;
import java.util.List;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.newtry.metrics.MetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;

public class Pool extends IbisConcept implements IbisConceptInterface {
	private String name;
		
	public Pool(ManagementServiceInterface manInterface, String name, Site[] sites) {
		super(manInterface);
		this.name = name;
		this.subConcepts = sites;		
	}

	public String getName() {
		return name;
	}	
	
	public IbisIdentifier[] getIbises() {
		List<IbisIdentifier> result = new ArrayList<IbisIdentifier>();
		for (int i=0; i< subConcepts.length; i++) {
			IbisIdentifier[] nodes = ((Site)subConcepts[i]).getIbises();
			for (int j=0; j<nodes.length; j++) {
				result.add(nodes[j]);
			}
		}
		return (IbisIdentifier[]) result.toArray();		
	}	
	
	public void update() throws StatNotRequestedException {		
		List<MetricsObject> stats = ((Site)subConcepts[0]).getCurrentlyGatheredStatistics();
		for (MetricsObject stat : stats) {
			if (compareStats(stat)) {		
				String key = stat.getName();
				List<Float> results = new ArrayList<Float>();
				for (Site site : (Site[])subConcepts) {			
					results.add(site.getValue(key));
				}
				float total = 0, average = 0;
				for (Float entry : results) {
					total += entry;
				}
				average = total / results.size();
				nodeMetricsValues.put(key, average);
				nodeMetricsColors.put(key, stat.getColor());
			}
		}
	}	
	
	private boolean compareStats(MetricsObject stat) {							
		for (Site site : (Site[])subConcepts) {
			if (!site.getCurrentlyGatheredStatistics().contains(stat)) return false;
		}		
		return true;
	}
	
	public void setCurrentlyGatheredStatistics(List<MetricsObject> currentlyGatheredStatistics) {
		for (Site site : (Site[])subConcepts) {
			site.setCurrentlyGatheredStatistics(currentlyGatheredStatistics);
		}
	}
}

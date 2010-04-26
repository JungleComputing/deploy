package ibis.deploy.gui.performance.dataholders;

import java.util.ArrayList;
import java.util.List;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.MetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;

public class Site extends IbisConcept implements IbisConceptInterface {	
	private String name;	
	
	private ArrayList<MetricsObject> currentlyGatheredStatistics;

	public Site(ManagementServiceInterface manInterface, String name, Node[] nodes) {	
		super(manInterface);
		this.name = name;
				
		this.subConcepts = nodes;
	}
		
	public String getName() {
		return name;
	}

	public IbisIdentifier[] getIbises() {
		IbisIdentifier[] result = new IbisIdentifier[subConcepts.length];
		for (int i=0; i< subConcepts.length; i++) {
			result[i] = ((Node)subConcepts[i]).getName();
		}
		return result;
	}	
	
	public void update() throws StatNotRequestedException {		
		for (MetricsObject stat : currentlyGatheredStatistics) {
			String key = stat.getName();
			List<Float> results = new ArrayList<Float>();
			for (Node node : (Node[])subConcepts) {			
				results.add((Float)node.getValue(key));
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
	
	public void setCurrentlyGatheredStatistics(ArrayList<MetricsObject> currentlyGatheredStatistics) {
		this.currentlyGatheredStatistics = currentlyGatheredStatistics;
	}

	public ArrayList<MetricsObject> getCurrentlyGatheredStatistics() {
		return currentlyGatheredStatistics;
	}
}

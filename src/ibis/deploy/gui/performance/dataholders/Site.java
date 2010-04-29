package ibis.deploy.gui.performance.dataholders;

import java.util.ArrayList;
import java.util.List;

import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.metrics.MetricsObject;
import ibis.deploy.gui.performance.metrics.special.ConnStatistic;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;

public class Site extends IbisConcept implements IbisConceptInterface {	
	private String name;	
	private ArrayList<Node> nodes;
	
	private ArrayList<MetricsObject> currentlyGatheredStatistics;
	
	public Site(ManagementServiceInterface manInterface, ArrayList<MetricsObject> initialStatistics, IbisIdentifier[] poolIbises, String siteName) {	
		super(manInterface);
		this.name = siteName;
		
		this.currentlyGatheredStatistics = initialStatistics;
		this.nodes = new ArrayList<Node>();
		
		String ibisLocationName;
		
		//Determine which ibises belong to this site
		for (int i=0; i<poolIbises.length; i++) {
			ibisLocationName = poolIbises[i].location().toString().split("@")[1];
			
			//And compare all ibises' locations to that sitename
			if (ibisLocationName.compareTo(siteName) == 0) {
				Node node = new Node(manInterface, siteName, poolIbises[i]);
				nodes.add(node);
			}
		}
	}
		
	public String getName() {
		return name;
	}

	public IbisIdentifier[] getIbises() {
		IbisIdentifier[] result = new IbisIdentifier[nodes.size()];
		int i=0;
		for (Node node : nodes) {
			result[i] = node.getName();
			i++;
		}
		return result;
	}	
	
	public void update() throws StatNotRequestedException {	
		nodeMetricsValues.clear();
		linkMetricsValues.clear();
		
		for (Node node : nodes) {			
			node.update(currentlyGatheredStatistics);
		}
		
		for (MetricsObject stat : currentlyGatheredStatistics) {
			if (!stat.getName().equals(ConnStatistic.NAME)) {
				String key = stat.getName();
				List<Float> results = new ArrayList<Float>();
				for (Node node : nodes) {			
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
	}
	
	public void setCurrentlyGatheredStatistics(ArrayList<MetricsObject> currentlyGatheredStatistics) {
		this.currentlyGatheredStatistics = currentlyGatheredStatistics;
	}

	public ArrayList<MetricsObject> getCurrentlyGatheredStatistics() {
		return currentlyGatheredStatistics;
	}
	
	public Node[] getSubConcepts() {
		Node[] result = new Node[nodes.size()];
		nodes.toArray(result);
		return result;
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

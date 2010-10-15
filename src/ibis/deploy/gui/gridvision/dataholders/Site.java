package ibis.deploy.gui.gridvision.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.interfaces.IbisConcept;
import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.deploy.gui.gridvision.metrics.link.LinkMetricsMap;
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Site extends ibis.deploy.gui.gridvision.dataholders.IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {
	private HashMap<IbisIdentifier, Node> ibisesToNodes;
	
	public Site(ibis.deploy.gui.gridvision.interfaces.IbisConcept parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, IbisIdentifier[] poolIbises, String siteName) {
		super(parent, manInterface, regInterface, initialMetrics);
		this.name = siteName;				

		this.ibisesToNodes = new HashMap<IbisIdentifier, Node>();
	
		String ibisLocationName;
		
		//Determine which ibises belong to this site
		for (int i=0; i<poolIbises.length; i++) {
			ibisLocationName = poolIbises[i].location().toString().split("@")[1];
			
			//And compare all ibises' locations to that sitename
			if (ibisLocationName.compareTo(siteName) == 0) {
				Node node = new Node(manInterface, initialMetrics, siteName, poolIbises[i]);
				children.add(node);
				//new Thread(node).start();
				ibisesToNodes.put(poolIbises[i], node);
			}
		}		
	}
	
	public Node getNode(IbisIdentifier ibis) {
		return ibisesToNodes.get(ibis);		
	}

	public IbisIdentifier[] getIbises() {
		synchronized(this) {
			IbisIdentifier[] result = new IbisIdentifier[children.size()];
			int i=0;
			for (Node node : children) {
				result[i] = node.getName();
				i++;
			}
			return result;
		}
	}	
}

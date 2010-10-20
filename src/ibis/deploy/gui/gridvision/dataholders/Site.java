package ibis.deploy.gui.gridvision.dataholders;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.MetricsManager;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class Site extends ibis.deploy.gui.gridvision.dataholders.IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {	
	public Site(MetricsManager mm, ibis.deploy.gui.gridvision.interfaces.IbisConcept parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, IbisIdentifier[] poolIbises, String siteName) {
		super(mm, parent, manInterface, regInterface, initialMetrics);
		this.name = siteName;
	
		String ibisLocationName;
		
		//Determine which ibises belong to this site
		for (int i=0; i<poolIbises.length; i++) {
			ibisLocationName = poolIbises[i].location().toString().split("@")[1];
			
			//And compare all ibises' locations to that sitename
			if (ibisLocationName.compareTo(siteName) == 0) {
				Node node = new Node(mm, this, manInterface, regInterface, initialMetrics, siteName, poolIbises[i]);
				children.add(node);
			}
		}		
	}
}
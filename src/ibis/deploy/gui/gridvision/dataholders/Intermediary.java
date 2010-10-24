package ibis.deploy.gui.gridvision.dataholders;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.MetricsManager;
import ibis.deploy.gui.gridvision.impl.Flock;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class Intermediary extends Flock implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {

	public Intermediary(MetricsManager mm, ibis.deploy.gui.gridvision.interfaces.IbisConcept parent, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, IbisIdentifier[] ibises, String name) {		
		super(mm, parent, manInterface, regInterface, initialMetrics);
		this.name = name;		
		
		Location ibisLocation;
		
		//Determine which ibises belong to this site
		for (int i=0; i<ibises.length; i++) {
			ibisLocation = ibises[i].location();
			
			
			//And compare all ibises' locations to that sitename
			if (ibisLocationName.compareTo(name) == 0) {
				Intermediary child = new Intermediary(mm, this, manInterface, regInterface, initialMetrics, name, ibises[i]);
				children.add(child);
			}
		}		
	}
}

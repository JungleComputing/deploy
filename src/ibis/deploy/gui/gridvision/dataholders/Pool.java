package ibis.deploy.gui.gridvision.dataholders;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ibis.deploy.gui.gridvision.MetricsList;
import ibis.deploy.gui.gridvision.MetricsManager;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class Pool extends ibis.deploy.gui.gridvision.dataholders.IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {

	public Pool(MetricsManager mm, ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, String poolName) {	
		super(mm, null, manInterface, regInterface, initialMetrics);
		this.name = poolName;
		
		//Get the members of this pool
		IbisIdentifier[] ibises = {};
		try {
			ibises = regInterface.getMembers(poolName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Initialize the list of sites
		Set<String> siteNames = new HashSet<String>();
		String[] locationsPerIbis = {};
		try {
			locationsPerIbis = regInterface.getLocations(poolName);
			
			//The site name is after the @ sign, we make this array only contain unique names
			for (int i=0; i<locationsPerIbis.length; i++) {
				locationsPerIbis[i] = locationsPerIbis[i].split("@")[1];
				siteNames.add(locationsPerIbis[i]);
			}			
		} catch (IOException e) {					
			e.printStackTrace();
		}
		
		//For all sites			
		for (String siteName : siteNames) {
			children.add(new Site(mm, this, manInterface, regInterface, initialMetrics.clone(), ibises, siteName));
		}				
	}
}
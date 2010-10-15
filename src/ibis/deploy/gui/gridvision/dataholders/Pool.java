package ibis.deploy.gui.gridvision.dataholders;

import java.io.IOException;
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
import ibis.deploy.gui.gridvision.metrics.node.NodeMetricsObject;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.smartsockets.virtual.NoSuitableModuleException;

public class Pool extends ibis.deploy.gui.gridvision.dataholders.IbisConcept implements ibis.deploy.gui.gridvision.interfaces.IbisConcept {

	public Pool(ManagementServiceInterface manInterface, RegistryServiceInterface regInterface, MetricsList initialMetrics, String poolName) {	
		super(null, manInterface, regInterface, initialMetrics);
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
			children.add(new Site(this, manInterface, regInterface, initialMetrics.clone(), ibises, siteName));
		}				
	}
	
	public IbisIdentifier[] getIbises() {
		synchronized(this) {
			List<IbisIdentifier> result = new ArrayList<IbisIdentifier>();
		
			for (Site site : children) {
				IbisIdentifier[] nodes = site.getIbises();
				for (int j=0; j<nodes.length; j++) {
					result.add(nodes[j]);
				}
			}
			return (IbisIdentifier[]) result.toArray();
		}
	}
}

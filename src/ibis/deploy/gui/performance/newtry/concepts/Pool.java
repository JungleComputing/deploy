package ibis.deploy.gui.performance.newtry.concepts;

import java.util.ArrayList;
import java.util.List;
import ibis.ipl.IbisIdentifier;

public class Pool {
	String name;
	Site[] sites;
	Trunk[] trunks;	
	
	public Pool(String name, Site[] sites, Trunk[] trunks) {
		this.name = name;
		this.sites = sites;
		this.trunks = trunks;
	}

	public String getName() {
		return name;
	}

	public Site[] getSites() {
		return sites;
	}

	public Trunk[] getTrunks() {
		return trunks;
	}
	
	public IbisIdentifier[] getIbises() {
		List<IbisIdentifier> result = new ArrayList<IbisIdentifier>();
		for (int i=0; i< sites.length; i++) {
			IbisIdentifier[] nodes = sites[i].getIbises();
			for (int j=0; j<nodes.length; j++) {
				result.add(nodes[j]);
			}
		}
		return (IbisIdentifier[]) result.toArray();		
	}	
}

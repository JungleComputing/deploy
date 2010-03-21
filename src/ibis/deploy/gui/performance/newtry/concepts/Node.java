package ibis.deploy.gui.performance.newtry.concepts;

import ibis.ipl.IbisIdentifier;

public class Node {
	String siteName;
	IbisIdentifier name;
	
	public Node(String siteName, IbisIdentifier name) {
		this.siteName = siteName;
		this.name = name;
	}

	public String getSiteName() {
		return siteName;
	}

	public IbisIdentifier getName() {
		return name;
	}	
}

package ibis.deploy.gui.performance.newtry.dataobjects;

import ibis.deploy.gui.performance.PerfVis;

public class Link extends DataObject {
	Node from, to;
	
	public Link(PerfVis perfvis, Node from, Node to) {
		super(perfvis);
		this.from = from;
		this.to = to;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}
}

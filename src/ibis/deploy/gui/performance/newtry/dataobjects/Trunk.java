package ibis.deploy.gui.performance.newtry.dataobjects;

import ibis.deploy.gui.performance.PerfVis;

public class Trunk extends DataObject {
	Site from, to;
	
	public Trunk(PerfVis perfvis, Site from, Site to) {
		super(perfvis);
		this.from = from;
		this.to = to;
	}

	public Site getFrom() {
		return from;
	}

	public Site getTo() {
		return to;
	}
}

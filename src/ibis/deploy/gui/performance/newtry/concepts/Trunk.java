package ibis.deploy.gui.performance.newtry.concepts;

public class Trunk {
	Site from, to;
	
	public Trunk(Site from, Site to) {
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

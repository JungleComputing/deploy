package ibis.deploy.gui.performance.newtry.concepts;

public class Link {
	Node from, to;
	
	public Link(Node from, Node to) {
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

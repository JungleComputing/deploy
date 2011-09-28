package ibis.deploy.gui.outputViz.common;

public class DualModel {
	public Model solid, transparent;
	
	public DualModel () {
		solid = null;
		transparent = null;
	}
	
	public DualModel(Model solid, Model transparent) {
		this.solid = solid;
		this.transparent = transparent;
	}
	
	public Model getSolids() {
		return solid;
	}
	
	public Model getTransparents() {
		return transparent;
	}
}

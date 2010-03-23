package ibis.deploy.gui.performance.newtry.dataobjects;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.newtry.Vrarchy.Vobject;

public class DataObject {
	protected Vobject myVisual;
	protected PerfVis perfvis;
	
	public DataObject(PerfVis perfvis) {
		this.perfvis = perfvis;
	}
	
	public int getGLName() {
		return myVisual.getGLName();
	}
	
	public Vobject getVisual() {
		return myVisual;
	}
}

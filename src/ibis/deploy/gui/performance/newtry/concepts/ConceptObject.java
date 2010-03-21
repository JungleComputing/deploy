package ibis.deploy.gui.performance.newtry.concepts;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vobject;

public class ConceptObject {
	protected Vobject myVisual;
	protected PerfVis perfvis;
	
	public ConceptObject(PerfVis perfvis) {
		this.perfvis = perfvis;
	}
	
	public int getGLName() {
		return myVisual.getGLName();
	}
}

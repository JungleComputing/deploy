package ibis.deploy.gui.performance;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.dataholders.IbisConcept;
import ibis.deploy.gui.performance.dataholders.Pool;
import ibis.deploy.gui.performance.visuals.Vpool;

public class VisualManager {
	List<IbisConcept> topConcepts;
	List<Vpool> vpools;
	
	private PerfVis perfvis;

	public VisualManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		topConcepts = new ArrayList<IbisConcept>();
		vpools = new ArrayList<Vpool>();
	}

	public void reinitialize(List<IbisConcept> topConcepts) {
		this.topConcepts = topConcepts;
		
		vpools.clear();
		
		for (IbisConcept pool : topConcepts) {
			vpools.add(new Vpool(perfvis, (Pool)pool));			
		}		
	}
	
	public void update() {
		for (Vpool vpool : vpools) {
			vpool.update();
		}
	}
	
	public void drawConcepts(GL gl, int glMode) {		
		for (Vpool vpool : vpools) {			
			vpool.drawThis(gl, glMode);			
		}
	}
}

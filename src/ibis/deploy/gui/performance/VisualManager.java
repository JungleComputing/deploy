package ibis.deploy.gui.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.dataholders.IbisConcept;
import ibis.deploy.gui.performance.dataholders.Pool;
import ibis.deploy.gui.performance.visuals.Vobject;
import ibis.deploy.gui.performance.visuals.Vpool;

public class VisualManager {
	List<Pool> topConcepts;
	List<Vpool> vpools;
	
	private HashMap<Integer, Vobject> glNameRegistry;
	
	private PerfVis perfvis;
	
	private Float[] origin = {0.0f, 0.0f, 0.0f};
	
	public VisualManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		topConcepts = new ArrayList<Pool>();
		vpools = new ArrayList<Vpool>();
		glNameRegistry = new HashMap<Integer, Vobject>();		
	}

	public void reinitialize(List<Pool> list) {
		this.topConcepts = list;
		
		glNameRegistry.clear();		
		vpools.clear();
		
		for (IbisConcept pool : list) {
			vpools.add(new Vpool(perfvis, this, (Pool)pool));			
		}		
	}
	
	public void update() {
		for (Vpool vpool : vpools) {
			vpool.update();
		}
	}
	
	public void drawConcepts(GL gl, int glMode) {		
		for (Vpool vpool : vpools) {	
			vpool.setLocation(origin);
			vpool.drawThis(gl, glMode);			
		}
	}
	
	public int registerGLObject(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		return name;
	}
}

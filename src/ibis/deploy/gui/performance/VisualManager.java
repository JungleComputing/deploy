package ibis.deploy.gui.performance;

import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;
import javax.swing.JPopupMenu;

import ibis.deploy.gui.performance.dataholders.Pool;
import ibis.deploy.gui.performance.visuals.Vobject;
import ibis.deploy.gui.performance.visuals.Vpool;

public class VisualManager {
	List<Pool> pools;
	List<Vpool> vpools;
	
	private HashMap<Integer, Vobject> glNameRegistry;
	
	private PerfVis perfvis;
	
	private Float[] origin = {0.0f, 0.0f, 0.0f};
	
	public VisualManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		pools = new ArrayList<Pool>();
		vpools = new ArrayList<Vpool>();
		glNameRegistry = new HashMap<Integer, Vobject>();		
	}

	public void reinitialize(List<Pool> list) {
		this.pools = list;
		
		glNameRegistry.clear();		
		vpools.clear();
		
		for (Pool pool : list) {
			vpools.add(new Vpool(perfvis, this, pool));			
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
			vpool.setSeparation(0.5f);
			vpool.drawThis(gl, glMode);			
		}
	}
	
	public int registerGLObject(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		return name;
	}
	
	public Float[] getVisualLocation(int name) {
		return glNameRegistry.get(name).getLocation();		
	}
	
	public PopupMenu getContextSensitiveMenu(int currentSelection) {
		return glNameRegistry.get(currentSelection).getMenu();
		
	}	
}

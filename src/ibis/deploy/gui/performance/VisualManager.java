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
	private HashMap<Integer, Vobject> poolRegistry;
	private HashMap<Integer, Vobject> siteRegistry;
	private HashMap<Integer, Vobject> nodeRegistry;
	private HashMap<Integer, Vobject> linkRegistry;
	private HashMap<Integer, Vobject> metricRegistry;
	
	private PerfVis perfvis;
	
	private Float[] origin = {0.0f, 0.0f, 0.0f};
	
	public VisualManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		pools = new ArrayList<Pool>();
		vpools = new ArrayList<Vpool>();
		glNameRegistry = new HashMap<Integer, Vobject>();
		poolRegistry = new HashMap<Integer, Vobject>();
		siteRegistry = new HashMap<Integer, Vobject>();
		nodeRegistry = new HashMap<Integer, Vobject>();
		linkRegistry = new HashMap<Integer, Vobject>();
		metricRegistry = new HashMap<Integer, Vobject>();
	}

	public void reinitialize(List<Pool> list) {
		this.pools = list;
		
		glNameRegistry.clear();		
		vpools.clear();
		
		for (Pool pool : list) {
			vpools.add(new Vpool(perfvis, this, null, pool));			
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
	
	public int registerPool(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		poolRegistry.put(name, visual);
		return name;
	}
	
	public int registerSite(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		siteRegistry.put(name, visual);
		return name;
	}
	
	public int registerNode(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		nodeRegistry.put(name, visual);
		return name;
	}
	
	public int registerLink(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		linkRegistry.put(name, visual);
		return name;
	}
	
	public int registerMetric(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		metricRegistry.put(name, visual);
		return name;
	}
	
	public Float[] getVisualLocation(int name) {
		return glNameRegistry.get(name).getLocation();		
	}
	
	public PopupMenu getContextSensitiveMenu(int currentSelection) {
		if (glNameRegistry.containsKey(currentSelection)) {
			if (poolRegistry.containsKey(currentSelection)) {
				
			} else if (siteRegistry.containsKey(currentSelection)) {
				
			} else if (nodeRegistry.containsKey(currentSelection)) {
				
			} else if (linkRegistry.containsKey(currentSelection)) {
				
			} else if (metricRegistry.containsKey(currentSelection)) {
				
			}
			return glNameRegistry.get(currentSelection).getMenu();
		} else {
			return new PopupMenu();
		}
	}	
}

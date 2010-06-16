package ibis.deploy.gui.performance;

import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.dataholders.Pool;
import ibis.deploy.gui.performance.visuals.VisualElementInterface;
import ibis.deploy.gui.performance.visuals.Vpool;

public class VisualManager {
	List<Pool> pools;
	List<Vpool> vpools;
	
	private HashMap<Integer, VisualElementInterface> glNameRegistry;
	private HashMap<Integer, VisualElementInterface> poolRegistry;
	private HashMap<Integer, VisualElementInterface> siteRegistry;
	private HashMap<Integer, VisualElementInterface> nodeRegistry;
	private HashMap<Integer, VisualElementInterface> linkRegistry;
	private HashMap<Integer, VisualElementInterface> metricRegistry;
	
	private PerfVis perfvis;
	
	private Float[] origin = {0.0f, 0.0f, 0.0f};
	
	public VisualManager(PerfVis perfvis) {
		this.perfvis = perfvis;
		pools = new ArrayList<Pool>();
		vpools = new ArrayList<Vpool>();
		glNameRegistry = new HashMap<Integer, VisualElementInterface>();
		poolRegistry = new HashMap<Integer, VisualElementInterface>();
		siteRegistry = new HashMap<Integer, VisualElementInterface>();
		nodeRegistry = new HashMap<Integer, VisualElementInterface>();
		linkRegistry = new HashMap<Integer, VisualElementInterface>();
		metricRegistry = new HashMap<Integer, VisualElementInterface>();
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
			vpool.setSeparation(vpool.getRadius()+1.0f);
			
			vpool.setSeparation(0.5f);
			vpool.drawThis(gl, glMode);			
		}
	}
	
	public int registerPool(VisualElementInterface visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		poolRegistry.put(name, visual);
		return name;
	}
	
	public int registerSite(VisualElementInterface visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		siteRegistry.put(name, visual);
		return name;
	}
	
	public int registerNode(VisualElementInterface visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		nodeRegistry.put(name, visual);
		return name;
	}
	
	public int registerLink(VisualElementInterface visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		linkRegistry.put(name, visual);
		return name;
	}
	
	public int registerMetric(VisualElementInterface visual) {
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

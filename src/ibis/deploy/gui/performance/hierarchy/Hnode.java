package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vnode;
import ibis.deploy.gui.performance.hierarchy.stats.*;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.ipl.IbisIdentifier;

import javax.media.opengl.GL;

public class Hnode extends Hobject implements HobjectInterface {	
	//TODO More stats? then give them names and colors
	public static final String[] DISPLAYNAMES = {"CPU", "MEM"};
	public static final Float[][] DISPLAYCOLORS = {Collection.CPU_HIGH_COLOR, Collection.MEM_HIGH_COLOR};
	
	protected Hsinglestat[] theStats;
	
	protected IbisIdentifier ibis;

	public Hnode(PerfVis perfvis, IbisIdentifier ibis) {
		super(perfvis);
		
		this.ibis = ibis;
		
		//TODO More stats!
		theStats = new Hsinglestat[DISPLAYNAMES.length];
		theStats[0] = new HcpuUsage(perfvis, ibis, DISPLAYCOLORS[0]);
		theStats[1] = new HmemUsage(perfvis, ibis, DISPLAYCOLORS[1]);
		
		myVisual = new Vnode(perfvis, DISPLAYCOLORS);	
		this.glName = myVisual.getGLName();
		
		theNames = new String[DISPLAYNAMES.length];
		
		for (int i=0; i < DISPLAYNAMES.length; i++) {
			theNames[i] = ibis + " " + DISPLAYNAMES[i];
		}
	}
	
	public void drawThis(GL gl, int glMode) {		
		if (perfvis.getCurrentZoom() == PerfVis.ZOOM_NODES) {
			try {			
				((Vnode) myVisual).setSize(width, height);
				((Vnode) myVisual).setLocation(location);
				
				((Vnode) myVisual).setForm(perfvis.getCurrentCollectionForm(), perfvis.getCurrentElementForm());
				((Vnode) myVisual).setSeparation(0.0f);
				
				Float[] values = new Float[DISPLAYNAMES.length];
				for (int i=0; i<DISPLAYNAMES.length; i++) {
					values[i] = theStats[i].getValue();
				}
				((Vnode) myVisual).setValues(values);				
				
				((Vnode) myVisual).drawThis(gl, glMode);			
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void update() throws Exception {			
		//update the values
		for (int i=0; i < theStats.length; i++) {
			theStats[i].update();
		}			
				
		//set the HUD value
		if (perfvis.getSelection() == glName) {
			Float[] values = new Float[DISPLAYNAMES.length];
			for (int i=0; i<DISPLAYNAMES.length; i++) {
				values[i] = theStats[i].getValue();
			}
			perfvis.setHUDValues(DISPLAYNAMES, values);				
		}
	}
	
	public float getStat(int i) {
		return theStats[i].getValue();
	}
	
	public IbisIdentifier getIbis() {
		return ibis;
	}
	
}

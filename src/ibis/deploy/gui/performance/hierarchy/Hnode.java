package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.stats.CpuUsage;
import ibis.deploy.gui.performance.stats.MemUsage;
import ibis.deploy.gui.performance.stats.SingleStat;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.ipl.IbisIdentifier;

import javax.media.opengl.GL;

public class Hnode extends Hobject implements HobjectInterface {
	public static final String[] DISPLAYNAMES = {"CPU", "MEM"};
	public static final Float[][] DISPLAYCOLORS = {Collection.CPU_HIGH_COLOR, Collection.MEM_HIGH_COLOR};
	
	protected SingleStat[] theStats;

	public Hnode(PerfVis perfvis, int glName, IbisIdentifier ibis) {
		super(perfvis, glName);
		
		//TODO More stats!
		theStats = new SingleStat[DISPLAYNAMES.length];
		theStats[0] = new CpuUsage(perfvis, ibis, DISPLAYCOLORS[0]);
		theStats[1] = new MemUsage(perfvis, ibis, DISPLAYCOLORS[1]);
		
		theNames = new String[DISPLAYNAMES.length];		
		
		for (int i=0; i < DISPLAYNAMES.length; i++) {
			theNames[i] = ibis + " " + DISPLAYNAMES[i];			
		}
		
		theVobjects = new Collection[1];
		theVobjects[0] = new Collection(perfvis, theNames, DISPLAYCOLORS);		
	}
	
	public void drawThis(GL gl, int glMode) {		
		if (perfvis.getCurrentZoom() == PerfVis.ZOOM_NODES) {
			try {			
				theVobjects[0].setSize(width, height);
				theVobjects[0].setGLName(glName);
				theVobjects[0].setLocation(location);
				
				((Collection) theVobjects[0]).setForm(perfvis.getCurrentCollectionForm(), perfvis.getCurrentElementForm());
				((Collection) theVobjects[0]).setSeparation(0.0f);
				
				Float[] values = {theStats[0].value, theStats[1].value};
				((Collection) theVobjects[0]).setValues(values);				
				
				((Collection) theVobjects[0]).drawThis(gl, glMode);			
			
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
			Float[] values = {theStats[0].value, theStats[1].value};
			perfvis.setHUDValues(theNames, values);				
		}
	}
	
	public float getStat(int i) {
		return theStats[i].value;
	}
	
	public float getCPU() {		
		return theStats[0].value;		
	}
	
	public float getMEM() {		
		return theStats[1].value;		
	}
}

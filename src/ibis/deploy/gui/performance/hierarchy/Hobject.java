package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vobject;

public class Hobject {
	//the visualization root
	protected PerfVis perfvis;	
	
	//the visualized objects
	protected Vobject myVisual;
	String[] theNames;
	
	//variables needed for the visualization of the object
	protected int glName;
	protected float[] location;	
	protected float width;
	protected float height;
	
	public Hobject(PerfVis perfvis) {
		this.perfvis = perfvis;
		
		location = new float[3];
		location[0] = 0.0f;
		location[1] = 0.0f;
		location[2] = 0.0f;
		
		width = 0.25f;
		height = 1.00f;
	}
	
	public void setLocation(float[] locationXYZ) {
		this.location[0] = locationXYZ[0];
		this.location[1] = locationXYZ[1];
		this.location[2] = locationXYZ[2];
	}
	
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	public Vobject getVisual() {
		return myVisual;
	}
}

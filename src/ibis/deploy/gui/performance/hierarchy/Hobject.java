package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vobject;
import ibis.deploy.gui.performance.visuals.VisualizationElement;

public class Hobject {
	private static final int GLNAMEMULT = 0;

	//the visualization root
	protected PerfVis perfvis;	
	
	//the visualized objects
	protected Vobject[] theVobjects;
	String[] theNames;
	
	//variables needed for the visualization of the object
	protected int glName;	
	protected float width;
	protected float height;	
	protected float[] location;
	
	public Hobject() {}
	
	public Hobject(PerfVis perfvis, int glName) {
		this.perfvis = perfvis;
		this.glName = glName;
		
		theVobjects = new Vobject[1];
		
		location = new float[3];
		location[0] = 0.0f;
		location[1] = 0.0f;
		location[2] = 0.0f;
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
}

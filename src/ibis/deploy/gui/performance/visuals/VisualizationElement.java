package ibis.deploy.gui.performance.visuals;

import ibis.deploy.gui.performance.PerfVis;

import javax.media.opengl.glu.GLU;

public class VisualizationElement {		
	public static Float[] CPU_HIGH_COLOR	= {1.0f, 0.0f, 0.0f};
	public static Float[] CPU_AVG_COLOR		= {1.0f, 0.5f, 0.0f};
	public static Float[] CPU_LOW_COLOR		= {1.0f, 1.0f, 0.0f};
	
	public static Float[] MEM_HIGH_COLOR	= {0.5f, 1.0f, 0.0f};
	public static Float[] MEM_AVG_COLOR		= {0.0f, 1.0f, 0.0f};
	public static Float[] MEM_LOW_COLOR		= {0.0f, 1.0f, 0.5f};	
	
	public static Float[] _0FF	= {0.0f, 1.0f, 1.0f};
	public static Float[] _08F	= {0.0f, 0.5f, 1.0f};
	public static Float[] _00F	= {0.0f, 0.0f, 1.0f};
	
	public static Float[] NETWORK_LINK_COLOR= {0.5f, 0.0f, 1.0f};
	public static Float[] _F0F	= {1.0f, 0.0f, 1.0f};
	public static Float[] _F08	= {1.0f, 0.0f, 0.5f};
	
	PerfVis perfvis;
	
	protected float[] location = {0.0f, 0.0f, 0.0f};
	protected int radius;
	
	protected int glName;
	protected float scaleXZ = 1.0f;
	protected float scaleY  = 0.25f;	
	
	GLU glu;
	
	public VisualizationElement() {
	}
	
	public VisualizationElement(PerfVis perfvis, int name) {
		glu = new GLU();
		this.perfvis = perfvis;
		this.glName = name;		
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		
		this.radius = (int) Math.max(width, height);
	}
	
	public int getGLName() {
		return glName;
	}
	
	public void setGLName(int glName) {
		this.glName = glName;
	}
	
	public void setLocation(float x, float y, float z) {
		location[0] = x;
		location[1] = y;
		location[2] = z;
	}
	
	public void setLocation(float[] newLocation) {
		this.location[0] = newLocation[0];
		this.location[1] = newLocation[1];
		this.location[2] = newLocation[2];
	}
	
	public void setRelativeX(float x) {
		location[0] += x;
	}
	public void setRelativeY(float y) {
		location[1] += y;
	}
	public void setRelativeZ(float z) {
		location[2] += z;
	}
	
	public float[] getLocation() {
		return location;
	}

	public int getRadius() {		
		return radius;
	}
}

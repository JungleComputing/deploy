package ibis.deploy.gui.performance.Vrarchy;

import ibis.deploy.gui.performance.PerfVis;

import javax.media.opengl.glu.GLU;

public class Vobject {		
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
	
	protected float[] location;
	protected float radius;
	
	protected int glName;
	protected float scaleXZ;
	protected float scaleY;	
	
	GLU glu;
	protected float separation;
		
	public Vobject(PerfVis perfvis) {
		glu = new GLU();
		this.perfvis = perfvis;
		
		this.location = new float[3];
		this.location[0] = 0.0f;
		this.location[1] = 0.0f;
		this.location[2] = 0.0f;
		
		this.separation = 0.0f;
		
		//set the size and radius to default
		setSize(1.0f, 0.25f);
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		
		//3d across
		this.radius = (float) Math.sqrt(  Math.pow(width, 2)
			 							+ Math.pow(height, 2)
			 							+ Math.pow(width, 2));
	}
	
	public void setGLName(int glName) {
		this.glName = glName;
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
	
	public void setSeparation(float newSeparation) {
		this.separation = newSeparation;
	}	
	
	public float[] getLocation() {
		return location;
	}

	public float getRadius() {		
		return radius;
	}
}

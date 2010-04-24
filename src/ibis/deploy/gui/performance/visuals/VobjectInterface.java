package ibis.deploy.gui.performance.visuals;

import javax.media.opengl.GL;

public interface VobjectInterface {
	//Specific to each Vobject type
	public void drawThis(GL gl, int glName);
	
	//Generic and handled in Vobject
	public void setSize(float width, float height);	
	public void setLocation(float[] newLocation);	
	public void setRelativeX(float x);
	public void setRelativeY(float y);
	public void setRelativeZ(float z);	
	
	public float[] getLocation();
	public float getRadius();
	public int getGLName();
}

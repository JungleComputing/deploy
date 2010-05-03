package ibis.deploy.gui.performance.visuals;

import javax.media.opengl.GL;

public interface VobjectInterface {
	//Specific to each Vobject type
	public void drawThis(GL gl, int glName);
	
	//Generic and handled in Vobject
	public void setSize(float width, float height);	
	public void setLocation(Float[] newLocation);
		
	public void setRelativeLocation(Float[] locationShift);	
	
	public Float[] getLocation();
	public float getRadius();
	public int getGLName();
}

package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.Vrarchy.Vobject;

import javax.media.opengl.GL;

public interface HobjectInterface {
	
	public void update() throws Exception;
	
	public void drawThis(GL gl, int glMode);
	
	public void setLocation(float[] locationXYZ);
	
	public void setSize(float width, float height);
	
	public Vobject getVisual();
}

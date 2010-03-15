package ibis.deploy.gui.performance.hierarchy;

import javax.media.opengl.GL;

public interface HobjectInterface {
	
	public void update() throws Exception;
	
	public void drawThis(GL gl, int glMode);
	
	public void setLocation(float[] locationXYZ);
	
	public void setSize(float width, float height);
}

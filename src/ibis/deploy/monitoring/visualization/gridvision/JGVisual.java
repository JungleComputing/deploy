package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

public interface JGVisual {
	public enum CollectionShape { CITYSCAPE, CUBE, SPHERE }
	public enum MetricShape { BAR, TUBE, ALPHATUBE, SPHERE, PARTICLES }
	public enum State { COLLAPSED, UNFOLDED, NOT_SHOWN }
	
	public void init(GL2 gl);
	
	public void setCoordinates(float[] newCoordinates);
	public void setRotation(float[] newRotation);
	
	public void setCollectionShape(CollectionShape newShape);
	public CollectionShape getCollectionShape();
		
	public void setState(State myState);
	public State getState();
	
	public JGVisual getParent();
	
	public void setMetricShape(MetricShape newShape);
	
	public float[] getCoordinates();
	
	public float getRadius();
	public float getWidth();
	public float getHeight();
	
	public void update();
	
	public void drawSolids(GL2 gl, int renderMode);
	public void drawTransparents(GL2 gl, int renderMode);
	public void drawSelectionCube(GL2 gl);

}

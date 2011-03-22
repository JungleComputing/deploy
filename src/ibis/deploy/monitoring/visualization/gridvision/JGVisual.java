package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

public interface JGVisual {
	public enum CollectionShape { CITYSCAPE, CUBE, SPHERE }
	public enum MetricShape { BAR, TUBE, SPHERE }
	public enum FoldState { COLLAPSED, UNFOLDED }
	
	public void init(GL2 gl);
	
	public void setCoordinates(float[] newCoordinates);
	public void setRotation(float[] newRotation);
	
	public void setCollectionShape(CollectionShape newShape);
	public CollectionShape getCollectionShape();
	public void setFoldState(FoldState myState);
	public FoldState getFoldState();
	public void setMetricShape(MetricShape newShape);
	
	public float[] getCoordinates();
	public float getRadius();
		
	public void update();
	
	public void drawSolids(GL2 gl, int renderMode);
	public void drawTransparents(GL2 gl, int renderMode);
	

}

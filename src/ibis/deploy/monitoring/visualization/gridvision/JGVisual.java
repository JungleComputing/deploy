package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

public interface JGVisual {
	enum CollectionShape { CITYSCAPE, CUBE, SPHERE } 
	enum MetricShape { BAR, TUBE, SPHERE }
	enum FoldState { COLLAPSED, UNFOLDED }
	
	public void init(GL2 gl);
	
	public void setCoordinates(Float[] newCoordinates);
	public void setRotation(Float[] newRotation);
	public void setDimensions(Float[] newDimensions);
	
	public void setCollectionShape(CollectionShape newShape);
	public void setFoldState(FoldState myState);
	public void setMetricShape(MetricShape newShape);
	
	public Float[] getCoordinates();	
	public Float[] getDimensions();
	public float getRadius();
		
	public void update();
	
	public void drawThis(GL2 gl, int renderMode);
	

}

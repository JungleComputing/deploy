package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

public abstract class JGVisualAbstract implements JGVisual {
	private final static float CUBE_RADIUS_MULTIPLIER = 0.075f;
	
	protected List<JGVisual> locations;
	protected List<JGVisual> ibises;
	protected List<JGVisual> metrics;
	protected List<JGVisual> links;
	
	protected Float[] coordinates;
	protected Float[] rotation;
	protected Float[] dimensions, maxChildDimensions;
	
	protected float maxAllChildDimensions;
	protected CollectionShape cShape;
	protected FoldState foldState;
	protected MetricShape mShape;
	protected float separation, ibisSeparation, metricSeparation;
	
	public JGVisualAbstract() {
		locations = new ArrayList<JGVisual>();
		ibises = new ArrayList<JGVisual>();
		metrics = new ArrayList<JGVisual>();
		links = new ArrayList<JGVisual>();
		
		coordinates   = new Float[3];
		//coordinates[0] = 0.0f;
		//coordinates[1] = 0.0f;
		//coordinates[2] = 0.0f;
		
		rotation   = new Float[3];		
		rotation[0] = 0.0f;
		rotation[1] = 0.0f;
		rotation[2] = 0.0f;
		
		dimensions = new Float[3];
		dimensions[0] = 0.0f;
		dimensions[1] = 0.0f;
		dimensions[2] = 0.0f;
		
		maxChildDimensions = new Float[3];
		maxAllChildDimensions = 0.0f;
		cShape = CollectionShape.CITYSCAPE;
		foldState = FoldState.UNFOLDED;
		mShape = MetricShape.BAR;
		separation = 0.0f;
		ibisSeparation = 0.5f;
		metricSeparation = 0.05f;
	}
	
	public void init(GL2 gl) {
		for (JGVisual child : locations) {
			child.init(gl);
		}
		for (JGVisual ibis : ibises) {
			ibis.init(gl);
		}	
		for (JGVisual metric : metrics) {
			metric.init(gl);
		}
		for (JGVisual link : links) {
			link.init(gl);
		}		
	}
	
	public void setCoordinates(Float[] newCoordinates) {
		coordinates[0] = newCoordinates[0];
		coordinates[1] = newCoordinates[1];
		coordinates[2] = newCoordinates[2];
		
		if (locations.size() > 0) {
			maxAllChildDimensions = Math.max(Math.max(maxChildDimensions[0], maxChildDimensions[1]), maxChildDimensions[2]);
			
			if (cShape == CollectionShape.CITYSCAPE) {
				//get the breakoff point for rows and columns
				int childCount = locations.size();
				int rows 		= (int)Math.ceil(Math.sqrt(childCount));
				int columns 	= (int)Math.floor(Math.sqrt(childCount));
				float xShiftPerChild = maxChildDimensions[0] + separation;
				float zShiftPerChild = maxChildDimensions[2] + separation;
				
				//Center the drawing around the location
				Float[] shiftedLocation = new Float[3];
				shiftedLocation[0] = coordinates[0] - ((xShiftPerChild*rows   )-separation) * 0.5f;
				shiftedLocation[1] = coordinates[1] - 5f;
				shiftedLocation[2] = coordinates[2] - ((zShiftPerChild*columns)-separation) * 0.5f;
				
				Float[] childLocation = new Float[3];
				
				int row = 0, column = 0, i = 0;
				for (JGVisual child : locations) {
					row = i % rows;
					
					//Move to next row (if applicable)
					if (i != 0 && row == 0) {
						column++;						
					}
									
					//cascade the new location
					childLocation[0] = shiftedLocation[0] + xShiftPerChild*row;
					childLocation[1] = shiftedLocation[1];
					childLocation[2] = shiftedLocation[2] + zShiftPerChild*column;
					
					child.setCoordinates(childLocation);
					    
					i++;
				}
			} else if (cShape == CollectionShape.SPHERE) {							
				double dlong = Math.PI*(3-Math.sqrt(5));
				double olong = 0.0;
				double dz    = 2.0/locations.size();
				double z     = 1 - (dz/2);
				Float[][] pt = new Float[locations.size()][3]; 
				double r = 0;
				float radius = CUBE_RADIUS_MULTIPLIER * (maxAllChildDimensions+separation) * locations.size();
				
				for (int k=0;k<locations.size();k++) {
					r = Math.sqrt(1-(z*z));
					pt[k][0] = coordinates[0] + radius*((float) (Math.cos(olong)*r));
					pt[k][1] = coordinates[1] + radius*((float) (Math.sin(olong)*r));
					pt[k][2] = coordinates[2] + radius*((float) z);
					z = z -dz;
					olong = olong +dlong;				
				}	
				
				int k=0;				
				for (JGVisual child : locations) {						
					//set the location						
					child.setCoordinates(pt[k]);							
					k++;
				}			
			} else if (cShape == CollectionShape.CUBE) {		
				//get the breakoff point for rows and columns
				int childCount = locations.size();
				int rows 		= (int)Math.ceil(Math.pow(childCount,  (1.0/3.0)));
				int columns 	= (int)Math.ceil(Math.pow(childCount, (1.0/3.0)));
				int layers		= (int)Math.floor(Math.pow(childCount, (1.0/3.0)));
							
				float xShiftPerChild = maxChildDimensions[0] + separation;
				float yShiftPerChild = maxChildDimensions[1] + separation;			
				float zShiftPerChild = maxChildDimensions[2] + separation;
				
				//Center the drawing around the location	
				Float[] shiftedLocation = new Float[3];
				shiftedLocation[0] = coordinates[0] - ((xShiftPerChild*rows   )-separation) * 0.5f;
				shiftedLocation[1] = coordinates[1] - ((yShiftPerChild*layers )-separation) * 0.5f;
				shiftedLocation[2] = coordinates[2] - ((zShiftPerChild*columns)-separation) * 0.5f;
				
				Float[] childLocation = new Float[3];
				
				int row = 0, column = 0, layer = 0;
				for (JGVisual child : locations) {								
					if (row == rows) {
						row = 0;
						column++;
					}
					if (column == columns) {
						column = 0;
						layer++;
					}			
									
					//cascade the new location
					childLocation[0] = shiftedLocation[0] + xShiftPerChild*row;
					childLocation[1] = shiftedLocation[1] + yShiftPerChild*layer;
					childLocation[2] = shiftedLocation[2] + zShiftPerChild*column;
					
					child.setCoordinates(childLocation);
					    
					row++;
				}
			} else {
				System.out.println("Collectionshape not defined while setting coordinates.");
				System.exit(0);
			}
		}
		
		if (ibises.size() > 0) {
			//get the breakoff point for rows and columns
			int number_of_children = ibises.size();
			int rows 		= (int)Math.ceil(Math.sqrt(number_of_children));
			int columns 	= (int)Math.floor(Math.sqrt(number_of_children));
			float xShiftPerChild = ibisSeparation;
			float zShiftPerChild = ibisSeparation;
			
			//Center the drawing around the location	
			Float[] shiftedLocation = new Float[3];
			shiftedLocation[0] = coordinates[0] - ((xShiftPerChild*rows   )-ibisSeparation) * 0.5f;
			shiftedLocation[1] = coordinates[1];
			shiftedLocation[2] = coordinates[2] - ((zShiftPerChild*columns)-ibisSeparation) * 0.5f;
			
			Float[] ibisLocation = new Float[3];
			
			int row = 0, column = 0, i = 0;
			for (JGVisual ibis : ibises) {
				row = i % rows;
				
				//Move to next row (if applicable)
				if (i != 0 && row == 0) {
					column++;						
				}
								
				//cascade the new location
				ibisLocation[0] = shiftedLocation[0] + xShiftPerChild*row;
				ibisLocation[1] = shiftedLocation[1];
				ibisLocation[2] = shiftedLocation[2] + zShiftPerChild*column;
				
				ibis.setCoordinates(ibisLocation);
				    
				i++;
			}
		}
		
		if (metrics.size() > 0) {
			//get the breakoff point for rows and columns
			int number_of_children = metrics.size();
			int rows 		= (int)Math.ceil(Math.sqrt(number_of_children));
			int columns 	= (int)Math.floor(Math.sqrt(number_of_children));
			float xShiftPerChild = metricSeparation;
			float zShiftPerChild = metricSeparation;
			
			//Center the drawing around the location	
			Float[] shiftedLocation = new Float[3];
			shiftedLocation[0] = coordinates[0] - ((xShiftPerChild*rows   )-metricSeparation) * 0.5f;
			shiftedLocation[1] = coordinates[1];
			shiftedLocation[2] = coordinates[2] - ((zShiftPerChild*columns)-metricSeparation) * 0.5f;
			
			Float[] metricLocation = new Float[3];
			
			int row = 0, column = 0, i = 0;
			for (JGVisual metric : metrics) {
				row = i % rows;
				
				//Move to next row (if applicable)
				if (i != 0 && row == 0) {
					column++;						
				}
								
				//cascade the new location
				metricLocation[0] = shiftedLocation[0] + xShiftPerChild*row;
				metricLocation[1] = shiftedLocation[1];
				metricLocation[2] = shiftedLocation[2] + zShiftPerChild*column;
				
				metric.setCoordinates(metricLocation);
				    
				i++;
			}
		}
		
	}
	
	public void setRotation(Float[] newRotation) {
		rotation[0] = newRotation[0];
		rotation[1] = newRotation[1];
		rotation[2] = newRotation[2];
	}
	
	public void setDimensions(Float[] newDimensions) {
		dimensions[0] = newDimensions[0];
		dimensions[1] = newDimensions[1];
		dimensions[2] = newDimensions[2];		
	}
	
	public void setCollectionShape(CollectionShape newShape) {		
		cShape = newShape;
		constructDimensions();
	}
	
	public void setFoldState(FoldState newFoldState) {
		foldState = newFoldState;		
	}
	
	public void setMetricShape(MetricShape newShape) {
		mShape = newShape;
		for (JGVisual metric : metrics) {
			metric.setMetricShape(newShape);
		}		
	}
	
	public Float[] getCoordinates() {
		Float[] myCoordinates = new Float[3];
		myCoordinates[0] = coordinates[0];
		myCoordinates[1] = coordinates[1];
		myCoordinates[2] = coordinates[2];
		
		return myCoordinates;
	}
	
	public Float[] getDimensions() {
		Float[] myDimensions = new Float[3];
		myDimensions[0] = dimensions[0];
		myDimensions[1] = dimensions[1];
		myDimensions[2] = dimensions[2];
		
		return myDimensions;
	}
	
	public float getRadius() {
		return maxAllChildDimensions;
	}
	
	public void update() {
		for (JGVisual child : locations) {
			child.update();
		}
		for (JGVisual ibis : ibises) {
			ibis.update();
		}
		for (JGVisual metric : metrics) {
			metric.update();
		}
		for (JGVisual link : links) {
			link.update();
		}
	}
	
	public void drawThis(GL2 gl, int renderMode) {
		if (foldState == FoldState.UNFOLDED) {
			for (JGVisual ibis : locations) {
				ibis.drawThis(gl, renderMode);
			}		
			for (JGVisual ibis : ibises) {
				ibis.drawThis(gl, renderMode);
			}
			for (JGVisual metric : metrics) {
				metric.drawThis(gl, renderMode);
			}
			for (JGVisual link : links) {
				link.drawThis(gl, renderMode);
			}			
		} else {
			
		}		
	}
	
	protected void constructDimensions() {
		Float[] childDimensions;
		maxChildDimensions[0] = 0.0f;
		maxChildDimensions[1] = 0.0f;
		maxChildDimensions[2] = 0.0f;
		
		for (JGVisual child : locations) {
			childDimensions = child.getDimensions();			
			if (childDimensions[0] > maxChildDimensions[0]) {
				maxChildDimensions[0] = childDimensions[0];
			}
			if (childDimensions[1] > maxChildDimensions[1]) {
				maxChildDimensions[1] = childDimensions[1];
			}
			if (childDimensions[2] > maxChildDimensions[2]) {
				maxChildDimensions[2] = childDimensions[2];
			}
		}
		
		maxAllChildDimensions = Math.max(Math.max(maxChildDimensions[0], maxChildDimensions[1]), maxChildDimensions[2]);
		
		if (cShape == CollectionShape.CITYSCAPE) {
			dimensions[0] = (maxChildDimensions[0]+separation) * (int) Math.ceil(Math.sqrt(locations.size()))-separation; 
			dimensions[1] = (maxChildDimensions[1]+separation);
			dimensions[2] = (maxChildDimensions[2]+separation) * (int) Math.floor(Math.sqrt(locations.size()))-separation;
		} else if (cShape == CollectionShape.SPHERE) {
			dimensions[0] = (maxAllChildDimensions+separation)*CUBE_RADIUS_MULTIPLIER * locations.size()*2f;
			dimensions[1] = (maxAllChildDimensions+separation)*CUBE_RADIUS_MULTIPLIER * locations.size()*2f;
			dimensions[2] = (maxAllChildDimensions+separation)*CUBE_RADIUS_MULTIPLIER * locations.size()*2f;
		} else if (cShape == CollectionShape.CUBE) {
			dimensions[0] = (maxChildDimensions[0]+separation) * (int) Math.ceil(Math.pow(locations.size(),(1.0/3.0)))-separation; 
			dimensions[1] = (maxChildDimensions[1]+separation) * (int) Math.floor(Math.pow(locations.size(),(1.0/3.0)))-separation;
			dimensions[2] = (maxChildDimensions[2]+separation) * (int) Math.ceil(Math.pow(locations.size(),(1.0/3.0)))-separation;
		}
	}
}

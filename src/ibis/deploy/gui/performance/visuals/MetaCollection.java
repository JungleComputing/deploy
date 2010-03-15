package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValuesMismatchException;

import java.nio.IntBuffer;

import javax.media.opengl.GL;

public class MetaCollection extends VisualizationElement{	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
	
	private Collection[] collections;
	private Float[][] values;
	
	private int clusterForm;
	private int elementForm;
	
	private float separation = 0.0f;
	
	public MetaCollection() {		
		super();
	}
	
	public MetaCollection(PerfVis perfvis, String[][] names, Float[][] colors) {
		super(perfvis, 0);
		
		collections = new Collection[names.length];	
		values = new Float[names.length][]; 
		
		for (int i = 0; i < names.length; i++) {
			collections[i] = new Collection(perfvis, names[i], colors);			
		}
	}

	public void setForm(int clusterForm, int elementForm) throws ModeUnknownException {
		if (clusterForm != MetaCollection.CITYSCAPE && clusterForm != MetaCollection.CIRCLE) {
			throw new ModeUnknownException();
		}
		this.clusterForm = clusterForm;
		this.elementForm = elementForm;
		
		//recalculate the outer radius for this form
		setSize(scaleXZ, scaleY);
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (int i = 0; i < collections.length; i++) {
			collections[i].setSize(width, height);
		}
		
		if (clusterForm == MetaCollection.CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(collections.length))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(collections.length))*(scaleXZ+0.1f));
			
			//Three-way pythagoras to determine the maximum distance across ;)
			radius = (int)Math.sqrt((int)(Math.sqrt(horz^2 + dept^2))^2 + vert^2);
			
		} else if (clusterForm == MetaCollection.CIRCLE) {
			double angle  = 2*Math.PI / values.length;
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			radius = (int)innerRadius+(int)scaleY;
		}
	}	
	
	public void setSeparation(float newSeparation) {
		this.separation = newSeparation;
	}
	
	public void setValues(Float[][] values) throws ValuesMismatchException {
		if (this.values.length != values.length) {
			throw new ValuesMismatchException();
		}
		this.values = values;
	}
	
	public void drawThis(GL gl, int glMode) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		

		//Move towards the intended location
		gl.glTranslatef(location[0], location[1], location[2]);
		
		//Draw the desired form
		if (clusterForm == MetaCollection.CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (clusterForm == MetaCollection.CIRCLE) {
			drawCircle(gl, glMode);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(collections.length));
		int columns 	= (int)Math.floor(Math.sqrt(collections.length));
		
		//Center the drawing around the location		
		//setRelativeX( ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f );
		//setRelativeY(-(0.5f*scaleY));
		//setRelativeZ(-((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f );
		
		int row = 0, column = 0;
		for (int i=0; i < collections.length; i++) {
			row = i % rows;
			//Move to next row (if applicable)
			if (i != 0 && row == 0) {
				column++;
			}
						
			//Setup the form
			try {
				collections[i].setForm(clusterForm, elementForm);
				collections[i].setValues(values[i]);
				collections[i].setLocation(location);
				collections[i].setSeparation(0.0f);
				collections[i].setRelativeX(-(scaleXZ+separation)*row);
				collections[i].setRelativeZ( (scaleXZ+separation)*column);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
			collections[i].setGLName(glName+i);
			collections[i].drawThis(gl, glMode);	
		}
	}
	
	protected void drawCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / collections.length;
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
						
		for (int i=0; i < collections.length; i++) {						
			//move towards the position
			collections[i].setLocation(location);
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
								
			//Setup the form
			try {
				collections[i].setForm(clusterForm, elementForm);
				collections[i].setValues(values[i]);				
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			//Draw the form
			collections[i].setGLName(glName+i);
			collections[i].drawThis(gl, glMode);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
	}
}

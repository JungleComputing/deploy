package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValuesMismatchException;

import java.nio.IntBuffer;

import javax.media.opengl.GL;

public class Collection extends VisualizationElement{	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
	
	private Element[] elements;
	private Float[] values;
	
	private int clusterForm;
	private int elementForm;
	
	private float separation = 0.0f;
	
	public Collection() {		
		super();
	}
	
	public Collection(PerfVis perfvis, String[] collection, Float[][] colors) {
		super(perfvis, 0);
		
		elements = new Element[collection.length];	
		values = new Float[collection.length]; 
		
		for (int i = 0; i < collection.length; i++) {
			elements[i] = new Element(perfvis, colors[i]); 
			values[i] = 0.0f;
		}
	}

	public void setForm(int clusterForm, int elementForm) throws ModeUnknownException {
		if (clusterForm != Collection.CITYSCAPE && clusterForm != Collection.CIRCLE) {
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
		for (int i = 0; i < elements.length; i++) {
			elements[i].setSize(width, height);
		}
		
		if (clusterForm == Collection.CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(elements.length))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(elements.length))*(scaleXZ+0.1f));
			
			//Three-way pythagoras to determine the maximum distance across ;)
			radius = (int)Math.sqrt((int)(Math.sqrt(horz^2 + dept^2))^2 + vert^2);
			
		} else if (clusterForm == Collection.CIRCLE) {
			double angle  = 2*Math.PI / values.length;
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			radius = (int)innerRadius+(int)scaleY;
		}
	}	
	
	public void setSeparation(float newSeparation) {
		this.separation = newSeparation;
	}
	
	public void setValues(Float[] values) throws ValuesMismatchException {
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
		if (clusterForm == Collection.CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (clusterForm == Collection.CIRCLE) {
			drawCircle(gl, glMode);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(elements.length));
		int columns 	= (int)Math.floor(Math.sqrt(elements.length));
		
		//Center the drawing around the location		
		//setRelativeX( ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f );
		//setRelativeY(-(0.5f*scaleY));
		//setRelativeZ(-((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f );
		
		int row = 0, column = 0;
		for (int i=0; i < elements.length; i++) {
			row = i % rows;
			//Move to next row (if applicable)
			if (i != 0 && row == 0) {
				column++;						
			}
						
			//Setup the form
			try {
				elements[i].setForm(elementForm);
				elements[i].setFilledArea(values[i]);
				elements[i].setLocation(location);
				elements[i].setRelativeX(-(scaleXZ+separation)*row);
				elements[i].setRelativeZ( (scaleXZ+separation)*column);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
			elements[i].setGLName(glName+i);
			elements[i].drawThis(gl, glMode);	
		}
	}
	
	protected void drawCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / elements.length;
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
						
		for (int i=0; i < elements.length; i++) {						
			//move towards the position
			elements[i].setLocation(location);
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
								
			//Setup the form
			try {
				elements[i].setForm(elementForm);
				elements[i].setFilledArea(values[i]);				
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			//Draw the form
			elements[i].setGLName(glName+i);
			elements[i].drawThis(gl, glMode);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
	}
}

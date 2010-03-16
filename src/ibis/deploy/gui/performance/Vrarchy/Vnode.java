package ibis.deploy.gui.performance.Vrarchy;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.exceptions.ValuesMismatchException;

import java.nio.IntBuffer;

import javax.media.opengl.GL;

public class Vnode extends Vobject implements VobjectInterface {	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
	
	private Vsinglestat[] elements;
	
	private int nodeForm;
	
	public Vnode(PerfVis perfvis, Float[][] colors) {
		super(perfvis);
		
		this.elements 	= new Vsinglestat[colors.length];
		
		for (int i = 0; i < colors.length; i++) {
			elements[i] = new Vsinglestat(perfvis, colors[i]); 
		}		
		
		try {
			setForm(Vnode.CITYSCAPE, Vsinglestat.BAR);			
		} catch (ModeUnknownException e) {			
			e.printStackTrace();
		}
	}

	public void setForm(int nodeForm, int statForm) throws ModeUnknownException {
		if (nodeForm != Vnode.CITYSCAPE && nodeForm != Vnode.CIRCLE) {
			throw new ModeUnknownException();
		}
		this.nodeForm = nodeForm;
		
		for (int i = 0; i < elements.length; i++) {
			elements[i].setForm(statForm);
		}
		
		//recalculate the outer radius for this form
		setSize(scaleXZ, scaleY);
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (int i = 0; i < elements.length; i++) {
			elements[i].setSize(width, height);
		}
		
		if (nodeForm == Vnode.CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(elements.length))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(elements.length))*(scaleXZ+0.1f));
			
			//3d across
			this.radius = (float) Math.sqrt(  Math.pow(horz, 2)
										 	+ Math.pow(vert, 2)
										 	+ Math.pow(dept, 2));
			
		} else if (nodeForm == Vnode.CIRCLE) {
			double angle  = 2*Math.PI / elements.length;
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			this.radius = (int)innerRadius+(int)scaleY;
		}
	}
	
	public void setValues(Float[] values) throws ValuesMismatchException, ValueOutOfBoundsException {
		if (this.elements.length != values.length) {
			throw new ValuesMismatchException();
		}
		for (int i = 0; i < elements.length; i++) {
			elements[i].setValue(values[i]);
		}
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
		if (nodeForm == Vnode.CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (nodeForm == Vnode.CIRCLE) {
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
		setRelativeX( ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f );
		//setRelativeY(-(0.5f*scaleY));
		setRelativeZ(-((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f );
		
		int row = 0, column = 0;
		for (int i=0; i < elements.length; i++) {
			row = i % rows;
			//Move to next row (if applicable)
			if (i != 0 && row == 0) {
				column++;						
			}
						
			//Setup the form
			try {
				elements[i].setLocation(location);
				elements[i].setRelativeX(-(scaleXZ+separation)*row);
				elements[i].setRelativeZ( (scaleXZ+separation)*column);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
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
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
								
			//Draw the form
			elements[i].drawThis(gl, glMode);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
	}
}

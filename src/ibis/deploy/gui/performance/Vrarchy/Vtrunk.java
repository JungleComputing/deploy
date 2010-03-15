package ibis.deploy.gui.performance.Vrarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.exceptions.ValuesMismatchException;

import java.nio.IntBuffer;

import javax.media.opengl.GL;

public class Vtrunk extends Vobject implements VobjectInterface {
	private static final int PARALLEL_SINGLE_ROW = 0; 
	
	private Vlink[] links;
	
	private int trunkForm;
	
	Vobject from;
	Vobject to;
	
	public Vtrunk(PerfVis perfvis, Float[][] colors, Vobject from, Vobject to) {
		super(perfvis);
		this.from = from;
		this.to = to;
		
		links = new Vlink[colors.length];
		
		for (int i = 0; i<colors.length; i++) {
			links[i] = new Vlink(perfvis, colors[i], from, to);
		}
		
		trunkForm = PARALLEL_SINGLE_ROW;		
	}
	
	public void setForm(int trunkForm, int linkForm) throws ModeUnknownException {
		for (int i = 0; i<links.length; i++) {
			links[i].setForm(linkForm);
		}
		this.trunkForm = trunkForm;
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (int i = 0; i < links.length; i++) {
			links[i].setSize(width, height);
		}
	}
	
	public void setValues(float[] values) throws ValuesMismatchException, ValueOutOfBoundsException {		
		if (values.length != links.length) {
			throw new ValuesMismatchException();
		}
		
		for (int i = 0; i<links.length; i++) {
			links[i].setValue(values[i]);
		}
	}
	
	public void setGLNames(int glNames[]) {		
		for (int i = 0; i < links.length; i++) {
			links[i].setGLName(glNames[i]);
		}
	}
	
	public void drawThis(GL gl, int glMode) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		
		
		//Calculate the angles we need to turn towards the destination
		float[] origin = from.getLocation();
		float[] destination = to.getLocation();
		
		float xDist = origin[0] - destination[0];
		float yDist = origin[1] - destination[1];
		float zDist = origin[2] - destination[2];
		
		float zAngle = (float) Math.atan(yDist/xDist);
		float yAngle = (float) Math.atan(zDist/xDist);
		
		//Calculate the length of this element : V( x^2 + y^2 + z^2 ) 
		float length  = (float) Math.sqrt(	Math.pow(xDist,2)
										  + Math.pow(yDist,2) 
										  + Math.pow(zDist,2));
		
		length = length - (from.getRadius() + to.getRadius());
		
		//Translate to the origin and turn towards the destination
		gl.glTranslatef(origin[0], origin[1], origin[2]);
		gl.glRotatef(zAngle, 0.0f, 0.0f, 1.0f);
		gl.glRotatef(yAngle, 0.0f, 1.0f, 0.0f);
		
		if (trunkForm == PARALLEL_SINGLE_ROW) {
			drawParallel_single_row(gl, glMode);
		}
				
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	private void drawParallel_single_row(GL gl, int glMode) {
		gl.glTranslatef(-(0.5f*(scaleXZ+separation)*links.length), 0.0f, 0.0f);
		for (int i=0; i < links.length; i++) {			
			links[i].setLocation(location);
			links[i].setRelativeX((scaleXZ+separation)*i);				
						
			//Draw the form
			links[i].drawThis(gl, glMode);
		}
	}
}
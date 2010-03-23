package ibis.deploy.gui.performance.newtry.Vrarchy;
import java.nio.IntBuffer;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLUquadric;

public class Vsinglestat extends Vobject implements VobjectInterface {
	public static final int TUBE = 1;
	public static final int BAR = 2;
	public static final int SPHERE = 3;	
	
	private static final float ALPHA = 0.2f;
	
	private Float[] color;	
	private float alpha;
	
	private float value;
	private int currentForm;
		
	public Vsinglestat(PerfVis perfvis, Float[] color) {
		super(perfvis);
		this.color = color;
		this.alpha = ALPHA;
		
		this.value = 1.0f;
		this.currentForm = BAR;
	}
	
	public void setValue(float value) throws ValueOutOfBoundsException {
		if (value >= 0.0f && value <= 1.0f) {
			this.value = value;
		} else {			
			throw new ValueOutOfBoundsException();			
		}
	}
	
	public void setForm(int form) throws ModeUnknownException {
		if (form == BAR || form == TUBE || form == SPHERE) {
			this.currentForm = form;
		} else {			
			throw new ModeUnknownException();
		}
	}
	
	public void drawThis(GL gl, int glMode) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		//Move towards the intended location
		if (glMode == GL.GL_SELECT) gl.glLoadName(glName);
		gl.glTranslatef(location[0], location[1], location[2]);
		
		//Draw the form
		if (currentForm == BAR) {
			drawBar(gl);
		} else if (currentForm == TUBE) {
			drawTube(gl);
		} else if (currentForm == SPHERE) {
			drawSphere(gl);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawBar(GL gl) {		
		//use nice variables, so that the ogl code is readable
		float o = 0.0f;			//(o)rigin
		float x = scaleXZ;		//(x) maximum coordinate
		float y = scaleY;		//(y) maximum coordinate
		float z = scaleXZ;		//(z) maximum coordinate	
		float f = value * y; 	//(f)illed area
		float r = y - f;		//(r)est area (non-filled, up until the maximum) 
		
		//Transparency
		float lineAlpha = alpha;
		
		//Color for the lines around the box
		float line_color_r = 0.8f;
		float line_color_g = 0.8f;
		float line_color_b = 0.8f;
		
		if (perfvis.getSelection() == glName) {
			line_color_r = 1.0f;
			line_color_g = 1.0f;
			line_color_b = 1.0f;
			lineAlpha = 1.0f;
		}
					
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
						
		//Center the drawing startpoint
		gl.glTranslatef(-0.5f*x, 0.0f, -0.5f*z);		
		
		//The solid Element
			gl.glBegin(GL.GL_LINE_LOOP);
				//TOP of filled area
				gl.glColor3f(line_color_r,line_color_g,line_color_b);			
				gl.glVertex3f( x, f, o);			
				gl.glVertex3f( o, f, o);			
				gl.glVertex3f( o, f, z);			
				gl.glVertex3f( x, f, z);			
			gl.glEnd();		
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//BOTTOM
				gl.glColor3f(line_color_r,line_color_g,line_color_b);			
				gl.glVertex3f( x, o, z);			
				gl.glVertex3f( o, o, z);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( x, o, o);			
			gl.glEnd();	
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//FRONT
				gl.glColor3f(line_color_r,line_color_g,line_color_b);			
				gl.glVertex3f( x, f, z);			
				gl.glVertex3f( o, f, z);			
				gl.glVertex3f( o, o, z);			
				gl.glVertex3f( x, o, z);			
			gl.glEnd();
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//BACK
				gl.glColor3f(line_color_r,line_color_g,line_color_b);			
				gl.glVertex3f( x, o, o);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( o, f, o);			
				gl.glVertex3f( x, f, o);			
			gl.glEnd();	
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//LEFT
				gl.glColor3f(line_color_r,line_color_g,line_color_b);			
				gl.glVertex3f( o, f, z);			
				gl.glVertex3f( o, f, o);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( o, o, z);			
			gl.glEnd();
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//RIGHT
				gl.glColor3f(line_color_r,line_color_g,line_color_b);			
				gl.glVertex3f( x, f, o);			
				gl.glVertex3f( x, f, z);			
				gl.glVertex3f( x, o, z);			
				gl.glVertex3f( x, o, o);
			gl.glEnd();
				
			gl.glBegin(GL.GL_QUADS);		
				//TOP
				gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);			
				gl.glVertex3f( x, f, o);			
				gl.glVertex3f( o, f, o);			
				gl.glVertex3f( o, f, z);			
				gl.glVertex3f( x, f, z);
				
				//BOTTOM
				gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);			
				gl.glVertex3f( x, o, z);			
				gl.glVertex3f( o, o, z);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( x, o, o);
				
				//FRONT
				gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);			
				gl.glVertex3f( x, f, z);			
				gl.glVertex3f( o, f, z);			
				gl.glVertex3f( o, o, z);			
				gl.glVertex3f( x, o, z);
				
				//BACK
				gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);			
				gl.glVertex3f( x, o, o);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( o, f, o);			
				gl.glVertex3f( x, f, o);
				
				//LEFT
				gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);			
				gl.glVertex3f( o, f, z);			
				gl.glVertex3f( o, f, o);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( o, o, z);
				
				//RIGHT
				gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);			
				gl.glVertex3f( x, f, o);			
				gl.glVertex3f( x, f, z);			
				gl.glVertex3f( x, o, z);			
				gl.glVertex3f( x, o, o);
			gl.glEnd();	
		
		//The shadow (nonfilled) element		
		gl.glTranslatef(0.0f, f, 0.0f);
	
		gl.glBegin(GL.GL_LINE_LOOP);
			//TOP of shadow area
			gl.glColor4f(line_color_r,line_color_g,line_color_b, lineAlpha);			
			gl.glVertex3f( x, r, o);			
			gl.glVertex3f( o, r, o);			
			gl.glVertex3f( o, r, z);			
			gl.glVertex3f( x, r, z);			
		gl.glEnd();		
		
		//Bottom left out, since it's the top of the solid area
		
		gl.glBegin(GL.GL_LINE_LOOP);
			//FRONT
			gl.glColor4f(line_color_r,line_color_g,line_color_b, lineAlpha);			
			gl.glVertex3f( x, r, z);			
			gl.glVertex3f( o, r, z);			
			gl.glVertex3f( o, o, z);			
			gl.glVertex3f( x, o, z);			
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_LOOP);
			//BACK
			gl.glColor4f(line_color_r,line_color_g,line_color_b, lineAlpha);			
			gl.glVertex3f( x, o, o);			
			gl.glVertex3f( o, o, o);			
			gl.glVertex3f( o, r, o);			
			gl.glVertex3f( x, r, o);			
		gl.glEnd();	
		
		gl.glBegin(GL.GL_LINE_LOOP);
			//LEFT
			gl.glColor4f(line_color_r,line_color_g,line_color_b, lineAlpha);			
			gl.glVertex3f( o, r, z);			
			gl.glVertex3f( o, r, o);			
			gl.glVertex3f( o, o, o);			
			gl.glVertex3f( o, o, z);			
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_LOOP);
			//RIGHT
			gl.glColor4f(line_color_r,line_color_g,line_color_b, lineAlpha);			
			gl.glVertex3f( x, r, o);			
			gl.glVertex3f( x, r, z);			
			gl.glVertex3f( x, o, z);			
			gl.glVertex3f( x, o, o);
		gl.glEnd();
			
		gl.glBegin(GL.GL_QUADS);		
			//TOP
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, r, o);			
			gl.glVertex3f( o, r, o);			
			gl.glVertex3f( o, r, z);			
			gl.glVertex3f( x, r, z);
			
			//BOTTOM left out
			
			//FRONT
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, r, z);			
			gl.glVertex3f( o, r, z);			
			gl.glVertex3f( o, o, z);			
			gl.glVertex3f( x, o, z);
			
			//BACK
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, o, o);			
			gl.glVertex3f( o, o, o);			
			gl.glVertex3f( o, r, o);			
			gl.glVertex3f( x, r, o);
			
			//LEFT
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( o, r, z);			
			gl.glVertex3f( o, r, o);			
			gl.glVertex3f( o, o, o);			
			gl.glVertex3f( o, o, z);
			
			//RIGHT
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, r, o);			
			gl.glVertex3f( x, r, z);			
			gl.glVertex3f( x, o, z);			
			gl.glVertex3f( x, o, o);
		gl.glEnd();		
	}
	
	protected void drawTube(GL gl) {		
		float line_color_r = 0.8f;
		float line_color_g = 0.8f;
		float line_color_b = 0.8f;
		
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
		
		float radius = scaleXZ /2;
		
		float f = value * scaleY;
		
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
		
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		
		//The Solid Element
			//Bottom disk
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluDisk(qobj, 0.0, radius, 32, 1);
						
			//Sides
			glu.gluCylinder(qobj, radius, radius, f, 32, 10);			
			
			//Edge of bottom disk
			gl.glColor3f(line_color_r, line_color_g, line_color_b);
			glu.gluCylinder(qobj, radius, radius, 0.01f, 32, 10);
			
			gl.glTranslatef(0.0f, 0.0f, f);
			
			//Top disk
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluDisk(qobj, 0.0, radius, 32, 1);
			
			//Edge of top disk
			gl.glColor3f(line_color_r, line_color_g, line_color_b);
			glu.gluCylinder(qobj, radius, radius, 0.01f, 32, 10);
		
		//The shadow Element				
		//Bottom disk left out, since it's the top disk of the solid
								
		//Sides
		gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
		glu.gluCylinder(qobj, radius, radius, scaleY-f, 32, 10);			
		
		//Edge of bottom disk also left out
					
		gl.glTranslatef(0.0f, 0.0f, scaleY-f);
		
		//Top disk
		gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
		glu.gluDisk(qobj, 0.0, radius, 32, 1);
		
		//Edge of top disk
		gl.glColor4f(line_color_r, line_color_g, line_color_b, alpha);
		glu.gluCylinder(qobj, radius, radius, 0.01f, 32, 10);	
		
		
		//Cleanup
		glu.gluDeleteQuadric(qobj);
	}	
	
	protected void drawSphere(GL gl) {
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
		
		float radius = scaleXZ /2;
		
		float f = value * radius;
		
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
		
		//The Solid Element
			//Sphere
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluSphere(qobj, f, 15, 10);			
		
		//The shadow Element
		
		//Sphere
		gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
		glu.gluSphere(qobj, radius, 15, 10);			
		
		//Cleanup
		glu.gluDeleteQuadric(qobj);
	}	
}

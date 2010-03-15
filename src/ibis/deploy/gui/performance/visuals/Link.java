package ibis.deploy.gui.performance.visuals;

import ibis.deploy.gui.performance.PerfVis;

import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLUquadric;

public class Link extends VisualizationElement {
	public static int TUBES = 1;
	public static int BARS = 2;
	public static int SPHERES = 3;
	
	private Float[] color = VisualizationElement.NETWORK_LINK_COLOR;	
	private float alpha = 0.2f;
		
	private float filledArea = 0.0f;
	private int currentForm = BARS;
	
	VisualizationElement from;
	VisualizationElement to;
	
	public Link() {
		super();
	}
	
	public Link(PerfVis perfvis, int glName, VisualizationElement from, VisualizationElement to) {
		super(perfvis, glName);
		this.from = from;
		this.to = to;
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
		
		//Calculate the length of the element
		float length  = (float)Math.sqrt((int)(Math.sqrt((int)xDist^2 + (int)yDist^2))^2 + (int)zDist^2);
		length = length - (from.getRadius() + to.getRadius());
		
		//Translate to the origin and turn towards the destination
		gl.glTranslatef(origin[0], origin[1], origin[2]);
		gl.glRotatef(zAngle, 0.0f, 0.0f, 1.0f);
		gl.glRotatef(yAngle, 0.0f, 1.0f, 0.0f);
		
		//And draw the link
		if (glMode == GL.GL_SELECT) gl.glLoadName(glName);
		if (currentForm == BARS) {
			drawBar(gl, length);
		} else if (currentForm == TUBES) {
			drawTube(gl, length);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawBar(GL gl, float lengthTotal) {		
		float o = 0.0f;
		float x = scaleXZ;
		float y = lengthTotal;
		float z = scaleXZ;
		
		float line_color_r = 0.8f;
		float line_color_g = 0.8f;
		float line_color_b = 0.8f;
		
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
				
		float f = filledArea * y;
		float r = y - f;		 
				
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
		
		//The shadow element
		
			gl.glTranslatef(0.0f, f, 0.0f);
		
			gl.glBegin(GL.GL_LINE_LOOP);
				//TOP of filled area
				gl.glColor4f(line_color_r,line_color_g,line_color_b, alpha);			
				gl.glVertex3f( x, r, o);			
				gl.glVertex3f( o, r, o);			
				gl.glVertex3f( o, r, z);			
				gl.glVertex3f( x, r, z);			
			gl.glEnd();		
			
			//Bottom left out, since it's the top of the solid area
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//FRONT
				gl.glColor4f(line_color_r,line_color_g,line_color_b, alpha);			
				gl.glVertex3f( x, r, z);			
				gl.glVertex3f( o, r, z);			
				gl.glVertex3f( o, o, z);			
				gl.glVertex3f( x, o, z);			
			gl.glEnd();
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//BACK
				gl.glColor4f(line_color_r,line_color_g,line_color_b, alpha);			
				gl.glVertex3f( x, o, o);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( o, r, o);			
				gl.glVertex3f( x, r, o);			
			gl.glEnd();	
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//LEFT
				gl.glColor4f(line_color_r,line_color_g,line_color_b, alpha);			
				gl.glVertex3f( o, r, z);			
				gl.glVertex3f( o, r, o);			
				gl.glVertex3f( o, o, o);			
				gl.glVertex3f( o, o, z);			
			gl.glEnd();
			
			gl.glBegin(GL.GL_LINE_LOOP);
				//RIGHT
				gl.glColor4f(line_color_r,line_color_g,line_color_b, alpha);			
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
	
	protected void drawTube(GL gl, float lengthTotal ) {		
		float line_color_r = 0.8f;
		float line_color_g = 0.8f;
		float line_color_b = 0.8f;
		
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
		
		float radius = scaleXZ /2;
		
		float f = filledArea * lengthTotal;
		
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
			glu.gluCylinder(qobj, radius, radius, lengthTotal-f, 32, 10);			
			
			//Edge of bottom disk also left out
						
			gl.glTranslatef(0.0f, 0.0f, lengthTotal-f);
			
			//Top disk
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
			glu.gluDisk(qobj, 0.0, radius, 32, 1);
			
			//Edge of top disk
			gl.glColor4f(line_color_r, line_color_g, line_color_b, alpha);
			glu.gluCylinder(qobj, radius, radius, 0.01f, 32, 10);	
		
		
		//Cleanup
		glu.gluDeleteQuadric(qobj);
	}
}

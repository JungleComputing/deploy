package ibis.deploy.gui.performance.visuals;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.nio.IntBuffer;
import java.util.Map;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.swing.SetCollectionFormAction;
import ibis.deploy.gui.performance.swing.SetMetricFormAction;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLUquadric;

public class Vmetric extends Vobject implements VobjectInterface {	
	private static final float ALPHA = 0.2f;
	
	private Float[] color;	
	private float alpha;
	
	private float value;
	private Vobject from, to;
		
	public Vmetric(PerfVis perfvis, VisualManager visman, Vobject parent, Float[] color) {
		super(perfvis, visman);
		this.parent = parent;
		
		this.color = color;
		this.alpha = ALPHA;
		
		this.value = 1.0f;
		this.currentMetricForm = Vobject.METRICS_BAR;
		
		this.from = null;
		this.to = null;
		
		//Register the new object with the Performance visualization object
		this.glName = visman.registerMetric(this);		
	}
	
	public Vmetric(PerfVis perfvis, VisualManager visman, Vobject parent, Float[] color, Vobject from, Vobject to) {
		super(perfvis, visman);
		this.parent = parent;
		
		this.color = color;
		this.alpha = ALPHA;
		
		this.value = 1.0f;
		this.currentMetricForm = Vobject.METRICS_TUBE;
		
		this.from = from;
		this.to = to;
		
		//Register the new object with the Performance visualization object
		this.glName = visman.registerMetric(this);	
	}
	
	public void setValue(float value) throws ValueOutOfBoundsException {
		if (value >= 0.0f && value <= 1.0f) {
			this.value = value;
		} else {			
			throw new ValueOutOfBoundsException();			
		}
	}
	
	/*
	public void setForm(int form) throws ModeUnknownException {
		if (form != Vobject.METRICS_BAR || form != Vobject.METRICS_TUBE || form != Vobject.METRICS_SPHERE) {
			throw new ModeUnknownException();			
		} else {			
			this.currentMetricForm = form;
		}
	}
	*/
	
	public void drawThis(GL gl, int glMode) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		if (from == null && to == null) {
			//Move towards the intended location
			if (glMode == GL.GL_SELECT) gl.glLoadName(glName);
			gl.glTranslatef(location[0], location[1], location[2]);
			
			//Draw the form
			if (currentMetricForm == Vobject.METRICS_BAR) {
				drawBar(gl, scaleY);
			} else if (currentMetricForm == Vobject.METRICS_TUBE) {
				drawTube(gl, scaleY);
			} else if (currentMetricForm == Vobject.METRICS_SPHERE) {
				drawSphere(gl);
			}		
		} else {
			//Calculate the angles we need to turn towards the destination
			Float[] origin = from.getLocation();
			Float[] destination = to.getLocation();
			
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
			
			//And draw the link
			if (glMode == GL.GL_SELECT) gl.glLoadName(glName);
			if (currentMetricForm == Vobject.METRICS_BAR) {
				drawBar(gl, length);
			} else if (currentMetricForm == Vobject.METRICS_TUBE) {
				drawTube(gl, length);
			}
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawBar(GL gl, float length) {		
		//use nice variables, so that the ogl code is readable
		float o = 0.0f;			//(o)rigin
		float x = scaleXZ;		//(x) maximum coordinate
		float y = length;		//(y) maximum coordinate
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
	
	protected void drawTube(GL gl, float length) {		
		float line_color_r = 0.8f;
		float line_color_g = 0.8f;
		float line_color_b = 0.8f;
		
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
		
		float radius = scaleXZ /2;
		
		float f = value * length;
		
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
		glu.gluCylinder(qobj, radius, radius, length-f, 32, 10);			
		
		//Edge of bottom disk also left out
					
		gl.glTranslatef(0.0f, 0.0f, length-f);
		
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
	
	public PopupMenu getMenu() {	
		return parent.getMenu();
		/*
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Circle"};
		
		PopupMenu newMenu = new PopupMenu();	
		
		Menu metricsForms 	= makeRadioGroup("Metric Form", elementsgroup);
		Menu nodeForms 		= makeRadioGroup("Node Group Form", collectionsgroup);
		Menu nodeMetricForms= makeRadioGroup("Node Metric Form", elementsgroup);
		Menu siteForms 		= makeRadioGroup("Site Group Form", collectionsgroup);
		Menu siteMetricForms= makeRadioGroup("Site Metric Form", elementsgroup);
		Menu poolForms 		= makeRadioGroup("Pool Group Form", collectionsgroup);
		Menu poolMetricForms= makeRadioGroup("Pool Metric Form", elementsgroup);
		
		newMenu.add(metricsForms);
		newMenu.add(nodeForms);
		newMenu.add(nodeMetricForms);
		newMenu.add(siteForms);
		newMenu.add(siteMetricForms);
		newMenu.add(poolForms);
		newMenu.add(poolMetricForms);
		
		Menu metricsOptions = new Menu("Metrics Toggle");
		metricsOptions.add(parent.getMetricsMenu("Node"));
		metricsOptions.add(parent.getParent().getMetricsMenu("Site"));
		metricsOptions.add(parent.getParent().getParent().getMetricsMenu("Pool"));
		newMenu.add(metricsOptions);

		newMenu.add(parent.getAveragesMenu("Compound Node"));
		newMenu.add(parent.getParent().getAveragesMenu("Compound Site"));
		newMenu.add(parent.getParent().getParent().getAveragesMenu("Compound Pool"));
		
		return newMenu;
		*/		
	}	
	
	protected Menu makeRadioGroup(String menuName, String[] itemNames) {
		Menu result = new Menu(menuName);
		
		for (String item : itemNames) {
			MenuItem newMenuItem = new MenuItem(item);
			if (menuName.equals("Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this, item));
			} else if (menuName.equals("Node Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent(), item));
			} else if (menuName.equals("Node Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this.getParent(), item));
			} else if (menuName.equals("Site Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent().getParent(), item));
			} else if (menuName.equals("Site Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this.getParent().getParent(), item));
			} else if (menuName.equals("Pool Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent().getParent().getParent(), item));
			} else if (menuName.equals("Pool Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this.getParent().getParent().getParent(), item));
			}
			result.add(newMenuItem);			
		}
				
		return result;
	}
}

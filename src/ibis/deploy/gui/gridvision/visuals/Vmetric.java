package ibis.deploy.gui.gridvision.visuals;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import ibis.deploy.gui.gridvision.GridVision;
import ibis.deploy.gui.gridvision.VisualManager;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.gridvision.swing.SetCollectionFormAction;
import ibis.deploy.gui.gridvision.swing.SetMetricFormAction;
import ibis.deploy.gui.gridvision.swing.ToggleAveragesAction;
import ibis.deploy.gui.gridvision.swing.ToggleMetricAction;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

public class Vmetric implements VisualElementInterface {	
	GridVision perfvis;
	VisualManager visman;
	
	protected Float[] location;
	protected float radius;
	protected int currentMetricForm;
	protected int currentCollectionForm;
	protected boolean showAverages;
	
	protected int glName;
	protected float scaleXZ;
	protected float scaleY;	
	
	GLU glu;
	protected float separation;
	
	protected HashMap<String, Vmetric> vmetrics;
	protected Set<String> shownMetrics;
	protected VisualElementInterface parent;
	
	private static final float ALPHA = 0.2f;
	
	private Float[] color;	
	private float alpha;
	
	private float value;
		
	public Vmetric(GridVision perfvis, VisualManager visman, VisualElementInterface parent, Float[] color) {
		this.perfvis = perfvis;
		this.visman = visman;
		
		glu = new GLU();
		this.showAverages = false;
		shownMetrics = new HashSet<String>();
		
		this.location = new Float[3];
		this.location[0] = 0.0f;
		this.location[1] = 0.0f;
		this.location[2] = 0.0f;
		
		this.separation = 0.0f;
		
		scaleXZ = 0.25f;
		scaleY = 1.0f;
				
		this.vmetrics 	= new HashMap<String, Vmetric>();
		
		this.parent = parent;
		
		this.color = color;
		this.alpha = ALPHA;
		
		this.value = 1.0f;
		this.currentMetricForm = VisualElementInterface.METRICS_BAR;
		
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
		if (currentMetricForm == VisualElementInterface.METRICS_BAR) {
			drawBar(gl, scaleY);
		} else if (currentMetricForm == VisualElementInterface.METRICS_TUBE) {				
			drawTube(gl, scaleY);
		} else if (currentMetricForm == VisualElementInterface.METRICS_SPHERE) {
			drawSphere(gl);
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
		
		//Rotate to align with the y axis instead of the default z axis
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
				
		//The Solid Element
			//Bottom disk
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluDisk(qobj, 0.0, radius, 8, 1);
						
			//Sides
			glu.gluCylinder(qobj, radius, radius, f, 8, 1);			
			
			//Edge of bottom disk
			gl.glColor3f(line_color_r, line_color_g, line_color_b);
			glu.gluCylinder(qobj, radius, radius, 0.01f, 8, 1);
			
			gl.glTranslatef(0.0f, 0.0f, f);
			
			//Top disk
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluDisk(qobj, 0.0, radius, 8, 1);
			
			//Edge of top disk
			gl.glColor3f(line_color_r, line_color_g, line_color_b);
			glu.gluCylinder(qobj, radius, radius, 0.01f, 8, 1);
		
		//The shadow Element				
		//Bottom disk left out, since it's the top disk of the solid
								
		//Sides
		gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
		glu.gluCylinder(qobj, radius, radius, length-f, 8, 1);			
		
		//Edge of bottom disk also left out
					
		gl.glTranslatef(0.0f, 0.0f, length-f);
		
		//Top disk
		gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
		glu.gluDisk(qobj, 0.0, radius, 8, 1);
		
		//Edge of top disk
		gl.glColor4f(line_color_r, line_color_g, line_color_b, alpha);
		glu.gluCylinder(qobj, radius, radius, 0.01f, 8, 1);	
		
		
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
	
	protected void drawCubes(GL gl, float length) {
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
		
	}
	
	public PopupMenu getMenu() {	
		return parent.getMenu();
	}	
	
	public Menu getSubMenu() {
		return null;
	}
	
	public Menu makeRadioGroup(String menuName, String[] itemNames) {
		Menu result = new Menu(menuName);
		
		for (String item : itemNames) {
			MenuItem newMenuItem = new MenuItem(item);
			if (menuName.equals("Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this, item));
			} else if (menuName.equals("Node Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(parent, item));
			} else if (menuName.equals("Node Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(parent, item));
			} else if (menuName.equals("Site Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(parent.getParent(), item));
			} else if (menuName.equals("Site Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(parent.getParent(), item));
			} else if (menuName.equals("Pool Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(parent.getParent().getParent(), item));
			} else if (menuName.equals("Pool Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(parent.getParent().getParent(), item));
			}
			result.add(newMenuItem);			
		}
				
		return result;
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;		
	}
	
	public void setLocation(Float[] newLocation) {
		this.location[0] = newLocation[0];
		this.location[1] = newLocation[1];
		this.location[2] = newLocation[2];
	}
	
	public void setSeparation(float newSeparation) {
		separation = newSeparation;		
	}
	
	public void setRadius() {
		radius = Math.max(vmetrics.size()*(scaleXZ), scaleY);
	}
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == VisualElementInterface.METRICS_BAR || newForm == VisualElementInterface.METRICS_TUBE || newForm == VisualElementInterface.METRICS_SPHERE) {
			currentMetricForm = newForm;
		} else if (newForm == VisualElementInterface.COLLECTION_CITYSCAPE || newForm == VisualElementInterface.COLLECTION_CIRCLE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
	}	
		
	public Float[] getLocation() {
		return location;
	}	

	public float getRadius() {		
		return radius;
	}
	
	public int getGLName() {
		return glName;
	}
	
	public VisualElementInterface getParent() {		
		return parent;
	}
	
	public Menu getMetricsMenu(String label) {
		Menu result = new Menu(label);
		
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			MenuItem newMenuItem = new MenuItem(entry.getKey());
			newMenuItem.addActionListener(new ToggleMetricAction(this, entry.getKey()));
			result.add(newMenuItem);
		}
		
		return result;
	}
	
	public Menu getAveragesMenu(String label) {
		Menu result = new Menu(label);
		MenuItem newMenuItem;
		
		if (!showAverages) {
			newMenuItem = new MenuItem("Show Averages");
		} else {
			newMenuItem = new MenuItem("Show Sublevel");
		}
		
		newMenuItem.addActionListener(new ToggleAveragesAction(this, newMenuItem.getLabel()));
		result.add(newMenuItem);
		
		return result; 
	}
	
	
	
	public void toggleMetricShown(String key) throws StatNotRequestedException {
		if (vmetrics.containsKey(key)) {
			if (!shownMetrics.contains(key)) {			
				shownMetrics.add(key);
			} else {			
				shownMetrics.remove(key);
			}
		} else {
			throw new StatNotRequestedException();
		}
	}
	
	public void toggleAverages() {
		this.showAverages = !showAverages;
	}
}

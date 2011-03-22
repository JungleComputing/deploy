package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;

public class JGMetric extends JGVisualAbstract implements JGVisual {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.gui.junglevision.visuals.Metric");
	
	private enum MetricDisplay { TRANSPARANCY, SOLIDS_ONLY }
	
	private static final float WIDTH = 0.25f;
	private static final float HEIGHT = 1.00f;
	
	private GLUgl2 glu;
	
	private Float[] color;
	
	private Metric metric;
	private float currentValue;
	private MetricOutput currentOutputMethod = MetricOutput.PERCENT;
	private MetricModifier myMod;
	
	private int glName;
	private int[] barPointer, transparencyPointer, selectedTransparencyPointer, selectedBarPointer;
	private MetricDisplay currentDisplay;
	
	JGMetric(JungleGoggles goggles, GLUgl2 glu, JGVisual parent, Metric metric, MetricModifier mod) {		
		super(goggles);
		
		this.goggles = goggles;	
		this.glu = glu;
		this.metric = metric;
		this.myMod = mod;
		this.color = metric.getDescription().getColor();
		
		update();
		
		transparencyPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.BAR_TRANSPARENCY);
		barPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.BAR);
		selectedTransparencyPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.SELECTED_BAR_TRANSPARENCY);
		selectedBarPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.SELECTED_BAR);
		
		currentDisplay = MetricDisplay.TRANSPARANCY;
		
		radius = WIDTH;
		
		glName = goggles.registerGLName(parent, this);
	}
	
	public void drawSolids(GL2 gl, int renderMode) {
		if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }
		if (mShape == MetricShape.BAR) {
			drawSolidBar(gl, currentValue);
		} else if (mShape == MetricShape.TUBE) {
			drawSolidTube(gl, currentValue);
		}		
	}
	
	public void drawTransparents(GL2 gl, int renderMode) {
		if (currentDisplay == MetricDisplay.TRANSPARANCY) {
			if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }
			if (mShape == MetricShape.BAR) {
				drawTransparentBar(gl, currentValue);
			} else if (mShape == MetricShape.TUBE) {
				drawTransparentTube(gl, currentValue);
			}		
		}
	}
	
	public void update() {
		try {			
			currentValue = (Float) metric.getValue(myMod, currentOutputMethod);
		} catch (OutputUnavailableException e) {
			logger.debug("OutputUnavailableException caught by visual metric for "+metric.getDescription().getName());
		}
	}

	protected void drawSolidBar(GL2 gl, float length) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);		
				
		int whichBar = (int) Math.floor(length*barPointer.length);
		if (length >= 0.95f) {
			whichBar = (barPointer.length)-1;
		}

		gl.glColor4f(color[0], color[1], color[2], 1.0f);
		
		if (goggles.currentlySelected(glName)) {
			gl.glCallList(selectedBarPointer[whichBar]);
		} else {
			gl.glCallList(barPointer[whichBar]);
		}
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	protected void drawTransparentBar(GL2 gl, float length) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);		
				
		int whichBar = (int) Math.floor(length*barPointer.length);
		if (length >= 0.95f) {
			whichBar = (barPointer.length)-1;
		}
		
		gl.glColor4f(color[0], color[1], color[2], 0.4f);
		if (goggles.currentlySelected(glName)) {
			gl.glCallList(selectedTransparencyPointer[whichBar]);
		} else {
			gl.glCallList(transparencyPointer[whichBar]);
		}	
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	protected void drawSolidTube(GL2 gl, float length) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);		
				
		final int SIDES = 12;
		final float EDGE_SIZE = 0.01f;
		 			
		float 	Yn = -0.5f*HEIGHT;
				//Yp =  0.5f*HEIGHT;

		float Yf = 0.0f;
					
		Yf = (length*HEIGHT)-(0.5f*HEIGHT);
		
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
		
		float radius = WIDTH / 2;
							
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
				
		//The Solid Element
			gl.glTranslatef(0.0f, Yn, 0.0f);
			
			//Bottom disk
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
						
			//Sides
			glu.gluCylinder(qobj, radius, radius, Yf, SIDES, 1);			
			
			//Edge of bottom disk
			gl.glColor3f(0.8f,0.8f,0.8f);
			glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
			
			gl.glTranslatef(0.0f, Yf, 0.0f);
			
			//Top disk
			gl.glColor3f(quad_color_r, quad_color_g, quad_color_b);
			glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
			
			//Edge of top disk
			gl.glColor3f(0.8f,0.8f,0.8f);
			glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);		
		
		//Cleanup
		glu.gluDeleteQuadric(qobj);
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	protected void drawTransparentTube(GL2 gl, float length) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);		
				
		final int SIDES = 12;
		final float EDGE_SIZE = 0.01f;			
		
		float alpha = 0.4f;
		 			
		float 	Yn = -0.5f*HEIGHT,
				Yp =  0.5f*HEIGHT;

		float Yf = 0.0f;
					
		Yf = (length*HEIGHT)-(0.5f*HEIGHT);
		
		float quad_color_r = color[0];
		float quad_color_g = color[1];
		float quad_color_b = color[2];
		
		float radius = WIDTH / 2;
							
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
				
		//The Solid Element
			gl.glTranslatef(0.0f, Yn, 0.0f);			
			gl.glTranslatef(0.0f, Yf, 0.0f);
		
		//The shadow Element				
			//Bottom disk left out, since it's the top disk of the solid
										
			//Sides
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
			glu.gluCylinder(qobj, radius, radius, Yp-Yf, SIDES, 1);			
			
			//Edge of bottom disk also left out
						
			gl.glTranslatef(0.0f, Yp-Yf, 0.0f);
			
			//Top disk
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);
			glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
			
			//Edge of top disk
			gl.glColor4f(0.8f,0.8f,0.8f, alpha);
			glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);		
		
		//Cleanup
		glu.gluDeleteQuadric(qobj);
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
}

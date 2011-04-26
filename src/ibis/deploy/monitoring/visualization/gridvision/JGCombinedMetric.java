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

public class JGCombinedMetric extends JGVisualAbstract implements JGVisual {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.visualization.gridvision.JGCombinedMetric");
	private final float ALPHA = 0.1f;
	private final float LINE_ALPHA = 0.4f;
	private final float LINE_WIDTH = 0.8f;
	private final int SIDES = 12;
	private final float EDGE_SIZE = 0.01f;
	
	private enum MetricDisplay { TRANSPARANCY_ENABLED, SOLIDS_ONLY };
	
	private Float[][] colors;
	
	private Metric[] metrics;
	private float maxValue, usedValue;
	private float[] solidValues, transparentValues;
	
	private GLUgl2 glu = new GLUgl2();
	private int glName;
	private int solidListPointer, transparentListPointer;
	private boolean solidListBuilt, transparentListBuilt;
	
	private MetricDisplay currentDisplay;
	
	JGCombinedMetric(JungleGoggles goggles, JGVisual parent, Metric[] metrics) {		
		super(goggles, parent);
		
		this.goggles = goggles;	
		this.metrics = metrics;
		
		this.colors = new Float[metrics.length][3];
		
		for (int i = 0; i < colors.length; i++) {
			this.colors[i] = metrics[i].getDescription().getColor();
		}
		
		this.solidValues 		= new float[metrics.length];
		this.transparentValues 	= new float[metrics.length];
		
		solidListPointer = -1;
		solidListBuilt = false;
			
		transparentListPointer = -1;
		transparentListBuilt = false;
		
		update();
		
		currentDisplay = MetricDisplay.TRANSPARANCY_ENABLED;
		mShape = MetricShape.BAR;
		
		width = 0.25f;
		height = 1f;
		
		radius = height;		
		
		glName = goggles.registerGLName(parent, this);
	}
	
	public void update() {
		try {
			maxValue 	= (Float) metrics[0].getValue(MetricModifier.MAX, MetricOutput.RPOS);
			usedValue 	= (Float) metrics[0].getValue(MetricModifier.NORM, MetricOutput.RPOS);
			
			float usedValueTotal = 0f;
			for (int i = 1; i < metrics.length; i++) {
				solidValues[i] = (Float) metrics[i].getValue(MetricModifier.NORM, MetricOutput.RPOS)/maxValue;
				
				usedValueTotal += solidValues[i];
			}
			solidValues[0] = usedValue/maxValue - usedValueTotal;
			usedValueTotal += solidValues[0];
			
			for (int i = 1; i < metrics.length; i++) {
				transparentValues[i] = (Float) metrics[i].getValue(MetricModifier.MAX, MetricOutput.RPOS);
				transparentValues[i] -= solidValues[i];
				transparentValues[i] = transparentValues[i]/maxValue;
				
				usedValueTotal += transparentValues[i];
			}
			transparentValues[0] = height - usedValueTotal;
			
			solidListBuilt = false;
			transparentListBuilt = false;
		} catch (OutputUnavailableException e) {
			logger.debug("OutputUnavailableException caught");
		}
	}
	
	public void drawSolids(GL2 gl, int renderMode) {
		if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }	
		
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);	
		
		if (solidListBuilt) {
			gl.glCallList(solidListPointer);
		} else {				
			solidListPointer = gl.glGenLists(1);
			
			gl.glNewList(solidListPointer, GL2.GL_COMPILE_AND_EXECUTE);					
				float bottom = -0.5f*height;
				
				float drawnPercentage = 0f;
				
				if (mShape == MetricShape.TUBE) {
					gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
				}
				
				for (int i = 0; i < metrics.length; i++) {
					float percentage = solidValues[i];
					
					gl.glColor4f(colors[i][0], colors[i][1], colors[i][2], 1f);
					if (mShape == MetricShape.BAR) {
						drawBarSolid(gl, bottom+drawnPercentage, percentage);
					} else if (mShape == MetricShape.TUBE) {
						drawTubeSolid(gl, bottom+drawnPercentage, percentage);
					}
					drawnPercentage += percentage;
				}
			gl.glEndList();
			
			solidListBuilt = true;
		}			
		
		//Restore the old modelview matrix
		gl.glPopMatrix();			
				
	}
	
	public void drawTransparents(GL2 gl, int renderMode) {
		if (currentDisplay == MetricDisplay.TRANSPARANCY_ENABLED) {
			if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }
			
			//Save the current modelview matrix
			gl.glPushMatrix();
			
			//Translate to the desired coordinates and rotate if desired
			gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
			gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
			gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
			gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);	
		
			if (transparentListBuilt) {
				gl.glCallList(transparentListPointer);
			} else {
				transparentListPointer = gl.glGenLists(1);
				
				gl.glNewList(transparentListPointer, GL2.GL_COMPILE_AND_EXECUTE);
					float bottom = -0.5f*height;
				
					float drawnPercentage = 0f;
					
					if (mShape == MetricShape.TUBE) {
						gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
					}
					
					for (int i = 0; i < metrics.length; i++) {
						float percentage = solidValues[i];							
						drawnPercentage += percentage;
					}
					for (int i = metrics.length-1; i >= 0 ; i--) {
						float percentage = transparentValues[i];
						gl.glColor4f(colors[i][0], colors[i][1], colors[i][2], ALPHA);
						if (mShape == MetricShape.BAR) {
							drawBarTransparent(gl, bottom+drawnPercentage, percentage);
						} else if (mShape == MetricShape.TUBE) {
							drawTubeTransparency(gl, bottom+drawnPercentage, percentage);
						}
						drawnPercentage += percentage;
					}
				gl.glEndList();
				
				transparentListBuilt = true;
			}			
			
			//Restore the old modelview matrix
			gl.glPopMatrix();
		}
	}
	
	private void drawBarSolid(GL2 gl, float bottom, float fill) {
		gl.glLineWidth(LINE_WIDTH);
		
		float 	Xn = -0.5f*width,
				Xp =  0.5f*width,
				Yn =  bottom,
				Zn = -0.5f*width,
				Zp =  0.5f*width;
		
		float Yf = bottom+fill;
			
		//The solid area				
		gl.glBegin(GL2.GL_QUADS);					
			//TOP
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zn);
			
			//BOTTOM
			gl.glVertex3f( Xn, Yn, Zn);
			gl.glVertex3f( Xp, Yn, Zn);
			gl.glVertex3f( Xp, Yn, Zp);
			gl.glVertex3f( Xn, Yn, Zp);
			
			//FRONT
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xn, Yn, Zp);
			gl.glVertex3f( Xp, Yn, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			
			//BACK
			gl.glVertex3f( Xp, Yf, Zn);
			gl.glVertex3f( Xp, Yn, Zn);
			gl.glVertex3f( Xn, Yn, Zn);
			gl.glVertex3f( Xn, Yf, Zn);
			
			//LEFT
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yn, Zn);
			gl.glVertex3f( Xn, Yn, Zp);
			gl.glVertex3f( Xn, Yf, Zp);
			
			//RIGHT
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yn, Zp);
			gl.glVertex3f( Xp, Yn, Zn);
			gl.glVertex3f( Xp, Yf, Zn);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			//TOP
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zn);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			//BOTTOM
			gl.glVertex3f( Xn, Yn, Zn);
			gl.glVertex3f( Xp, Yn, Zn);
			gl.glVertex3f( Xp, Yn, Zp);
			gl.glVertex3f( Xn, Yn, Zp);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			//FRONT
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xn, Yn, Zp);
			gl.glVertex3f( Xp, Yn, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			//BACK
			gl.glVertex3f( Xp, Yf, Zn);
			gl.glVertex3f( Xp, Yn, Zn);
			gl.glVertex3f( Xn, Yn, Zn);
			gl.glVertex3f( Xn, Yf, Zn);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			//LEFT
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yn, Zn);
			gl.glVertex3f( Xn, Yn, Zp);
			gl.glVertex3f( Xn, Yf, Zp);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			//RIGHT
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yn, Zp);
			gl.glVertex3f( Xp, Yn, Zn);
			gl.glVertex3f( Xp, Yf, Zn);
		gl.glEnd();
	}
	
	private void drawBarTransparent(GL2 gl, float bottom, float fill) {
		gl.glLineWidth(LINE_WIDTH);
		
		float 	Xn = -0.5f*width,
				Xp =  0.5f*width,
				Yp =  bottom+fill,
				Zn = -0.5f*width,
				Zp =  0.5f*width;
		
		float Yf = bottom;
						
		//The transparent area
		gl.glBegin(GL2.GL_QUADS);					
			//TOP
			gl.glVertex3f( Xn, Yp, Zn);
			gl.glVertex3f( Xn, Yp, Zp);
			gl.glVertex3f( Xp, Yp, Zp);
			gl.glVertex3f( Xp, Yp, Zn);
			
			//BOTTOM LEFT OUT
			//gl.glVertex3f( Xn, Yn, Zn);
			//gl.glVertex3f( Xp, Yn, Zn);
			//gl.glVertex3f( Xp, Yn, Zp);
			//gl.glVertex3f( Xn, Yn, Zp);
			
			//FRONT
			gl.glVertex3f( Xn, Yp, Zp);
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yp, Zp);
			
			//BACK
			gl.glVertex3f( Xp, Yp, Zn);
			gl.glVertex3f( Xp, Yf, Zn);
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yp, Zn);
			
			//LEFT
			gl.glVertex3f( Xn, Yp, Zn);
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xn, Yp, Zp);
			
			//RIGHT
			gl.glVertex3f( Xp, Yp, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zn);
			gl.glVertex3f( Xp, Yp, Zn);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4f(0.8f,0.8f,0.8f, LINE_ALPHA);
			
			//TOP
			gl.glVertex3f( Xn, Yp, Zn);
			gl.glVertex3f( Xn, Yp, Zp);
			gl.glVertex3f( Xp, Yp, Zp);
			gl.glVertex3f( Xp, Yp, Zn);
		gl.glEnd();
		
		//gl.glBegin(GL2.GL_LINE_LOOP);
			//gl.glColor3f(0.8f,0.8f,0.8f);
			//BOTTOM LEFT OUT
			//gl.glVertex3f( Xn, Yn, Zn);
			//gl.glVertex3f( Xp, Yn, Zn);
			//gl.glVertex3f( Xp, Yn, Zp);
			//gl.glVertex3f( Xn, Yn, Zp);
		//gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4f(0.8f,0.8f,0.8f, LINE_ALPHA);
				
			//FRONT
			gl.glVertex3f( Xn, Yp, Zp);
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yp, Zp);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4f(0.8f,0.8f,0.8f, LINE_ALPHA);
			
			//BACK
			gl.glVertex3f( Xp, Yp, Zn);
			gl.glVertex3f( Xp, Yf, Zn);
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yp, Zn);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4f(0.8f,0.8f,0.8f, LINE_ALPHA);
			
			//LEFT
			gl.glVertex3f( Xn, Yp, Zn);
			gl.glVertex3f( Xn, Yf, Zn);
			gl.glVertex3f( Xn, Yf, Zp);
			gl.glVertex3f( Xn, Yp, Zp);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor4f(0.8f,0.8f,0.8f, LINE_ALPHA);
			
			//RIGHT
			gl.glVertex3f( Xp, Yp, Zp);
			gl.glVertex3f( Xp, Yf, Zp);
			gl.glVertex3f( Xp, Yf, Zn);
			gl.glVertex3f( Xp, Yp, Zn);
		gl.glEnd();
	}
	
	protected void drawTubeSolid(GL2 gl, float bottom, float fill) {		
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		float radius = width/2;
				
		//Translate 'down' to center
		gl.glTranslatef(0f, 0f, bottom);
					
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
				
		//The Solid Element, draw dynamically colored elements first				
			//Bottom disk
			glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
			
			//Sides
			glu.gluCylinder(qobj, radius, radius, fill, SIDES, 1);	
			
			//Translate 'up'
			gl.glTranslatef(0f, 0f, fill);
			
			//Top disk
			glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
			
		//Now, draw the fixed color elements.			
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);	
			
			//Translate 'down'
			gl.glTranslatef(0f, 0f, -fill);
			
			//Edge of bottom disk
			gl.glColor3f(0.8f,0.8f,0.8f);
			glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
							
		//Cleanup
		glu.gluDeleteQuadric(qobj);		
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	protected void drawTubeTransparency(GL2 gl, float bottom, float fill) {	
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		float radius = width/2;
				
		//Make a new quadratic object
		GLUquadric qobj = glu.gluNewQuadric();
				
		//Move away from the Solid Element
		gl.glTranslatef(0f, 0f, bottom);
		
		//The shadow Element, draw dynamically colored elements first
			//Bottom disk left out, since it's the top disk of the solid
										
			//Sides
			glu.gluCylinder(qobj, radius, radius, fill, SIDES, 1);
						
			gl.glTranslatef(0f, 0f, fill);
			
			//Top disk
			glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
			
			//Edge of top disk
			gl.glColor4f(0.8f,0.8f,0.8f, LINE_ALPHA);
			
			glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);		
		
		//Cleanup
		glu.gluDeleteQuadric(qobj);
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
}

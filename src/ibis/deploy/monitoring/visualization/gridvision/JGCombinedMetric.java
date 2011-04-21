package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

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
	
	private enum MetricDisplay { TRANSPARANCY_ENABLED, SOLIDS_ONLY };
	
	private Float[][] colors;
	
	private Metric[] metrics;
	private float maxValue, usedValue;
	private float[] solidValues, transparentValues;
	
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
		
		if (mShape == MetricShape.BAR) {
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
					for (int i = 0; i < metrics.length; i++) {
						float percentage = solidValues[i];
						
						gl.glColor4f(colors[i][0], colors[i][1], colors[i][2], 1f);
						drawBarSolid(gl, bottom+drawnPercentage, percentage);
						
						drawnPercentage += percentage;
					}
				gl.glEndList();
				
				solidListBuilt = true;
			}			
			
			//Restore the old modelview matrix
			gl.glPopMatrix();			
		}		
	}
	
	public void drawTransparents(GL2 gl, int renderMode) {
		if (currentDisplay == MetricDisplay.TRANSPARANCY_ENABLED) {
			if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }
			if (mShape == MetricShape.BAR) {
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
						for (int i = 0; i < metrics.length; i++) {
							float percentage = solidValues[i];							
							drawnPercentage += percentage;
						}
						for (int i = metrics.length-1; i >= 0 ; i--) {
							float percentage = transparentValues[i];
							gl.glColor4f(colors[i][0], colors[i][1], colors[i][2], ALPHA);
							drawBarTransparent(gl, bottom+drawnPercentage, percentage);
							
							drawnPercentage += percentage;
						}
					gl.glEndList();
					
					transparentListBuilt = true;
				}			
				
				//Restore the old modelview matrix
				gl.glPopMatrix();
			}
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
}

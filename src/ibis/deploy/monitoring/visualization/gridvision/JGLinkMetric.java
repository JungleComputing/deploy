package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;

public class JGLinkMetric extends JGVisualAbstract implements JGVisual {
	private static final float WIDTH = 0.1f;
	private static final float HEIGHT = 1.00f;
	private static final float ALPHA = 0.4f;
	private static final int ACCURACY = 20;
	
	private GLUgl2 glu;
	
	private Metric metric;
	private Float[] color;
	private float currentValue;
	private MetricOutput currentOutputMethod = MetricOutput.PERCENT;
	private int glName;
		
	//On-demand generated displaylists
	private int[] onDemandList;
	private boolean[] onDemandListsBuilt;
	private boolean listsInitialized;
	private int whichList;
	
	private float[] dimensions = {WIDTH,HEIGHT,WIDTH};
	
	JGLinkMetric(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, Metric metric) {
		super(goggles, parent);
		
		this.glu = glu;
		this.metric = metric;
		this.color = metric.getDescription().getColor();
		
		try {
			currentValue = (Float) metric.getValue(MetricModifier.NORM, currentOutputMethod);
		} catch (OutputUnavailableException e) {
			//This shouldn't happen if the metric is defined properly
			e.printStackTrace();
		}
		
		onDemandList 		= new int[ACCURACY+1];
		onDemandListsBuilt 	= new boolean[ACCURACY+1];		
		listsInitialized = false;
		whichList = 0;		
		
		glName = goggles.registerGLName(parent, this);
	}
	
	public void init(GL2 gl) {
		if (listsInitialized) {
			gl.glDeleteLists(onDemandList[0], ACCURACY+1);
		}
		onDemandList[0] = gl.glGenLists(ACCURACY+1);
		
		for (int i=0; i<ACCURACY+1; i++) {
			onDemandListsBuilt[i] = false;
			onDemandList[i] = onDemandList[0]+i;
		}
		listsInitialized = true;
	}
	
	public void setCoordinates(float[] newCoords) {	
		coordinates[0] = newCoords[0];
		coordinates[1] = newCoords[1];
		coordinates[2] = newCoords[2];
		
		for (int i=0; i<ACCURACY+1; i++) {
			onDemandListsBuilt[i] = false;
		}
	}
	
	public void setDimensions(float[] newDims) {
		for (int i=0;i<3;i++) {
			dimensions[i] = newDims[i];
		}
	}
	
	public float[] getDimensions() {
		float[] result = new float[3];
		for (int i=0; i<3; i++) {
			result[i] = dimensions[i];
		}
		return result;
	}
	
	public void drawSolids(GL2 gl, int renderMode) {
		if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }		
		
		whichList = (int)(ACCURACY*currentValue);
		
		if (mShape == MetricShape.BAR) {
			drawBar(gl, currentValue, dimensions[1]);
		} else if (mShape == MetricShape.TUBE) {
			drawTube(gl, currentValue, dimensions[1]);
		}		
	}
	
	public void update() {				
		try {
			currentValue = (Float) metric.getValue(MetricModifier.NORM, currentOutputMethod);
		} catch (OutputUnavailableException e) {
			//This shouldn't happen if the metric is defined properly
			e.printStackTrace();
		}	
	}

	protected void drawBar(GL2 gl, float length, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		if (onDemandListsBuilt[whichList]) {
			gl.glCallList(onDemandList[whichList]);
		} else {
			onDemandListsBuilt[whichList] = true;
			
			float alpha = ALPHA;
			
			float 	Xn = -0.5f*dimensions[0],
					Xp =  0.5f*dimensions[0],
					Yn = -0.5f*maxLength,
					Yp =  0.5f*maxLength,
					Zn = -0.5f*dimensions[2],
					Zp =  0.5f*dimensions[2];
	
			float Yf = 0.0f;
						
			Yf = (length*maxLength)-(0.5f*maxLength);
			
			gl.glNewList(onDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);
				
				//The solid area
				gl.glBegin(GL2.GL_QUADS);	
					gl.glColor3f(color[0],color[1],color[2]);
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
				
				//The transparent area			
				gl.glBegin(GL2.GL_QUADS);
				gl.glColor4f(color[0],color[1],color[2], alpha);
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
					gl.glColor3f(0.8f,0.8f,0.8f);
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
					gl.glColor3f(0.8f,0.8f,0.8f);
					//FRONT
					gl.glVertex3f( Xn, Yp, Zp);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yp, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(0.8f,0.8f,0.8f);
					//BACK
					gl.glVertex3f( Xp, Yp, Zn);
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yp, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(0.8f,0.8f,0.8f);
					//LEFT
					gl.glVertex3f( Xn, Yp, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xn, Yp, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(0.8f,0.8f,0.8f);
					//RIGHT
					gl.glVertex3f( Xp, Yp, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xp, Yp, Zn);
				gl.glEnd();
				
			gl.glEndList();
		}		
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	protected void drawTube(GL2 gl, float length, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
		
		if (onDemandListsBuilt[whichList]) {
			gl.glCallList(onDemandList[whichList]);
		} else {		
			final int SIDES = 12;
			final float EDGE_SIZE = 0.01f;			
			
			float alpha = ALPHA;
			 			
			float 	Yn = -0.5f*maxLength,
					Yp =  0.5f*maxLength;
	
			float Yf = (length*maxLength)-(0.5f*maxLength);
			
			float radius = Math.max(dimensions[0], dimensions[2]) / 2;
			
			//On-demand generated list			
			onDemandListsBuilt[whichList] = true;
			gl.glNewList(onDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);				
				//Make a new quadratic object
				GLUquadric qobj = glu.gluNewQuadric();
						
				//The Solid Element
					gl.glTranslatef(0.0f, Yn, 0.0f);
					
					//Bottom disk
					gl.glColor3f(color[0], color[1], color[2]);
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
								
					//Sides
					glu.gluCylinder(qobj, radius, radius, Yf, SIDES, 1);			
					
					//Edge of bottom disk
					gl.glColor3f(0.8f,0.8f,0.8f);
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
					
					gl.glTranslatef(0.0f, Yf, 0.0f);
					
					//Top disk
					gl.glColor3f(color[0], color[1], color[2]);
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Edge of top disk
					gl.glColor3f(0.8f,0.8f,0.8f);
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
				
				//The shadow Element				
					//Bottom disk left out, since it's the top disk of the solid
												
					//Sides
					gl.glColor4f(color[0], color[1], color[2], alpha);
					glu.gluCylinder(qobj, radius, radius, Yp-Yf, SIDES, 1);			
					
					//Edge of bottom disk also left out
								
					gl.glTranslatef(0.0f, Yp-Yf, 0.0f);
					
					//Top disk
					gl.glColor4f(color[0], color[1], color[2], alpha);
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Edge of top disk
					gl.glColor4f(0.8f,0.8f,0.8f, alpha);
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);		
				
				//Cleanup
				glu.gluDeleteQuadric(qobj);
			gl.glEndList();
		}
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
}

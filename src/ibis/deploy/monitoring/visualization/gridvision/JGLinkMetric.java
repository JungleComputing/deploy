package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.AllInUseException;

public class JGLinkMetric extends JGVisualAbstract implements JGVisual {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.visualization.gridvision.JGLinkMetric");
	
	private static final float WIDTH = 0.1f;
	private static final float HEIGHT = 1.00f;
	private static final float ALPHA = 0.4f;
	private static final int ACCURACY = 20;
	
	private static boolean DONT_SHOW_EMPTY = true;
	
	private GLUgl2 glu;
	
	private Metric metric;
	private Float[] color;
	private float currentValue;
	private MetricOutput currentOutputMethod = MetricOutput.PERCENT;
	private int glName;
		
	//On-demand generated displaylists
	private int[] transparentsOnDemandList, solidsOnDemandList;
	private boolean[] transparentsOnDemandListsBuilt, solidsOnDemandListsBuilt;
	private boolean listsInitialized;
	private int whichList;
	
	private float[] dimensions = {WIDTH,HEIGHT,WIDTH};
	
	ArrayList<Particle> localParticleStore;
	private boolean reversed;
	
	JGLinkMetric(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, Metric metric, boolean reversed) {
		super(goggles, parent);
		
		this.reversed = reversed;
		
		this.glu = glu;
		this.metric = metric;
		this.color = metric.getDescription().getColor();
		
		try {
			currentValue = (Float) metric.getValue(MetricModifier.NORM, currentOutputMethod);
		} catch (OutputUnavailableException e) {
			//This shouldn't happen if the metric is defined properly
			e.printStackTrace();
		}
		
		mShape = MetricShape.PARTICLES;
		width = Math.max(dimensions[0], dimensions[2]);
		height = dimensions[1];		
		
		solidsOnDemandList 				= new int[ACCURACY+1];
		transparentsOnDemandList 		= new int[ACCURACY+1];
		solidsOnDemandListsBuilt 		= new boolean[ACCURACY+1];
		transparentsOnDemandListsBuilt 	= new boolean[ACCURACY+1];		
		
		listsInitialized = false;
		whichList = 0;		
		
		glName = goggles.registerGLName(parent, this);
		
		localParticleStore = new ArrayList<Particle>();
	}
	
	public void init(GL2 gl) {
		if (mShape != MetricShape.PARTICLES) {
			if (listsInitialized) {
				gl.glDeleteLists(solidsOnDemandList[0], ACCURACY+1);
				gl.glDeleteLists(transparentsOnDemandList[0], ACCURACY+1);
			}
			solidsOnDemandList[0] = gl.glGenLists(ACCURACY+1);
			transparentsOnDemandList[0] = gl.glGenLists(ACCURACY+1);
			
			for (int i=0; i<ACCURACY+1; i++) {
				solidsOnDemandListsBuilt[i] = false;
				transparentsOnDemandListsBuilt[i] = false;
				
				solidsOnDemandList[i] = solidsOnDemandList[0]+i;
				transparentsOnDemandList[i] = transparentsOnDemandList[0]+i;
			}
			listsInitialized = true;
		}
	}
	
	public void setCoordinates(float[] newCoords) {	
		coordinates[0] = newCoords[0];
		coordinates[1] = newCoords[1];
		coordinates[2] = newCoords[2];
		
		for (int i=0; i<ACCURACY+1; i++) {
			solidsOnDemandListsBuilt[i] = false;
			transparentsOnDemandListsBuilt[i] = false;
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
		
		//Save the current modelview matrix
		gl.glPushMatrix();
		
			//Do not draw links that have no value
			if (DONT_SHOW_EMPTY && whichList != 0) {			
				if (mShape == MetricShape.BAR) {
					drawSolidBar(gl, currentValue, dimensions[1]);
				} else if (mShape == MetricShape.TUBE) {
					drawSolidTube(gl, currentValue, dimensions[1]);
				} else if (mShape == MetricShape.ALPHATUBE) {
				}
			}
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
		
	public void drawTransparents(GL2 gl, int renderMode) {
		if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }		
		
		whichList = (int)(ACCURACY*currentValue);
		
		//Do not draw links that have no value
		if (DONT_SHOW_EMPTY && whichList != 0) {	
			if (mShape == MetricShape.BAR) {
				drawTransparentBar(gl, currentValue, dimensions[1]);
			} else if (mShape == MetricShape.TUBE) {
				drawTransparentTube(gl, currentValue, dimensions[1]);
			} else if (mShape == MetricShape.ALPHATUBE) {
				drawAlphaTube(gl, currentValue, dimensions[1]);
			}
		}
		
		if (mShape == MetricShape.PARTICLES) {		
			if (whichList != 0) {
				try {
					Particle p = goggles.getParticle();
					localParticleStore.add(p);
					startParticle(p, dimensions[1], currentValue);
				} catch (AllInUseException e) {
					//Whatever, ignore.
				}
			}
			drawParticles(gl);
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
		
	private void drawSolidBar(GL2 gl, float length, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		if (solidsOnDemandListsBuilt[whichList]) {
			gl.glCallList(solidsOnDemandList[whichList]);
		} else {
			solidsOnDemandListsBuilt[whichList] = true;
			
			float 	Xn = -0.5f*dimensions[0],
					Xp =  0.5f*dimensions[0],
					Yn = -0.5f*maxLength,
					Zn = -0.5f*dimensions[2],
					Zp =  0.5f*dimensions[2];
	
			float Yf = 0.0f;
						
			Yf = (length*maxLength)-(0.5f*maxLength);
			
			gl.glNewList(solidsOnDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);
				
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
				
			gl.glEndList();
		}		
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}

	private void drawTransparentBar(GL2 gl, float length, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		if (transparentsOnDemandListsBuilt[whichList]) {
			gl.glCallList(transparentsOnDemandList[whichList]);
		} else {
			transparentsOnDemandListsBuilt[whichList] = true;
			
			float alpha = ALPHA;
			
			float 	Xn = -0.5f*dimensions[0],
					Xp =  0.5f*dimensions[0],
					Yp =  0.5f*maxLength,
					Zn = -0.5f*dimensions[2],
					Zp =  0.5f*dimensions[2];
	
			float Yf = 0.0f;
						
			Yf = (length*maxLength)-(0.5f*maxLength);
			
			gl.glNewList(transparentsOnDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);				
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
	
	private void drawSolidTube(GL2 gl, float length, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();	
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
		
		if (solidsOnDemandListsBuilt[whichList]) {
			gl.glCallList(solidsOnDemandList[whichList]);
		} else {		
			final int SIDES = 12;
			final float EDGE_SIZE = 0.01f;
			 			
			float Yn = -0.5f*maxLength;	
			float Yf = length*maxLength;
			
			float radius = Math.max(dimensions[0], dimensions[2]) / 2;
			
			//On-demand generated list			
			solidsOnDemandListsBuilt[whichList] = true;
			gl.glNewList(solidsOnDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);
				
				//Translate 'down' to center this object
				gl.glTranslatef(0f, 0f, Yn);
							
				//Make a new quadratic object
				GLUquadric qobj = glu.gluNewQuadric();
						
				//The Solid Element, draw dynamically colored elements first
					gl.glColor3f(color[0], color[1], color[2]);
					//Bottom disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Sides
					glu.gluCylinder(qobj, radius, radius, Yf, SIDES, 1);	
					
					//Translate 'up'
					gl.glTranslatef(0f, 0f, Yf);
					
					//Top disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
				//Now, draw the fixed color elements.	
					gl.glColor3f(0.8f,0.8f,0.8f);
					
					//Edge of top disk
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);	
					
					//Translate 'down'
					gl.glTranslatef(0f, 0f, -Yf);
					
					//Edge of bottom disk
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
									
				//Cleanup
				glu.gluDeleteQuadric(qobj);
			
			gl.glEndList();
		}
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
		
		
	private void drawTransparentTube(GL2 gl, float length, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();	
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
		
		if (transparentsOnDemandListsBuilt[whichList]) {
			gl.glCallList(transparentsOnDemandList[whichList]);
		} else {		
			final int SIDES = 12;
			final float EDGE_SIZE = 0.01f;
			 			
			float base = -0.5f*maxLength;
	
			float fill = length*maxLength;
			
			float radius = Math.max(dimensions[0], dimensions[2]) / 2;
			
			//On-demand generated list			
			transparentsOnDemandListsBuilt[whichList] = true;
			gl.glNewList(transparentsOnDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);					
				//Translate 'down' to center this object
				gl.glTranslatef(0f, 0f, base+fill);
				
				//Make a new quadratic object
				GLUquadric qobj = glu.gluNewQuadric();						
				
				//The shadow Element, draw dynamically colored elements first			
					//Bottom disk left out, since it's the top disk of the solid
												
					//Sides
					gl.glColor4f(color[0], color[1], color[2], ALPHA);
					glu.gluCylinder(qobj, radius, radius, maxLength-fill, SIDES, 1);
								
					gl.glTranslatef(0f, 0f, maxLength-fill);
					
					//Top disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Edge of top disk
					gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);					
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);		
				
				//Cleanup
				glu.gluDeleteQuadric(qobj);
			
			gl.glEndList();
		}
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	private void drawAlphaTube(GL2 gl, float alpha, float maxLength) {
		//Save the current modelview matrix
		gl.glPushMatrix();	
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
		
		if (transparentsOnDemandListsBuilt[whichList]) {
			gl.glCallList(transparentsOnDemandList[whichList]);
		} else {		
			final int SIDES = 12;
			final float EDGE_SIZE = 0.01f;
			 			
			float Yn = -0.5f*maxLength;	
			float Yf = maxLength;
			
			float radius = Math.max(dimensions[0], dimensions[2]) / 2;
			
			//On-demand generated list
			transparentsOnDemandListsBuilt[whichList] = true;
			gl.glNewList(transparentsOnDemandList[whichList], GL2.GL_COMPILE_AND_EXECUTE);
				
				//Translate 'down' to center this object
				gl.glTranslatef(0f, 0f, Yn);
							
				//Make a new quadratic object
				GLUquadric qobj = glu.gluNewQuadric();
						
				//The Solid Element, draw dynamically colored elements first
					gl.glColor4f(color[0], color[1], color[2], alpha);
					//Bottom disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Sides
					glu.gluCylinder(qobj, radius, radius, Yf, SIDES, 1);	
					
					//Translate 'up'
					gl.glTranslatef(0f, 0f, Yf);
					
					//Top disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
				//Now, draw the fixed color elements.	
					gl.glColor4f(0.8f,0.8f,0.8f, alpha);
					
					//Edge of top disk
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);	
					
					//Translate 'down'
					gl.glTranslatef(0f, 0f, -Yf);
					
					//Edge of bottom disk
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
									
				//Cleanup
				glu.gluDeleteQuadric(qobj);
			
			gl.glEndList();
		}
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
	private void startParticle(Particle p, float maxLength, float alpha) {
		float 	Yn, Yp;
		
		Yn = -0.5f*maxLength;
		Yp = 0.5f*maxLength;
		
		
		float[] theColor = new float[4];
		theColor[0] = color[0];
		theColor[1] = color[1];
		theColor[2] = color[2];
		theColor[3] = alpha;
		
		p.init(Yn, Yp, reversed, theColor);
	}
	
	private void drawParticles(GL2 gl) {
		//Save the current modelview matrix
		gl.glPushMatrix();	
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);
		
		if (reversed) {
			gl.glTranslatef(0f, 0f, 0.05f);
		} else {
			gl.glTranslatef(0f, 0f,-0.05f);
		}
		
		ArrayList<Particle> deadParticles = new ArrayList<Particle>();
		for (Particle p : localParticleStore) {
			gl.glColor4f(color[0], color[1], color[2], 1f);
			if (!p.draw(gl)) {
				deadParticles.add(p);				
			}
		}
		
		for (Particle p : deadParticles) {
			localParticleStore.remove(p);
			goggles.returnParticle(p);
		}
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}
	
}

package ibis.deploy.monitoring.visualization.gridvision;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;


public class DisplayListBuilder {
	GLUgl2 glu;

	private static final int ACCURACY = 20;
	private static final float ALPHA = 0.4f;
	
	private float LINE_WIDTH = 0.3f;
	final int SIDES = 12;
	final float EDGE_SIZE = 0.01f;
	
	private final float WIDTH = 0.25f;
	private final float HEIGHT = 1.0f;
	
	public static enum DisplayList {BAR, BAR_TRANSPARENCY,  
									SELECTED_BAR, SELECTED_BAR_TRANSPARENCY,
									TUBE, TUBE_TRANSPARENCY,
									SELECTED_TUBE, SELECTED_TUBE_TRANSPARENCY};
	
	private int[] barSolidsPointer, barTransparenciesPointer;	
	private int[] selectedBarSolidsPointer, selectedBarTransparenciesPointer;
	
	private int[] tubeSolidsPointer, tubeTransparanciesPointer;	
	private int[] selectedTubeSolidsPointer, selectedTubeTransparanciesPointer;
	
	public DisplayListBuilder(GL2 gl, GLUgl2 glu) {
		this.glu = glu;
		
		barSolidsPointer = new int[ACCURACY];
		selectedBarSolidsPointer = new int[ACCURACY];
		buildBarSolids(gl, barSolidsPointer, ACCURACY, false);
		buildBarSolids(gl, selectedBarSolidsPointer, ACCURACY, true);
		
		barTransparenciesPointer = new int[ACCURACY];
		selectedBarTransparenciesPointer = new int[ACCURACY];
		buildBarTransparencies(gl, barTransparenciesPointer, ACCURACY, false);
		buildBarTransparencies(gl, selectedBarTransparenciesPointer, ACCURACY, true);		
		
		tubeSolidsPointer = new int[ACCURACY];
		selectedTubeSolidsPointer = new int[ACCURACY];
		buildTubeSolids(gl, tubeSolidsPointer, ACCURACY, false);
		buildTubeSolids(gl, selectedTubeSolidsPointer, ACCURACY, true);
		
		tubeTransparanciesPointer = new int[ACCURACY];
		selectedTubeTransparanciesPointer = new int[ACCURACY];
		buildTubeTransparencies(gl, tubeTransparanciesPointer, ACCURACY, false);		
		buildTubeTransparencies(gl, selectedTubeTransparanciesPointer, ACCURACY, true);
		
		
	}
	
	public int[] getPointer(DisplayList whichPointer) {
		int[] pointer = null;
		if (whichPointer == DisplayList.BAR) {
			pointer = barSolidsPointer;
		} else if (whichPointer == DisplayList.BAR_TRANSPARENCY) {
			pointer = barTransparenciesPointer;
		} else if (whichPointer == DisplayList.SELECTED_BAR) {
			pointer = selectedBarSolidsPointer;
		}  else if (whichPointer == DisplayList.SELECTED_BAR_TRANSPARENCY) {
			pointer = selectedBarTransparenciesPointer;
		}else if (whichPointer == DisplayList.TUBE) {
			pointer = tubeSolidsPointer;
		} else if (whichPointer == DisplayList.SELECTED_TUBE) {
			pointer = selectedTubeSolidsPointer;
		} else if (whichPointer == DisplayList.TUBE_TRANSPARENCY) {
			pointer = tubeTransparanciesPointer;
		} else if (whichPointer == DisplayList.SELECTED_TUBE_TRANSPARENCY) {
			pointer = selectedTubeTransparanciesPointer;
		}
		return pointer;
	}
		
	private void buildBarSolids(GL2 gl, int[] pointers, int amount, boolean selected) {
		gl.glLineWidth(LINE_WIDTH);
		
		float 	Xn = -0.5f*WIDTH,
				Xp =  0.5f*WIDTH,
				Yn = -0.5f*HEIGHT,
				//Yp =  0.5f*HEIGHT,
				Zn = -0.5f*WIDTH,
				Zp =  0.5f*WIDTH;
		
		float Yf = 0.0f;
				
		pointers[0] = gl.glGenLists(amount);		
		
		for (int i=0; i<(amount); i++) {
			pointers[i]   = pointers[0]+i;
			
			Yf = ((HEIGHT/amount)*(i))-(0.5f*HEIGHT);
			
			//The solid area
			gl.glNewList(pointers[i], GL2.GL_COMPILE);				
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
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					//TOP
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					//BOTTOM
					gl.glVertex3f( Xn, Yn, Zn);
					gl.glVertex3f( Xp, Yn, Zn);
					gl.glVertex3f( Xp, Yn, Zp);
					gl.glVertex3f( Xn, Yn, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					//FRONT
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xn, Yn, Zp);
					gl.glVertex3f( Xp, Yn, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					//BACK
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xp, Yn, Zn);
					gl.glVertex3f( Xn, Yn, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					//LEFT
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yn, Zn);
					gl.glVertex3f( Xn, Yn, Zp);
					gl.glVertex3f( Xn, Yf, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					//RIGHT
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yn, Zp);
					gl.glVertex3f( Xp, Yn, Zn);
					gl.glVertex3f( Xp, Yf, Zn);
				gl.glEnd();
			gl.glEndList();	
		}
	}
	
	private void buildBarTransparencies(GL2 gl, int[] pointers, int amount, boolean selected) {		
		gl.glLineWidth(LINE_WIDTH);
		
		float 	Xn = -0.5f*WIDTH,
				Xp =  0.5f*WIDTH,
				//Yn = -0.5f*HEIGHT,
				Yp =  0.5f*HEIGHT,
				Zn = -0.5f*WIDTH,
				Zp =  0.5f*WIDTH;
		
		float Yf = 0.0f;
				
		pointers[0] = gl.glGenLists(amount);
		
		for (int i=0; i<(amount); i++) {
			pointers[i]   = pointers[0]+i;
			
			Yf = ((HEIGHT/amount)*i)-(0.5f*HEIGHT);
						
			//The transparent area
			gl.glNewList(pointers[i], GL2.GL_COMPILE);
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
					if (selected) {
						gl.glColor4f(1f,1f,1f, ALPHA);
					} else {
						gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);
					}
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
					if (selected) {
						gl.glColor4f(1f,1f,1f, ALPHA);
					} else {
						gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);
					}
					//FRONT
					gl.glVertex3f( Xn, Yp, Zp);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yp, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor4f(1f,1f,1f, ALPHA);
					} else {
						gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);
					}
					//BACK
					gl.glVertex3f( Xp, Yp, Zn);
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yp, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor4f(1f,1f,1f, ALPHA);
					} else {
						gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);
					}
					//LEFT
					gl.glVertex3f( Xn, Yp, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xn, Yp, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					if (selected) {
						gl.glColor4f(1f,1f,1f, ALPHA);
					} else {
						gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);
					}
					//RIGHT
					gl.glVertex3f( Xp, Yp, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xp, Yp, Zn);
				gl.glEnd();
			gl.glEndList();		
		}
	}
	
	protected void buildTubeSolids(GL2 gl, int[] pointers, int amount, boolean selected) {			
		pointers[0] = gl.glGenLists(amount);
		
		float 	radius 	=  0.5f*WIDTH,
				length;
				
		for (int i=0; i<(amount); i++) {
			gl.glLoadIdentity();
			
			pointers[i] = pointers[0]+i;
			
			length = ((HEIGHT/amount)*i);
						
			gl.glNewList(pointers[i], GL2.GL_COMPILE);
				gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
				
				//Translate 'down' to center
				gl.glTranslatef(0f, 0f, -0.5f);
							
				//Make a new quadratic object
				GLUquadric qobj = glu.gluNewQuadric();
						
				//The Solid Element, draw dynamically colored elements first				
					//Bottom disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Sides
					glu.gluCylinder(qobj, radius, radius, length, SIDES, 1);	
					
					//Translate 'up'
					gl.glTranslatef(0f, 0f, length);
					
					//Top disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
				//Now, draw the fixed color elements.	
					//Edge of top disk
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);	
					
					//Translate 'down'
					gl.glTranslatef(0f, 0f, -length);
					
					//Edge of bottom disk
					if (selected) {
						gl.glColor3f(1f,1f,1f);
					} else {
						gl.glColor3f(0.8f,0.8f,0.8f);
					}
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);
									
				//Cleanup
				glu.gluDeleteQuadric(qobj);
			gl.glEndList();
		}		
	}
	
	protected void buildTubeTransparencies(GL2 gl, int[] pointers, int amount, boolean selected) {		
		pointers[0] = gl.glGenLists(amount);
		
		float 	radius 	= 0.5f*WIDTH,
				Yp 		= HEIGHT,
				Yf;				
				
		for (int i=0; i<(amount); i++) {
			gl.glLoadIdentity();
			
			pointers[i] = pointers[0]+i;
			
			Yf = ((HEIGHT/amount)*i);
		
			gl.glNewList(pointers[i], GL2.GL_COMPILE);
				gl.glRotatef(-90f, 1.0f, 0.0f, 0.0f);
				
				//Translate 'down' to center
				gl.glTranslatef(0f, 0f, -0.5f);
				
				//Make a new quadratic object
				GLUquadric qobj = glu.gluNewQuadric();
						
				//Move away from the Solid Element
					gl.glTranslatef(0f, 0f, Yf);
				
				//The shadow Element, draw dynamically colored elements first			
					//Bottom disk left out, since it's the top disk of the solid
												
					//Sides
					glu.gluCylinder(qobj, radius, radius, Yp-Yf, SIDES, 1);
								
					gl.glTranslatef(0f, 0f, Yp-Yf);
					
					//Top disk
					glu.gluDisk(qobj, 0.0, radius, SIDES, 1);
					
					//Edge of top disk
					if (selected) {
						gl.glColor4f(1f,1f,1f,1f);
					} else {
						gl.glColor4f(0.8f,0.8f,0.8f, ALPHA);
					}
					glu.gluCylinder(qobj, radius, radius, EDGE_SIZE, SIDES, 1);		
				
				//Cleanup
				glu.gluDeleteQuadric(qobj);
			gl.glEndList();
		}		
	}	
}

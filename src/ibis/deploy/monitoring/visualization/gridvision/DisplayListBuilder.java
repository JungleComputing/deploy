package ibis.deploy.monitoring.visualization.gridvision;
import javax.media.opengl.GL2;


public class DisplayListBuilder {
	private static final int ACCURACY = 20;
	private float LINE_WIDTH = 0.3f;
	private float SELECTED_LINE_WIDTH = 1f;
	final float WIDTH = 0.25f;
	final float HEIGHT = 1.0f;
	
	public static enum DisplayList { BAR_TRANSPARENCY, BAR, SELECTED_BAR_TRANSPARENCY, SELECTED_BAR }
	
	private int[] transparancyPointer;
	private int[] barPointer;
	
	private int[] selectedTransparancyPointer;
	private int[] selectedBarPointer;
	
	public DisplayListBuilder(GL2 gl) {
		transparancyPointer = new int[ACCURACY];
		buildTransparancies(gl, ACCURACY);
		barPointer = new int[ACCURACY];
		buildBars(gl, ACCURACY);
		selectedTransparancyPointer = new int[ACCURACY];
		buildSelectedTransparancies(gl, ACCURACY);
		selectedBarPointer = new int[ACCURACY];
		buildSelectedBars(gl, ACCURACY);
	}
	
	public int[] getPointer(DisplayList whichPointer) {
		int[] pointer = null;
		if (whichPointer == DisplayList.BAR_TRANSPARENCY) {
			pointer = transparancyPointer;
		} else if (whichPointer == DisplayList.BAR) {
			pointer = barPointer;
		} else if (whichPointer == DisplayList.SELECTED_BAR_TRANSPARENCY) {
			pointer = selectedTransparancyPointer;
		} else if (whichPointer == DisplayList.SELECTED_BAR) {
			pointer = selectedBarPointer;
		}
		return pointer;
	}
	
	private void buildTransparancies(GL2 gl, int amount) {		
		gl.glLineWidth(LINE_WIDTH);
		
		float 	Xn = -0.5f*WIDTH,
				Xp =  0.5f*WIDTH,
				//Yn = -0.5f*HEIGHT,
				Yp =  0.5f*HEIGHT,
				Zn = -0.5f*WIDTH,
				Zp =  0.5f*WIDTH;
		
		float Yf = 0.0f;
				
		transparancyPointer[0] = gl.glGenLists(amount);
		
		for (int i=0; i<(amount); i++) {
			transparancyPointer[i]   = transparancyPointer[0]+i;
			
			Yf = ((HEIGHT/amount)*i)-(0.5f*HEIGHT);
						
			//The transparent area
			gl.glNewList(transparancyPointer[i], GL2.GL_COMPILE);
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
	}
	
	private void buildBars(GL2 gl, int amount) {
		gl.glLineWidth(LINE_WIDTH);
		
		float 	Xn = -0.5f*WIDTH,
				Xp =  0.5f*WIDTH,
				Yn = -0.5f*HEIGHT,
				//Yp =  0.5f*HEIGHT,
				Zn = -0.5f*WIDTH,
				Zp =  0.5f*WIDTH;
		
		float Yf = 0.0f;
				
		barPointer[0] = gl.glGenLists(amount);		
		
		for (int i=0; i<(amount); i++) {
			barPointer[i]   = barPointer[0]+i;
			
			Yf = ((HEIGHT/amount)*(i))-(0.5f*HEIGHT);
			
			//The solid area
			gl.glNewList(barPointer[i], GL2.GL_COMPILE);
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
			gl.glEndList();	
		}
	}
	
	private void buildSelectedTransparancies(GL2 gl, int amount) {		
		gl.glLineWidth(SELECTED_LINE_WIDTH);
		
		float 	Xn = -0.5f*WIDTH,
				Xp =  0.5f*WIDTH,
				//Yn = -0.5f*HEIGHT,
				Yp =  0.5f*HEIGHT,
				Zn = -0.5f*WIDTH,
				Zp =  0.5f*WIDTH;
		
		float Yf = 0.0f;
				
		selectedTransparancyPointer[0] = gl.glGenLists(amount);
		
		for (int i=0; i<(amount); i++) {
			selectedTransparancyPointer[i]   = selectedTransparancyPointer[0]+i;
			
			Yf = ((HEIGHT/amount)*i)-(0.5f*HEIGHT);
						
			//The transparent area
			gl.glNewList(selectedTransparancyPointer[i], GL2.GL_COMPILE);
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
					gl.glColor3f(1f,1f,1f);
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
					gl.glColor3f(1f,1f,1f);
					//FRONT
					gl.glVertex3f( Xn, Yp, Zp);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yp, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//BACK
					gl.glVertex3f( Xp, Yp, Zn);
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yp, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//LEFT
					gl.glVertex3f( Xn, Yp, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xn, Yp, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//RIGHT
					gl.glVertex3f( Xp, Yp, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xp, Yp, Zn);
				gl.glEnd();
			gl.glEndList();		
		}
	}
	
	private void buildSelectedBars(GL2 gl, int amount) {
		gl.glLineWidth(SELECTED_LINE_WIDTH);
		
		float 	Xn = -0.5f*WIDTH,
				Xp =  0.5f*WIDTH,
				Yn = -0.5f*HEIGHT,
				//Yp =  0.5f*HEIGHT,
				Zn = -0.5f*WIDTH,
				Zp =  0.5f*WIDTH;
		
		float Yf = 0.0f;
				
		selectedBarPointer[0] = gl.glGenLists(amount);		
		
		for (int i=0; i<(amount); i++) {
			selectedBarPointer[i]   = selectedBarPointer[0]+i;
			
			Yf = ((HEIGHT/amount)*(i))-(0.5f*HEIGHT);
			
			//The solid area
			gl.glNewList(selectedBarPointer[i], GL2.GL_COMPILE);
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
					gl.glColor3f(1f,1f,1f);
					//TOP
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yf, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//BOTTOM
					gl.glVertex3f( Xn, Yn, Zn);
					gl.glVertex3f( Xp, Yn, Zn);
					gl.glVertex3f( Xp, Yn, Zp);
					gl.glVertex3f( Xn, Yn, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//FRONT
					gl.glVertex3f( Xn, Yf, Zp);
					gl.glVertex3f( Xn, Yn, Zp);
					gl.glVertex3f( Xp, Yn, Zp);
					gl.glVertex3f( Xp, Yf, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//BACK
					gl.glVertex3f( Xp, Yf, Zn);
					gl.glVertex3f( Xp, Yn, Zn);
					gl.glVertex3f( Xn, Yn, Zn);
					gl.glVertex3f( Xn, Yf, Zn);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//LEFT
					gl.glVertex3f( Xn, Yf, Zn);
					gl.glVertex3f( Xn, Yn, Zn);
					gl.glVertex3f( Xn, Yn, Zp);
					gl.glVertex3f( Xn, Yf, Zp);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glColor3f(1f,1f,1f);
					//RIGHT
					gl.glVertex3f( Xp, Yf, Zp);
					gl.glVertex3f( Xp, Yn, Zp);
					gl.glVertex3f( Xp, Yn, Zn);
					gl.glVertex3f( Xp, Yf, Zn);
				gl.glEnd();
			gl.glEndList();	
		}
	}
}

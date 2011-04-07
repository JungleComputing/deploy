package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

public class Particle implements Moveable {
	private static final float SPEED = 0.1f;
	
	Timer timer;
	
	float start, current, stop;
	boolean shown;

	private boolean listBuilt;
	private int list;
	
	public Particle() {
		start = 0f;
		timer = new Timer(this);
		new Thread(timer).start();
		
		shown = false;
		listBuilt = false;
	}
	
	public void init(float start, float stop) {
		this.start = start;
		this.current = start;
		this.stop = stop;
		
		float length = stop - start;
		long time = (long) (length / SPEED);
				
		timer.startTiming(time);
		shown = true;
	}
	
	public void doMoveFraction(float fraction) {		
		if (fraction < 0.0f) { 
			System.out.println("too small");
			return; 
		} else if (fraction > 1.0f) {
			shown = false;
		}
		
		current = start + (fraction * ( stop - start ));
		System.out.println(current);
	}
	
	public void draw(GL2 gl) {
		if (shown) {
			//Save the current modelview matrix
			gl.glPushMatrix();		
			
			gl.glTranslatef(0f, current, 0f);
			
			if (listBuilt) {			
				gl.glCallList(list);
			} else {
				listBuilt = true;
				list = gl.glGenLists(1);
			
				float 	Xn = -0.5f*0.05f,
						Xp =  0.5f*0.05f,
						Yn = -0.5f*0.05f,
						Zn = -0.5f*0.05f,
						Zp =  0.5f*0.05f;
				
				float Yf = 0.05f;
				
				gl.glNewList(list, GL2.GL_COMPILE_AND_EXECUTE);
				
					gl.glBegin(GL2.GL_QUADS);	
						gl.glColor3f(0.0f,0.0f,0.8f);
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
				
				gl.glEndList();
			}
			
			//Restore the old modelview matrix
			gl.glPopMatrix();
		}
	}
}

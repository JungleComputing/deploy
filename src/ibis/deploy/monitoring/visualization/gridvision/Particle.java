package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

public class Particle {
	private static final float SPEED = 4f;
		
	float current, stop;
	boolean shown;

	private boolean listBuilt;
	private int list;
	private int number;
	
	float[] color;
	private boolean reversed;
	
	public Particle(int number) {				
		shown = false;
		listBuilt = false;
		list = -1;
		
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void init(float start, float stop, boolean reversed, float[] color) {
		if (!reversed) {
			this.current = start;
			this.stop = stop;
		} else {
			this.current = stop;
			this.stop = start;
		}
		this.reversed = reversed;
		
		this.color = new float[4];		
		for (int i = 0; i < 4; i++) {
			this.color[i] = color[i];
		}
		
		shown = true;
		listBuilt = false;
		
	}
	
	public void doMoveFraction(float fraction) {
		if (!reversed) {
			float toMove = fraction*SPEED;
			current += toMove;
			
			if (current > stop) {
				shown = false;
			}
		} else {
			float toMove = fraction*SPEED;
			current -= toMove;
			
			if (current < stop) {
				shown = false;
			}
		}
	}
	
	public boolean draw(GL2 gl) {
		if (shown) {
			//Save the current modelview matrix
			gl.glPushMatrix();		
			
			gl.glTranslatef(0f, current, 0f);
			
			if (listBuilt) {			
				gl.glCallList(list);
			} else {				
				listBuilt = true;
				gl.glDeleteLists(list, 1);
				list = gl.glGenLists(1);
			
				float 	Xn = -0.5f*0.05f,
						Xp =  0.5f*0.05f,
						Yn = -0.5f*0.05f,
						Zn = -0.5f*0.05f,
						Zp =  0.5f*0.05f;
				
				float Yf = 0.05f;
				
				gl.glNewList(list, GL2.GL_COMPILE_AND_EXECUTE);
				
					gl.glBegin(GL2.GL_QUADS);
						gl.glColor4f(color[0], color[1], color[2], color[3]);
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
			
			return true;
		}
		
		return false;
	}
	
}

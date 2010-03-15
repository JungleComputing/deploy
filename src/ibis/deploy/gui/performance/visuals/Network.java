package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.Site;
import ibis.deploy.gui.performance.exceptions.ArraySizeMismatchException;
import ibis.ipl.IbisIdentifier;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class Network {	
	
	/*
	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
	
	public static Float[] CPU_HIGH_COLOR	= {1.0f, 0.0f, 0.0f};
	public static Float[] CPU_AVG_COLOR		= {1.0f, 0.5f, 0.0f};
	public static Float[] CPU_LOW_COLOR		= {1.0f, 1.0f, 0.0f};
	public static Float[] MEM_HIGH_COLOR	= {0.5f, 1.0f, 0.0f};
	public static Float[] MEM_AVG_COLOR		= {0.0f, 1.0f, 0.0f};
	public static Float[] MEM_LOW_COLOR		= {0.0f, 1.0f, 0.5f};
	public static Float[] _0FF	= {0.0f, 1.0f, 1.0f};
	public static Float[] _08F	= {0.0f, 0.5f, 1.0f};
	public static Float[] _00F	= {0.0f, 0.0f, 1.0f};
	
	public static Float[] NETWORK_LINK_COLOR= {0.5f, 0.0f, 1.0f};
	public static Float[] _F0F	= {1.0f, 0.0f, 1.0f};
	public static Float[] _F08	= {1.0f, 0.0f, 0.5f};
	
	private Site[] sites;
	private Link[] links;
	private String name;
	
	private Float[][] colors;
	private String[] names;
		
	private float scaleXZ;
	private float scaleY;
	private boolean fill;	
	
	GLU glu;
	
	public Network(String name, Site[] sites, Map<String, String> connections, float[] linkColor) {		
		glu = new GLU();		
		this.name = name;		
		this.sites = sites;		
		
		int i = 0;
		for (Map.Entry<String, String> entry : connections.entrySet()) {
			links[i] = new Link();
		}
		
	}
	
	public boolean isMetaCollection() {
		return metaCollection;
	}
	
	public String getName() {
		return name;
	}	
	
	public String[] getElementNames() {
		return names;
	}	
	
	public Float[][] getColors() {
		return colors;
	}
	
	public boolean getFill() {
		return fill;
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		if (metaCollection) {
			for (int i=0; i<collections.length; i++) {
				collections[i].setSize(width, height);
			}
		}
	}
		
	public void drawThis(GL gl, int clusterForm, int individualForm, int renderMode, Float[]... values) throws ArraySizeMismatchException {	
		if (metaCollection) {
			for (int i=0; i<collections.length; i++) {
				//TODO space between collections of collections
				if (clusterForm == Network.CITYSCAPE) {
					collections[i].drawCityscapeCluster(gl, clusterForm, individualForm, renderMode, values[i]);					
				} else if (clusterForm == Network.CIRCLE) {
					collections[i].drawCircleCluster(gl, clusterForm, individualForm, renderMode, values[i]);
				}
			}
		} else {
			if (links.length != values.length) throw new ArraySizeMismatchException();
			for (int i=0; i<links.length; i++) {
				if (clusterForm == Network.CITYSCAPE) {			
					drawCityscape(gl, individualForm, renderMode, Integer.parseInt(names[i]), values[i]);	
				} else if (clusterForm == Network.CIRCLE) {
					drawCircle(gl, individualForm, renderMode, Integer.parseInt(names[i]), values[i]);
				}
			}
		}
	}
	
	protected void drawCityscape(GL gl, int form, int renderMode, int glName, Float[] values) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		
		
		//get the breakoff point for rows and columns
		int root = (int)Math.ceil(Math.sqrt(links.length));		
				
		for (int i=0; i < links.length; i++) {		
			//Move to next row (if applicable)
			if (i != 0 && i % root == 0) {
				gl.glTranslatef((scaleXZ+0.1f)*root, 0.0f, scaleXZ+0.1f);		
			}
						
			//Setup the form
			try {
				links[i].setMode(form);
				links[i].setFill(values[i]);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			//Draw the form
			links[i].drawThis(gl, renderMode, glName+i);			
			
			//Move to next bar in the row
			gl.glTranslatef(-(scaleXZ+0.1f), 0.0f, 0.0f);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCircle(GL gl, int form, int renderMode, int glName, Float[] values) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		
				
		double angle  = 2*Math.PI / values.length;
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
						
		for (int i=0; i < values.length; i++) {						
			//move towards the position
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
								
			//Setup the form
			try {
				links[i].setMode(form);
				links[i].setFill(values[i]);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			//Draw the form
			links[i].drawThis(gl, renderMode, glName+i);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscapeCluster(GL gl, int form, int renderMode, int glName, Float[] values) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		
		
		//get the breakoff point for rows and columns
		int root = (int)Math.ceil(Math.sqrt(links.length));		
				
		for (int i=0; i < links.length; i++) {		
			//Move to next row (if applicable)
			if (i != 0 && i % root == 0) {
				gl.glTranslatef((scaleXZ+0.1f)*root, 0.0f, scaleXZ+0.1f);		
			}
						
			//Setup the form
			try {
				links[i].setMode(form);
				links[i].setFill(values[i]);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			//Draw the form
			links[i].drawThis(gl, renderMode, glName+i);			
			
			//Move to next bar in the row
			gl.glTranslatef(-(scaleXZ+0.1f), 0.0f, 0.0f);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCircleCluster(GL gl, int form, int renderMode, int glName, Float[] values) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		
				
		double angle  = 2*Math.PI / values.length;
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
						
		for (int i=0; i < values.length; i++) {						
			//move towards the position
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
								
			//Setup the form
			try {
				links[i].setMode(form);
				links[i].setFill(values[i]);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			//Draw the form
			links[i].drawThis(gl, renderMode, glName+i);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	*/
}

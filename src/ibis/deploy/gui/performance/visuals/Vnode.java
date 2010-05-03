package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Node;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;

public class Vnode extends Vobject implements VobjectInterface {	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
			
	private Node node;
	
	public Vnode(PerfVis perfvis, VisualManager visman, Node node) {
		super(perfvis, visman);
		this.node = node;
		this.currentForm = CITYSCAPE;
		
		HashMap<String, Float[]> colors = node.getMetricsColors();
				
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, entry.getValue()));

			//TODO REMOVE DEBUG
			System.out.println("Making Metric!");
		}
		
		try {
			setForm(Vnode.CITYSCAPE);			
		} catch (ModeUnknownException e) {			
			e.printStackTrace();
		}
	}

	public void setForm(int nodeForm) throws ModeUnknownException {
		if (nodeForm != Vnode.CITYSCAPE && nodeForm != Vnode.CIRCLE) {
			throw new ModeUnknownException();
		}
		this.currentForm = nodeForm;
				
		//recalculate the outer radius for this form
		setSize(scaleXZ, scaleY);
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			entry.getValue().setSize(width, height);
		}
		
		if (currentForm == Vnode.CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(vmetrics.size()))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(vmetrics.size()))*(scaleXZ+0.1f));
			
			//3d across
			this.radius = (float) Math.sqrt(  Math.pow(horz, 2)
										 	+ Math.pow(vert, 2)
										 	+ Math.pow(dept, 2));
			
		} else if (currentForm == Vnode.CIRCLE) {
			double angle  = 2*Math.PI / vmetrics.size();
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			this.radius = (int)innerRadius+(int)scaleY;
		}
	}
	
	public void update(){
		HashMap<String, Float> stats = node.getMonitoredNodeMetrics();
		for (Map.Entry<String, Float> entry : stats.entrySet()) {
			try {
				vmetrics.get(entry.getKey()).setValue(entry.getValue());
			} catch (ValueOutOfBoundsException e) {	
				System.out.println("VALUE: "+entry.getValue()+" OUT OF BOUNDS!");
				e.printStackTrace();
			}
		}
	}
	
	public void drawThis(GL gl, int glMode) {
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		

		//Move towards the intended location
		gl.glTranslatef(location[0], location[1], location[2]);
		
		//Draw the desired form
		if (currentForm == Vnode.CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (currentForm == Vnode.CIRCLE) {
			drawCircle(gl, glMode);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(vmetrics.size()));
		int columns 	= (int)Math.floor(Math.sqrt(vmetrics.size()));
		
		//Center the drawing around the location	
		Float[] shift = new Float[3];
		shift[0] =  ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] = 0.0f;
		shift[2] = -((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f;
		setRelativeLocation(shift);
		
		int row = 0, column = 0, i = 0;
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			row = i % rows;
			//Move to next row (if applicable)
			if (i != 0 && row == 0) {
				column++;						
			}
						
			//Setup the form
			try {
				entry.getValue().setLocation(location);
				
				shift[0] = -(scaleXZ+separation)*row;
				shift[1] = 0.0f;
				shift[2] =  (scaleXZ+separation)*column;
				entry.getValue().setRelativeLocation(shift);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
			entry.getValue().drawThis(gl, glMode);
			i++;
		}
	}
	
	protected void drawCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / vmetrics.size();
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
				
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {					
			//move towards the position			
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
								
			//Draw the form
			entry.getValue().drawThis(gl, glMode);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
	}
}

package ibis.deploy.gui.performance.visuals;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.dataholders.Node;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;

public class Vlink extends Vobject implements VobjectInterface {
	public static int CITYSCAPE = 5001;	
		
	private Node node;
	
	public Vlink(PerfVis perfvis, Node node, Vobject from, Vobject to) {
		super(perfvis);
		this.node = node;
		
		//Preparing the metrics vobjects for the average values
		HashMap<String, Float[]> colors = node.getLinkColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, entry.getValue(), from, to));		
		}
	}	
	
	public void setForm(int nodeForm) throws ModeUnknownException {
		if (nodeForm != Vnode.CITYSCAPE) {
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
			
		}
	}
	
	public void update(){
		HashMap<String, Float> stats = node.getMonitoredLinkMetrics();
		for (Map.Entry<String, Float> entry : stats.entrySet()) {
			try {
				vmetrics.get(entry.getKey()).setValue(entry.getValue());
			} catch (ValueOutOfBoundsException e) {				
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
		setRelativeX( ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f );
		//setRelativeY(-(0.5f*scaleY));
		setRelativeZ(-((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f );
		
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
				entry.getValue().setRelativeX(-(scaleXZ+separation)*row);
				entry.getValue().setRelativeZ( (scaleXZ+separation)*column);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
			entry.getValue().drawThis(gl, glMode);
			i++;
		}
	}
}
package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Node;
import ibis.deploy.gui.performance.dataholders.Site;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

public class Vsite extends Vobject implements VobjectInterface {	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
	
	private List<Vnode> vnodes;
	private Site site;
		
	public Vsite(PerfVis perfvis, VisualManager visman, Site site) {
		super(perfvis, visman);
		this.site = site;
		this.currentForm = CITYSCAPE;
		
		//Preparing the vnodes
		Node[] nodes = site.getSubConcepts();
		vnodes = new ArrayList<Vnode>();
				
		for (Node node : nodes) {
			vnodes.add(new Vnode(perfvis, visman, node));
		}
		
		//Preparing the metrics vobjects for the average values
		HashMap<String, Float[]> colors = site.getMetricsColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, entry.getValue()));		
		}
	}

	public void setForm(int siteForm) throws ModeUnknownException {
		if (siteForm != Vsite.CITYSCAPE && siteForm != Vsite.CIRCLE) {
			throw new ModeUnknownException();
		}
		this.currentForm = siteForm;
				
		//recalculate the outer radius for this form
		setSize(scaleXZ, scaleY);
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (Vnode vnode : vnodes) {
			vnode.setSize(width, height);
		}
		
		if (currentForm == Vnode.CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(vnodes.size()))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(vnodes.size()))*(scaleXZ+0.1f));
			
			//3d across
			this.radius = (float) Math.sqrt(  Math.pow(horz, 2)
											+ Math.pow(vert, 2)
											+ Math.pow(dept, 2));
			
		} else if (currentForm == Vnode.CIRCLE) {
			double angle  = 2*Math.PI / vnodes.size();
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			radius = (int)innerRadius+(int)scaleY;
		}
	}
	
	public void update() {		
		for (Vnode vnode : vnodes) {
			vnode.update();			
		}
		HashMap<String, Float> stats = site.getMonitoredNodeMetrics();
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
		if (currentForm == Vsite.CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (currentForm == Vsite.CIRCLE) {
			drawCircle(gl, glMode);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(vnodes.size()));
		int columns 	= (int)Math.floor(Math.sqrt(vnodes.size()));
		
		//Center the drawing around the location		
		setRelativeX( ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f );
		//setRelativeY(-(0.5f*scaleY));
		setRelativeZ(-((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f );
		
		int row = 0, column = 0, i =0;
		for (Vnode vnode : vnodes) {
			row = i % rows;
			//Move to next row (if applicable)
			if (i != 0 && row == 0) {
				column++;
			}
						
			//Setup the form
			try {
				vnode.setLocation(location);				
				vnode.setRelativeX(-(scaleXZ+separation)*row);
				vnode.setRelativeZ( (scaleXZ+separation)*column);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
			vnode.drawThis(gl, glMode);	
			i++;
		}
	}
	
	protected void drawCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / vnodes.size();
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
						
		for (Vnode vnode : vnodes) {						
			//move towards the position			
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
											
			//Draw the form
			vnode.drawThis(gl, glMode);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
	}
}

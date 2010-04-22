package ibis.deploy.gui.performance.newtry.Vrarchy;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.newtry.dataobjects.Pool;
import ibis.deploy.gui.performance.newtry.dataobjects.Site;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

public class Vpool extends Vobject implements VobjectInterface {	
	public static int CITYSCAPE = 11;
	public static int CIRCLE = 12;
	
	private List<Vsite> vsites;	
	private Pool pool; 
		
	public Vpool(PerfVis perfvis, Pool pool) {
		super(perfvis);
		this.pool = pool;
		
		Site[] sites = (Site[])pool.getSubConcepts();
		vsites = new ArrayList<Vsite>();
				
		for (Site site : sites) {
			vsites.add(new Vsite(perfvis, site));
		}	
		
		//Preparing the metrics vobjects for the average values
		HashMap<String, Float[]> colors = pool.getMetricsColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, entry.getValue()));		
		}
	}

	public void setForm(int poolForm) throws ModeUnknownException {
		if (poolForm != Vpool.CITYSCAPE && poolForm != Vpool.CIRCLE) {
			throw new ModeUnknownException();
		}
		this.currentForm = poolForm;
				
		//recalculate the outer radius for this form
		setSize(scaleXZ, scaleY);
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (Vsite vsite : vsites) {
			vsite.setSize(width, height);
		}
		
		if (currentForm == Vnode.CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(vsites.size()))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(vsites.size()))*(scaleXZ+0.1f));
			
			//3d across
			this.radius = (float) Math.sqrt(  Math.pow(horz, 2)
											+ Math.pow(vert, 2)
											+ Math.pow(dept, 2));
			
		} else if (currentForm == Vnode.CIRCLE) {
			double angle  = 2*Math.PI / vsites.size();
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			radius = (int)innerRadius+(int)scaleY;
		}
	}
	
	public void update() {		
		for (Vsite vsite : vsites) {
			vsite.update();			
		}
		HashMap<String, Float> stats = pool.getMonitoredNodeMetrics();
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
		if (currentForm == Vpool.CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (currentForm == Vpool.CIRCLE) {
			drawCircle(gl, glMode);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(vsites.size()));
		int columns 	= (int)Math.floor(Math.sqrt(vsites.size()));
		
		//Center the drawing around the location		
		setRelativeX( ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f );
		//setRelativeY(-(0.5f*scaleY));
		setRelativeZ(-((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f );
		
		int row = 0, column = 0, i = 0;
		for (Vsite vsite : vsites) {
			row = i % rows;
			//Move to next row (if applicable)
			if (i != 0 && row == 0) {
				column++;
			}
						
			//Setup the form
			try {
				vsite.setLocation(location);				
				vsite.setRelativeX(-(scaleXZ+separation)*row);
				vsite.setRelativeZ( (scaleXZ+separation)*column);
					
			} catch (Exception e) {					
				e.printStackTrace();
			}
			
			//Draw the form
			vsite.drawThis(gl, glMode);	
			i++;
		}
	}
	
	protected void drawCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / vsites.size();
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
						
		for (Vsite vsite : vsites) {						
			//move towards the position			
			gl.glTranslatef(radius, 0.0f, 0.0f);
			gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
											
			//Draw the form
			vsite.drawThis(gl, glMode);
			
			//Move back to the center			
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(-radius, 0.0f, 0.0f);
			
			//Turn for the next iteration		
			gl.glRotatef(degs, 0.0f, 0.0f, 1.0f);
		}
	}
}

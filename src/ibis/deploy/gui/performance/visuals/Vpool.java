package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Pool;
import ibis.deploy.gui.performance.dataholders.Site;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

public class Vpool extends Vobject implements VobjectInterface {	
	private List<Vsite> vsites;	
	private Pool pool; 
		
	public Vpool(PerfVis perfvis, VisualManager visman, Pool pool) {
		super(perfvis, visman);
		this.pool = pool;
		this.currentCollectionForm = Vobject.COLLECTION_CITYSCAPE;
				
		Site[] sites = pool.getSubConcepts();
		vsites = new ArrayList<Vsite>();
				
		for (Site site : sites) {
			vsites.add(new Vsite(perfvis, visman, site));			
		}
				
		initializeMetrics();
	}
	
	private void initializeMetrics() {
		vmetrics.clear();
		
		HashMap<String, Float[]> colors = pool.getMetricsColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, entry.getValue()));
		}		
	}

	/*
	public void setForm(int poolForm) throws ModeUnknownException {
		if (poolForm != Vobject.COLLECTION_CITYSCAPE && poolForm != Vobject.COLLECTION_CIRCLE) {
			throw new ModeUnknownException();
		}
		this.currentCollectionForm = poolForm;
				
		//recalculate the outer radius for this form
		setSize(scaleXZ, scaleY);
	}
		
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		for (Vsite vsite : vsites) {
			vsite.setSize(width, height);
		}
		
		if (currentCollectionForm == Vobject.COLLECTION_CITYSCAPE) {
			int horz = (int)(Math.ceil(Math.sqrt(vsites.size()))*(scaleXZ+0.1f));
			int vert = (int)scaleY;
			int dept = (int)(Math.ceil(Math.sqrt(vsites.size()))*(scaleXZ+0.1f));
			
			//3d across
			this.radius = (float) Math.sqrt(  Math.pow(horz, 2)
											+ Math.pow(vert, 2)
											+ Math.pow(dept, 2));
			
		} else if (currentCollectionForm == Vobject.COLLECTION_CIRCLE) {
			double angle  = 2*Math.PI / vsites.size();
			float innerRadius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
			innerRadius = Math.max(innerRadius, 0);
			
			radius = (int)innerRadius+(int)scaleY;
		}
	}
	*/
	
	public void update() {		
		for (Vsite vsite : vsites) {
			vsite.update();			
		}
		HashMap<String, Float> stats = pool.getMonitoredNodeMetrics();
		for (Map.Entry<String, Float> entry : stats.entrySet()) {
			try {
				String metricName = entry.getKey();
				Float metricValue = entry.getValue();
				Vmetric visual = vmetrics.get(metricName);
								
				visual.setValue(metricValue);
				
			} catch (ValueOutOfBoundsException e) {				
				System.err.println("Value of: "+ entry.getKey() +" out of bounds with "+entry.getValue()+".");				
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
		if (currentCollectionForm == Vobject.COLLECTION_CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (currentCollectionForm == Vobject.COLLECTION_CIRCLE) {
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
		Float[] shift = new Float[3];
		shift[0] =  ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] = 0.0f;
		shift[2] = -((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f;
		setRelativeLocation(shift);
				
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
				vsite.setSeparation(vsite.getRadius()+separation);
				
				shift[0] = -(scaleXZ+separation)*row;
				shift[1] = 0.0f;
				shift[2] =  (scaleXZ+separation)*column;
				vsite.setRelativeLocation(shift);
					
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

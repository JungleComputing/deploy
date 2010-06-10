package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Pool;
import ibis.deploy.gui.performance.dataholders.Site;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.swing.SetCollectionFormAction;
import ibis.deploy.gui.performance.swing.SetMetricFormAction;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;

public class Vpool extends Vobject implements VobjectInterface {	
	private List<Vsite> vsites;	
	private Pool pool; 
		
	public Vpool(PerfVis perfvis, VisualManager visman, Vobject parent, Pool pool) {
		super(perfvis, visman);
		this.pool = pool;
		this.currentCollectionForm = Vobject.COLLECTION_CITYSCAPE;
		
		//Register the new object with the Performance visualization object
		this.glName = visman.registerPool(this);
				
		Site[] sites = pool.getSubConcepts();
		vsites = new ArrayList<Vsite>();
				
		for (Site site : sites) {
			vsites.add(new Vsite(perfvis, visman, this, site));			
		}
				
		initializeMetrics();
		
		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			shownMetrics.add(entry.getKey());
		}
	}
	
	private void initializeMetrics() {
		vmetrics.clear();		
		
		HashMap<String, Float[]> colors = pool.getMetricsColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, this, entry.getValue()));			
		}		
	}	
	
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
		//IntBuffer oldMode = IntBuffer.allocate(1);
		//gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		//gl.glPushMatrix();
		//gl.glMatrixMode(GL.GL_MODELVIEW);

		//Move towards the intended location
		//gl.glTranslatef(location[0], location[1], location[2]);
		
		if (!showAverages) {
			//Draw the desired form
			if (currentCollectionForm == Vobject.COLLECTION_CITYSCAPE) {
				drawCityscape(gl, glMode);
			} else if (currentCollectionForm == Vobject.COLLECTION_CIRCLE) {
				drawCircle(gl, glMode);
			}
		} else {
			if (currentCollectionForm == Vobject.COLLECTION_CITYSCAPE) {
				drawAveragesCityscape(gl, glMode);
			} else if (currentCollectionForm == Vobject.COLLECTION_CIRCLE) {
				drawAveragesCircle(gl, glMode);
			}
		}
		
		//Restore the old matrix mode and transformation matrix		
		//gl.glMatrixMode(oldMode.get());
		//gl.glPopMatrix();
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
				
		radius = 0;
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
				float siteRadius = vsite.getRadius()+separation;
				radius += siteRadius;
				vsite.setSeparation(siteRadius);
				
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
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == Vobject.METRICS_BAR || newForm == Vobject.METRICS_TUBE || newForm == Vobject.METRICS_SPHERE) {
			currentMetricForm = newForm;			
		} else if (newForm == Vobject.COLLECTION_CITYSCAPE || newForm == Vobject.COLLECTION_CIRCLE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
		for (Vsite vsite : vsites) {
			vsite.setForm(newForm);
		}
	}
	
	public PopupMenu getMenu() {		
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Circle"};
		
		PopupMenu newMenu = new PopupMenu();	
		
		Menu metricsForms 	= makeRadioGroup("Metric Form", elementsgroup);
		Menu nodeForms 		= makeRadioGroup("Group Form", collectionsgroup);
		
		newMenu.add(metricsForms);
		newMenu.add(nodeForms);
		newMenu.add(getMetricsMenu("Metrics Toggle"));
		newMenu.add(getAveragesMenu("Compound Pool"));
		
		return newMenu;		
	}	
	
	protected Menu makeRadioGroup(String menuName, String[] itemNames) {
		Menu result = new Menu(menuName);
		
		for (String item : itemNames) {
			MenuItem newMenuItem = new MenuItem(item);
			if (menuName.equals("Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this, item));
			} else if (menuName.equals("Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this, item));
			}
			result.add(newMenuItem);			
		}
				
		return result;
	}
	
	public void toggleMetricShown(String key) throws StatNotRequestedException {
		if (vmetrics.containsKey(key)) {
			if (!shownMetrics.contains(key)) {			
				shownMetrics.add(key);
			} else {			
				shownMetrics.remove(key);
			}
		} else {
			throw new StatNotRequestedException();
		}
		
		for (Vsite vsite : vsites) {
			vsite.toggleMetricShown(key);
		}
	}
}

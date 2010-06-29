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
import ibis.deploy.gui.performance.swing.ToggleAveragesAction;
import ibis.deploy.gui.performance.swing.ToggleMetricAction;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class Vpool implements VisualElementInterface {	
	PerfVis perfvis;
	VisualManager visman;
	
	protected Float[] location;
	protected float radius;
	protected int currentMetricForm;
	protected int currentCollectionForm;
	protected boolean showAverages;
	
	protected int glName;
	protected float scaleXZ;
	protected float scaleY;	
	
	GLU glu;
	protected float separation;
	
	protected HashMap<String, Vmetric> vmetrics;
	protected Set<String> shownMetrics;
	protected VisualElementInterface parent;
	
	private List<Vsite> vsites;	
	private Pool pool; 
		
	public Vpool(PerfVis perfvis, VisualManager visman, VisualElementInterface parent, Pool pool) {
		this.perfvis = perfvis;
		this.visman = visman;
		
		glu = new GLU();
		this.showAverages = false;
		shownMetrics = new HashSet<String>();
		
		this.location = new Float[3];
		this.location[0] = 0.0f;
		this.location[1] = 0.0f;
		this.location[2] = 0.0f;
		
		this.separation = 0.0f;
		
		scaleXZ = 0.25f;
		scaleY = 1.0f;
				
		this.vmetrics 	= new HashMap<String, Vmetric>();
		
		this.pool = pool;
		this.currentCollectionForm = VisualElementInterface.COLLECTION_CITYSCAPE;
		
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
		if (!showAverages) {
			//Draw the desired form
			if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
				drawCityscape(gl, glMode);
			} else if (currentCollectionForm == VisualElementInterface.COLLECTION_CIRCLE) {
				drawCircle(gl, glMode);
			} else if (currentCollectionForm == VisualElementInterface.COLLECTION_SPHERE) {
				drawSphere(gl, glMode);
			}
		} else {			
			if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
				drawAveragesCityscape(gl, glMode);
			} else if (currentCollectionForm == VisualElementInterface.COLLECTION_CIRCLE) {
				drawAveragesCircle(gl, glMode);
			}
		}
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(vsites.size()));
		int columns 	= (int)Math.floor(Math.sqrt(vsites.size()));
		separation = vsites.get(0).getRadius()*1.5f;
		float xzShift = scaleXZ+separation;
		
		//Center the drawing around the location	
		Float[] shift = new Float[3];		
		shift[0] =  location[0] +(((xzShift*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] =  location[1];
		shift[2] =  location[2] -(((xzShift*columns)-separation)-(0.5f*scaleXZ))*0.5f;		
		setLocation(shift);
				
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
				Float[] newLocation = new Float[3];
				newLocation[0] = location[0] - xzShift*row;
				newLocation[1] = location[1];
				newLocation[2] = location[2] + xzShift*column;				
				vsite.setLocation(newLocation);
				
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
		float radius = (float) ((vsites.get(0).getRadius()/2) / Math.tan(degs/2));		
		
		int i = 0;
		for (VisualElementInterface vitem : vsites) {
			float xCoord = (float) (Math.acos(degs*i) / radius);
			float zCoord = (float) (Math.asin(degs*i) / radius);
			
			Float[] newLocation = new Float[3];
			newLocation[0] = location[0] + xCoord;
			newLocation[1] = location[1];
			newLocation[2] = location[2] + zCoord;
			vitem.setLocation(newLocation);
			
			//Draw the form
			vitem.drawThis(gl, glMode);
			
			i++;
		}
	}
	
	protected void drawSphere(GL gl, int glMode) {	
		//http://www.cgafaq.info/wiki/Evenly_distributed_points_on_sphere
		
		double dlong = Math.PI*(3-Math.sqrt(5));
		double olong = 0.0;
		double dz    = 2.0/vsites.size();
		double z     = 1 - (dz/2);
		Float[][] pt = new Float[vsites.size()][3]; 
		double r = 0;
		
		for (int k=0;k<vsites.size();k++) {
			r = Math.sqrt(1-(z*z));
			pt[k][0] = location[0] + 4*((float) (Math.cos(olong)*r));
			pt[k][1] = location[1] + 4*((float) (Math.sin(olong)*r));
			pt[k][2] = location[2] + 4*((float) z);
			z = z -dz;
			olong = olong +dlong;			
		}	
		
		int k=0;				
		for (Vsite vsite : vsites) {						
			//set the location						
			vsite.setLocation(pt[k]);
														
			//Draw the form at that location
			vsite.drawThis(gl, glMode);
						
			k++;
		}
	}
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == VisualElementInterface.METRICS_BAR || newForm == VisualElementInterface.METRICS_TUBE || newForm == VisualElementInterface.METRICS_SPHERE) {
			currentMetricForm = newForm;	
			for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
				entry.getValue().setForm(newForm);
			}
		} else if (newForm == VisualElementInterface.COLLECTION_CITYSCAPE || newForm == VisualElementInterface.COLLECTION_CIRCLE || newForm == VisualElementInterface.COLLECTION_SPHERE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
	}
	
	public PopupMenu getMenu() {		
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Circle", "Sphere"};
		
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
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;		
	}
	
	public void setLocation(Float[] newLocation) {
		this.location[0] = newLocation[0];
		this.location[1] = newLocation[1];
		this.location[2] = newLocation[2];
	}
	
	public void setRelativeLocation(Float[] locationShift) {
		location[0] += locationShift[0];
		location[1] += locationShift[1];
		location[2] += locationShift[2];
	}
	
	public void setSeparation(float newSeparation) {
		separation = newSeparation;		
	}
		
	public Float[] getLocation() {
		return location;
	}	

	public float getRadius() {
		float radius = 0.0f, maxRadiusChildren = 0.0f;
		for (Vsite vsite : vsites) {
			maxRadiusChildren = Math.max(vsite.getRadius(),maxRadiusChildren);
		}
		
		if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
			radius = (float) Math.max((Math.ceil(Math.sqrt(vsites.size()))*(maxRadiusChildren)), maxRadiusChildren);
		} else if (currentCollectionForm == VisualElementInterface.COLLECTION_CIRCLE) {
			
		} else if (currentCollectionForm == VisualElementInterface.COLLECTION_SPHERE) {
			radius = (float) Math.max((Math.ceil(2*(vsites.size()))*(maxRadiusChildren)), maxRadiusChildren);
		}
		
		return radius;
	}
	
	public int getGLName() {
		return glName;
	}
	
	public VisualElementInterface getParent() {		
		return parent;
	}
	
	public Menu getMetricsMenu(String label) {
		Menu result = new Menu(label);
		
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			MenuItem newMenuItem = new MenuItem(entry.getKey());
			newMenuItem.addActionListener(new ToggleMetricAction(this, entry.getKey()));
			result.add(newMenuItem);
		}
		
		return result;
	}
	
	public Menu getAveragesMenu(String label) {
		Menu result = new Menu(label);
		MenuItem newMenuItem;
		
		if (!showAverages) {
			newMenuItem = new MenuItem("Show Averages");
		} else {
			newMenuItem = new MenuItem("Show Sublevel");
		}
		
		newMenuItem.addActionListener(new ToggleAveragesAction(this, newMenuItem.getLabel()));
		result.add(newMenuItem);
		
		return result; 
	}	
	
	public void toggleAverages() {
		this.showAverages = !showAverages;
	}
	
	public void drawAveragesCityscape(GL gl, int glMode) {	
		///get the breakoff point for rows and columns
		int rows 		= 3;
		int columns 	= (shownMetrics.size()/3); //always come in groups of 3
		
		float xzShift = scaleXZ+separation;
		
		//Center the drawing around the location	
		Float[] shift = new Float[3];		
		shift[0] =  location[0] +(((xzShift*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] =  location[1];
		shift[2] =  location[2] -(((xzShift*columns)-separation)-(0.5f*scaleXZ))*0.5f;		
		setLocation(shift);
		
		int row = 0, column = 0, i = 0;
		Map<String, Vmetric> sortedMap = new TreeMap<String, Vmetric>(vmetrics);
		
		for (Entry<String, Vmetric> entry : sortedMap.entrySet()) {
			if (shownMetrics.contains(entry.getKey())) {
				row = i % rows;
				//Move to next row (if applicable)
				if (i != 0 && row == 0) {
					column++;						
				}
							
				//Setup the form
				try {
					Float[] newLocation = new Float[3];
					newLocation[0] = location[0] - xzShift*row;
					newLocation[1] = location[1];
					newLocation[2] = location[2] + xzShift*column;				
					entry.getValue().setLocation(newLocation);
						
				} catch (Exception e) {					
					e.printStackTrace();
				}
				
				//Draw the form
				entry.getValue().drawThis(gl, glMode);
				i++;
			}
		}
	}
	
	public void drawAveragesCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / shownMetrics.size();
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(angle/2));	
		radius = Math.max(radius, 0);
				
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {	
			if (shownMetrics.contains(entry.getKey())) {
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
}

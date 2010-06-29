package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Node;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class Vnode implements VisualElementInterface {
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
	
	private Node node;
	
	public Vnode(PerfVis perfvis, VisualManager visman, VisualElementInterface parent, Node node) {
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
		
		this.parent = parent;
		
		this.node = node;
		this.currentCollectionForm = VisualElementInterface.COLLECTION_CITYSCAPE;

		//Register the new object with the Performance visualization object
		this.glName = visman.registerNode(this);
		
		initializeMetrics();
		
		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			shownMetrics.add(entry.getKey());
		}
	}
	
	private void initializeMetrics() {
		vmetrics.clear();
		
		HashMap<String, Float[]> colors = node.getMetricsColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, this, entry.getValue()));
		}		
	}
	
	public void update() {		
		HashMap<String, Float> stats = node.getMonitoredNodeMetrics();
		for (Map.Entry<String, Float> entry : stats.entrySet()) {
			try {
				String metricName = entry.getKey();
				Float metricValue = entry.getValue();
				Vmetric visual = vmetrics.get(metricName);
								
				visual.setValue(metricValue);
				
			} catch (ValueOutOfBoundsException e) {	
				System.out.println("VALUE: "+entry.getValue()+" OUT OF BOUNDS!");
			}
		}		
	}
	
	public void drawThis(GL gl, int glMode) {
		//Draw the desired form
		if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
			drawCityscape(gl, glMode);
		} else if (currentCollectionForm == VisualElementInterface.COLLECTION_CIRCLE) {
			drawCircle(gl, glMode);
		}		
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(shownMetrics.size()));
		int columns 	= (int)Math.floor(Math.sqrt(shownMetrics.size()));
		float xzShift = scaleXZ+separation;
		
		//Center the drawing around the location	
		Float[] shift = new Float[3];
		shift[0] = location[0] +  (((xzShift*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] = location[1];
		shift[2] = location[2] + -(((xzShift*columns)-separation)-(0.5f*scaleXZ))*0.5f;
		setLocation(shift);
		
		int row = 0, column = 0, i = 0;
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {
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
	
	protected void drawCircle(GL gl, int glMode) {				
		double angle  = 2*Math.PI / vmetrics.size();
		float degs = (float) Math.toDegrees(angle);
		float radius = (float) ((scaleXZ/2) / Math.tan(degs/2));		
		
		int i = 0;
		for (VisualElementInterface vitem : vmetrics.values()) {
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
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == VisualElementInterface.METRICS_BAR || newForm == VisualElementInterface.METRICS_TUBE || newForm == VisualElementInterface.METRICS_SPHERE) {
			currentMetricForm = newForm;	
			for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
				entry.getValue().setForm(newForm);
			}
		} else if (newForm == VisualElementInterface.COLLECTION_CITYSCAPE || newForm == VisualElementInterface.COLLECTION_CIRCLE) {
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
		Menu siteForms 		= makeRadioGroup("Site Group Form", collectionsgroup);
		Menu siteMetricForms= makeRadioGroup("Site Metric Form", elementsgroup);
		Menu poolForms 		= makeRadioGroup("Pool Group Form", collectionsgroup);
		Menu poolMetricForms= makeRadioGroup("Pool Metric Form", elementsgroup);
		
		newMenu.add(metricsForms);
		newMenu.add(nodeForms);
		newMenu.add(siteForms);
		newMenu.add(siteMetricForms);
		newMenu.add(poolForms);
		newMenu.add(poolMetricForms);
		newMenu.add(getMetricsMenu("Metrics Toggle"));
		newMenu.add(getAveragesMenu("Compound Node"));
		newMenu.add(parent.getAveragesMenu("Compound Site"));
		newMenu.add(parent.getParent().getAveragesMenu("Compound Pool"));
		
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
			} else if (menuName.equals("Site Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent(), item));
			} else if (menuName.equals("Site Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this.getParent(), item));
			} else if (menuName.equals("Pool Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent().getParent(), item));
			} else if (menuName.equals("Pool Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this.getParent().getParent(), item));
			}
			result.add(newMenuItem);			
		}
				
		return result;
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
	
	public void setSeparation(float newSeparation) {
		separation = newSeparation;		
	}
			
	public Float[] getLocation() {
		return location;
	}	

	public float getRadius() {
		float radius = 0.0f;
		if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
			radius = (float) Math.max((Math.ceil(Math.sqrt(vmetrics.size()))*(scaleXZ)), scaleY);
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
	}
	
	public void toggleAverages() {
		this.showAverages = !showAverages;
	}
}

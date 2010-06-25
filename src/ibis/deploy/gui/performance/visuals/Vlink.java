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
import ibis.ipl.IbisIdentifier;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class Vlink implements VisualElementInterface {
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
	private VisualElementInterface from, to;	
	private IbisIdentifier source, destination;
	
	public Vlink(PerfVis perfvis, VisualManager visman, VisualElementInterface parent, Node node, IbisIdentifier source, VisualElementInterface from, IbisIdentifier destination, VisualElementInterface to) {
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
		
		scaleXZ = 0.1f;
		scaleY = 1.0f;
				
		this.vmetrics 	= new HashMap<String, Vmetric>();
		
		this.parent = parent;
		this.source = source;		
		this.from = from;
		this.destination = destination;
		this.to = to;
		this.node = node;
		
		this.currentCollectionForm = VisualElementInterface.COLLECTION_CITYSCAPE;		

		//Register the new object with the Performance visualization object
		this.glName = visman.registerLink(this);
				
		initializeMetrics();

		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			shownMetrics.add(entry.getKey());
		}
	}	
		
	private void initializeMetrics() {
		vmetrics.clear();
		
		HashMap<String, Float[]> colors = node.getLinkColors();
						
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, this, entry.getValue(), from, to));
		}		
	}
	
	public void update() {
		Map<String, Float> stats = node.getLinkValueMap(destination);		
		if (stats != null) {			
			for (Entry<String, Float> entry : stats.entrySet()) {
				try {
					String metricName = entry.getKey();
					Float metricValue = entry.getValue();
					Vmetric visual = vmetrics.get(metricName);
								
					visual.setValue(metricValue);
				
				} catch (ValueOutOfBoundsException e) {				
					System.out.println(source +" to "+destination+" VALUE of "+entry.getKey()+": "+entry.getValue()+" OUT OF BOUNDS!");
				}
			}
		}
	}
	
	public void drawThis(GL gl, int glMode) {	
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);	
		
		//Calculate the angles we need to turn towards the destination
		Float[] origin = from.getLocation();
		Float[] destination = to.getLocation();
		int xSign = 1, ySign = 1, zSign = 1;
		
		float xDist = destination[0] - origin[0];
		if (xDist<0) xSign = -1;
		xDist = Math.abs(xDist);
		
		float yDist = destination[1] - origin[1];
		if (yDist<0) ySign = -1;
		yDist = Math.abs(yDist);
		
		float zDist = destination[2] - origin[2];
		if (zDist<0) zSign = -1;
		zDist = Math.abs(zDist);
		
		//Calculate the length of this element : V( x^2 + y^2 + z^2 ) 
		float length  = (float) Math.sqrt(	Math.pow(xDist,2)
										  + Math.pow(yDist,2) 
										  + Math.pow(zDist,2));
		
		float xzDist =  (float) Math.sqrt(	Math.pow(xDist,2)
				  						  + Math.pow(zDist,2));
					
		float yAngle = 0.0f;
		if (xSign < 0) {
			yAngle = 180.0f + (zSign * (float) Math.toDegrees(Math.atan(zDist/xDist)));
		} else {
			yAngle = (-zSign * (float) Math.toDegrees(Math.atan(zDist/xDist)));
		}
		
		float zAngle = ySign * (float) Math.toDegrees(Math.atan(yDist/xzDist));
					
		//Translate to the origin coordinates
		gl.glTranslatef(origin[0], origin[1], origin[2]);						
					
		//Rotate towards the destination, the x axis is now pointed towards the target
		gl.glRotatef(yAngle, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(zAngle, 0.0f, 0.0f, 1.0f);				
					
		//point with the y axis instead of the x axis
		gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);		
					
		//Translate the origin's radius over the y axis
		gl.glTranslatef(0.0f, from.getRadius(), 0.0f);
		
		//reduce the length by the radii of the origin and destination objects
		length = length - (from.getRadius() + to.getRadius());
		
		//Draw the desired form
		if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
			drawCityscape(gl, glMode, length);
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode, float length) {		
		Float[] shift = new Float[3];
				
		int i =0;
		for (Entry<String, Vmetric> entry : vmetrics.entrySet()) {			
			if (shownMetrics.contains(entry.getKey())) {							
				//Setup the form
				try {					
					shift[0] = -scaleXZ*i;
					shift[1] = 0.0f;
					shift[2] =  ((0.5f*scaleXZ)+separation);
					entry.getValue().setLocation(shift);
					
					entry.getValue().setSize(scaleXZ, length);
				} catch (Exception e) {					
					e.printStackTrace();
				}
				
				//Draw the form
				entry.getValue().drawThis(gl, glMode);
				i++;
			}
		}
	}
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == VisualElementInterface.METRICS_BAR || newForm == VisualElementInterface.METRICS_TUBE || newForm == VisualElementInterface.METRICS_SPHERE) {
			currentMetricForm = newForm;			
		} else if (newForm == VisualElementInterface.COLLECTION_CITYSCAPE || newForm == VisualElementInterface.COLLECTION_CIRCLE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			entry.getValue().setForm(newForm);
		}
	}
	
	public PopupMenu getMenu() {		
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Circle"};
		
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
		newMenu.add(getAveragesMenu("Compound Link"));
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
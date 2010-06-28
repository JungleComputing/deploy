package ibis.deploy.gui.performance.visuals;
import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Node;
import ibis.deploy.gui.performance.dataholders.Site;
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

public class Vsite implements VisualElementInterface {
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
	
	private List<Vnode> vnodes;
	private HashMap<Node, Vnode> nodesToVisuals;
	private HashMap<IbisIdentifier, HashMap<IbisIdentifier, Vlink>> vlinkMap;
	private Set<Vlink> vlinks;
	
	private Site site;
		
	public Vsite(PerfVis perfvis, VisualManager visman, VisualElementInterface parent, Site site) {
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
		
		this.site = site;
		this.currentCollectionForm = VisualElementInterface.COLLECTION_SPHERE;
		
		//Register the new object with the Performance visualization object
		this.glName = visman.registerSite(this);
		
		//Preparing the vnodes
		Node[] nodes = site.getSubConcepts();
		vnodes = new ArrayList<Vnode>();
		vlinkMap = new HashMap<IbisIdentifier, HashMap<IbisIdentifier, Vlink>>();
		vlinks = new HashSet<Vlink>();
		nodesToVisuals = new HashMap<Node, Vnode>();
				
		for (Node node : nodes) {
			Vnode newVnode = new Vnode(perfvis, visman, this, node);
			vnodes.add(newVnode);
			nodesToVisuals.put(node, newVnode);
		}		
		
		initializeMetrics();	
		
		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			shownMetrics.add(entry.getKey());
		}
	}
	
	private void initializeMetrics() {
		vmetrics.clear();		
		
		HashMap<String, Float[]> colors = site.getMetricsColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			vmetrics.put(entry.getKey(), new Vmetric(perfvis, visman, this, entry.getValue()));			
		}
	}
		
	private void createLinks() {	
		vlinks.clear();
		
		for (Map.Entry<Node, Vnode> entry : nodesToVisuals.entrySet()) {
			Node node = entry.getKey();
			Vnode from = entry.getValue();
			
			IbisIdentifier source = node.getName();
						
			for (IbisIdentifier destination : node.getConnections()) {
				Vnode to = nodesToVisuals.get(site.getNode(destination));
				
				if (to != null) {
				//only show links within this site
				//if (nodesToVisuals.containsKey(destination)) {
					if (vlinkMap.containsKey(source)) {
						if (!vlinkMap.get(source).containsKey(destination)) {
							vlinkMap.get(source).put(destination, new Vlink(perfvis, visman, this, node, source, from, destination, to));
						}
					} else {
						HashMap<IbisIdentifier, Vlink> newMap = new HashMap<IbisIdentifier, Vlink>();
						newMap.put(destination, new Vlink(perfvis, visman, this, node, source, from, destination, to));
						vlinkMap.put(source, newMap);
					}	
				//}
				}
			}
		}
		
		for (Entry<IbisIdentifier, HashMap<IbisIdentifier, Vlink>> entry : vlinkMap.entrySet()) {
			for (Entry<IbisIdentifier, Vlink> entry2 : entry.getValue().entrySet()) {
				vlinks.add(entry2.getValue());
			}
		}		
	}
	
	public void update() {
		for (Vnode vnode : vnodes) {
			vnode.update();			
		}
		
		createLinks();
		
		for (Vlink vlink : vlinks) {
			vlink.update();
		}
		
		HashMap<String, Float> stats = site.getMonitoredNodeMetrics();
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
			drawLinks(gl, glMode);
		} else {
			if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
				drawAveragesCityscape(gl, glMode);
			} else if (currentCollectionForm == VisualElementInterface.COLLECTION_CIRCLE) {
				drawAveragesCircle(gl, glMode);
			}
		}		
	}
	
	protected void drawLinks(GL gl, int glMode) {
		for (Vlink vlink : vlinks) {			
			vlink.drawThis(gl, glMode);
		}		
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(vnodes.size()));
		int columns 	= (int)Math.floor(Math.sqrt(vnodes.size()));
		
		//Center the drawing around the location	
		Float[] shift = new Float[3];
		shift[0] =  ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] = 0.0f;
		shift[2] = -((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f;
		setRelativeLocation(shift);
		
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
				vnode.setSeparation(0.0f);		
				
				shift[0] = -(scaleXZ+separation)*row;
				shift[1] = 0.0f;
				shift[2] =  (scaleXZ+separation)*column;
				vnode.setRelativeLocation(shift);
					
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
	
	protected void drawSphere(GL gl, int glMode) {	
		//http://www.cgafaq.info/wiki/Evenly_distributed_points_on_sphere
		
		double dlong = Math.PI*(3-Math.sqrt(5));
		double olong = 0.0;
		double dz    = 2.0/vnodes.size();
		double z     = 1 - (dz/2);
		Float[][] pt = new Float[vnodes.size()][3]; 
		double r = 0;
		
		for (int k=0;k<vnodes.size();k++) {
			r = Math.sqrt(1-(z*z));
			pt[k][0] = location[0] + 4*((float) (Math.cos(olong)*r));
			pt[k][1] = location[1] + 4*((float) (Math.sin(olong)*r));
			pt[k][2] = location[2] + 4*((float) z);
			z = z -dz;
			olong = olong +dlong;			
		}	
		
		int k=0;				
		for (Vnode vnode : vnodes) {						
			//set the location						
			vnode.setLocation(pt[k]);
														
			//Draw the form at that location
			vnode.drawThis(gl, glMode);
						
			k++;
		}
	}
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == VisualElementInterface.METRICS_BAR || newForm == VisualElementInterface.METRICS_TUBE || newForm == VisualElementInterface.METRICS_SPHERE) {
			currentMetricForm = newForm;			
		} else if (newForm == VisualElementInterface.COLLECTION_CITYSCAPE || newForm == VisualElementInterface.COLLECTION_CIRCLE || newForm == VisualElementInterface.COLLECTION_SPHERE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
		for (Vnode vnode : vnodes) {
			vnode.setForm(newForm);
		}
	}
	
	public PopupMenu getMenu() {		
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Circle", "Sphere"};
		
		PopupMenu newMenu = new PopupMenu();	
		
		Menu metricsForms 	= makeRadioGroup("Metric Form", elementsgroup);
		Menu nodeForms 		= makeRadioGroup("Group Form", collectionsgroup);
		Menu poolForms 		= makeRadioGroup("Pool Group Form", collectionsgroup);
		Menu poolMetricForms= makeRadioGroup("Pool Metric Form", elementsgroup);
		
		newMenu.add(metricsForms);
		newMenu.add(nodeForms);
		newMenu.add(poolForms);
		newMenu.add(poolMetricForms);
		newMenu.add(getMetricsMenu("Metrics Toggle"));
		newMenu.add(getAveragesMenu("Compound Site"));
		newMenu.add(parent.getAveragesMenu("Compound Pool"));
		
		
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
			} else if (menuName.equals("Pool Group Form")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent(), item));
			} else if (menuName.equals("Pool Metric Form")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this.getParent(), item));
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
		
		for (Vnode vnode : vnodes) {
			vnode.toggleMetricShown(key);
		}
		
		for (Vlink vlink : vlinks) {
			vlink.toggleMetricShown(key);
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
		
	public void toggleAverages() {
		this.showAverages = !showAverages;
	}
	
	public void drawAveragesCityscape(GL gl, int glMode) {		
		///get the breakoff point for rows and columns
		int rows 		= 3;
		int columns 	= (shownMetrics.size()/3); //always come in groups of 3
		
		float tempSeparation = separation;
		separation = 0.25f;
				
		//Center the drawing around the location	
		Float[] shift = new Float[3];
		shift[0] =  ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] = 0.0f;
		shift[2] = -((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f;
		setRelativeLocation(shift);
		
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
		
		separation = tempSeparation;
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

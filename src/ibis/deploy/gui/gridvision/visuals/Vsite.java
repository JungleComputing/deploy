package ibis.deploy.gui.gridvision.visuals;
import ibis.deploy.gui.gridvision.GridVision;
import ibis.deploy.gui.gridvision.VisualManager;
import ibis.deploy.gui.gridvision.dataholders.Node;
import ibis.deploy.gui.gridvision.dataholders.Site;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.gridvision.swing.SetCollectionFormAction;
import ibis.deploy.gui.gridvision.swing.SetMetricFormAction;
import ibis.deploy.gui.gridvision.swing.ToggleAveragesAction;
import ibis.deploy.gui.gridvision.swing.ToggleMetricAction;
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
	GridVision perfvis;
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
	private List<IbisIdentifier> ids;
	private HashMap<Node, Vnode> nodesToVisuals;
	private HashMap<IbisIdentifier, HashMap<IbisIdentifier, Vlink>> vlinkMap;
	private HashMap<IbisIdentifier, IbisIdentifier> externalLinks;
	private Set<Vlink> vlinks;
	
	private Site site;
		
	public Vsite(GridVision perfvis, VisualManager visman, VisualElementInterface parent, Site site) {
		this.perfvis = perfvis;
		this.visman = visman;
		
		glu = new GLU();
		this.showAverages = false;
		shownMetrics = new HashSet<String>();
		
		this.location = new Float[3];
		this.location[0] = 0.0f;
		this.location[1] = 0.0f;
		this.location[2] = 0.0f;
		
		this.separation = 0.25f;
		
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
		ids = new ArrayList<IbisIdentifier>();
		vlinkMap = new HashMap<IbisIdentifier, HashMap<IbisIdentifier, Vlink>>();
		vlinks = new HashSet<Vlink>();
		externalLinks = new HashMap<IbisIdentifier, IbisIdentifier>();
		nodesToVisuals = new HashMap<Node, Vnode>();
				
		for (Node node : nodes) {
			Vnode newVnode = new Vnode(perfvis, visman, this, node);
			vnodes.add(newVnode);
			ids.add(node.getName());
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
		externalLinks.clear();
		
		for (Map.Entry<Node, Vnode> entry : nodesToVisuals.entrySet()) {
			Node node = entry.getKey();
			Vnode from = entry.getValue();
			
			IbisIdentifier source = node.getName();
						
			for (IbisIdentifier destination : node.getConnections()) {
				Vnode to = nodesToVisuals.get(site.getNode(destination));
				
				if (to != null) { //This link is site-internal
					if (vlinkMap.containsKey(source)) {
						if (!vlinkMap.get(source).containsKey(destination)) {
							vlinkMap.get(source).put(destination, new Vlink(perfvis, visman, this, node, source, from, destination, to));
						}
					} else {
						HashMap<IbisIdentifier, Vlink> newMap = new HashMap<IbisIdentifier, Vlink>();
						newMap.put(destination, new Vlink(perfvis, visman, this, node, source, from, destination, to));
						vlinkMap.put(source, newMap);
					}
				} else { //this link is site-external
					externalLinks.put(source, destination);
				}
			}
		}
		
		for (Entry<IbisIdentifier, HashMap<IbisIdentifier, Vlink>> entry : vlinkMap.entrySet()) {
			for (Entry<IbisIdentifier, Vlink> entry2 : entry.getValue().entrySet()) {
				vlinks.add(entry2.getValue());
			}
		}		
	}
	
	public VisualElementInterface getVisual(IbisIdentifier id) {
		if (ids.contains(id)) {
			return nodesToVisuals.get(site.getNode(id));
		} else {
			return null;
		}
	}
	
	public Node getNode(IbisIdentifier id) {
		if (ids.contains(id)) {
			return site.getNode(id);
		} else {
			return null;
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
		//drawBoundingBox(gl, glMode);
	}
	
	protected void drawBoundingBox(GL gl, int glMode) {
		//use nice variables, so that the ogl code is readable
		float o = 0.0f;			//(o)rigin
		float x = getRadius();	//(x) maximum coordinate
		float y = getRadius();	//(y) maximum coordinate
		float z = getRadius();	//(z) maximum coordinate		 
		float alpha = 0.2f;
							
		float quad_color_r = 0.3f;
		float quad_color_g = 0.0f;
		float quad_color_b = 0.3f;
		
		//Center the drawing startpoint
		gl.glTranslatef(location[0], location[1], location[2]);
		gl.glTranslatef(-0.5f*x, 0.0f, -0.5f*z);
		
		gl.glBegin(GL.GL_QUADS);		
			//TOP
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, y, o);			
			gl.glVertex3f( o, y, o);			
			gl.glVertex3f( o, y, z);			
			gl.glVertex3f( x, y, z);
			
			//BOTTOM left out
			
			//FRONT
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, y, z);			
			gl.glVertex3f( o, y, z);			
			gl.glVertex3f( o, o, z);			
			gl.glVertex3f( x, o, z);
			
			//BACK
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, o, o);			
			gl.glVertex3f( o, o, o);			
			gl.glVertex3f( o, y, o);			
			gl.glVertex3f( x, y, o);
			
			//LEFT
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( o, y, z);			
			gl.glVertex3f( o, y, o);			
			gl.glVertex3f( o, o, o);			
			gl.glVertex3f( o, o, z);
			
			//RIGHT
			gl.glColor4f(quad_color_r, quad_color_g, quad_color_b, alpha);			
			gl.glVertex3f( x, y, o);			
			gl.glVertex3f( x, y, z);			
			gl.glVertex3f( x, o, z);			
			gl.glVertex3f( x, o, o);
		gl.glEnd();		
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
		separation = vnodes.get(0).getRadius()*1.5f;
		float xzShift = scaleXZ+separation;
		
		//Center the drawing around the location	
		Float[] shift = new Float[3];		
		shift[0] =  location[0] +(((xzShift*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] =  location[1];
		shift[2] =  location[2] -(((xzShift*columns)-separation)-(0.5f*scaleXZ))*0.5f;		
		setLocation(shift);
		
		int row = 0, column = 0, i =0;
		for (Vnode vnode : vnodes) {
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
				vnode.setLocation(newLocation);
				
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
		float radius = (float) ((vnodes.get(0).getRadius()/2) / Math.tan(degs/2));
		
		int i = 0;
		for (VisualElementInterface vitem : vnodes) {
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
			for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
				entry.getValue().setForm(newForm);
			}
			for (Vnode vnode : vnodes) {
				vnode.setForm(newForm);
			}
		} else if (newForm == VisualElementInterface.COLLECTION_CITYSCAPE || newForm == VisualElementInterface.COLLECTION_CIRCLE || newForm == VisualElementInterface.COLLECTION_SPHERE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
	}
	
	public PopupMenu getMenu() {		
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Sphere"};
		
		PopupMenu newMenu = new PopupMenu();	
		
		Menu metricsForms 	= makeRadioGroup("Metric Forms", elementsgroup);
		Menu nodeForms 		= makeRadioGroup("Group Form", collectionsgroup);
		
		newMenu.add(metricsForms);
		newMenu.add(nodeForms);
		newMenu.add(getMetricsMenu("Metrics Toggle"));
		newMenu.add(getAveragesMenu("Compound"));		
		
		return newMenu;		
	}	
	
	public Menu getSubMenu() {
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Sphere"};
		Menu result = new Menu("Site");
		
		Menu elements = makeRadioGroup("Metric Forms", elementsgroup);
		result.add(elements);
		
		Menu collection = makeRadioGroup("Group Form", collectionsgroup);
		result.add(collection);
		
		result.add(getMetricsMenu("Metrics Toggle"));
		result.add(getAveragesMenu("Compound"));
		
		return result;
	}
	
	public Menu makeRadioGroup(String menuName, String[] itemNames) {
		Menu result = new Menu(menuName);
		
		for (String item : itemNames) {
			MenuItem newMenuItem = new MenuItem(item);
			if (menuName.equals("Metric Forms")) {
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
	
	public void setSeparation(float newSeparation) {
		separation = newSeparation;		
	}
		
	public Float[] getLocation() {
		return location;
	}	

	public float getRadius() {
		float radius = 0.0f, maxRadiusChildren = 0.0f;
		for (Vnode vnode : vnodes) {
			maxRadiusChildren = Math.max(vnode.getRadius(),maxRadiusChildren);
		}
		
		if (currentCollectionForm == VisualElementInterface.COLLECTION_CITYSCAPE) {
			radius = (float) Math.max((Math.ceil(Math.sqrt(vnodes.size()))*(maxRadiusChildren)), maxRadiusChildren);
		} else if (currentCollectionForm == VisualElementInterface.COLLECTION_CIRCLE) {
			
		} else if (currentCollectionForm == VisualElementInterface.COLLECTION_SPHERE) {
			radius = (float) 10*maxRadiusChildren;
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
	
	public HashMap<IbisIdentifier, IbisIdentifier> getExternalLinks() {
		return externalLinks;
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
				
		float xzShift = scaleXZ+0.1f;
		
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

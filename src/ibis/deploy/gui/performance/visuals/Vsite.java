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
import ibis.ipl.IbisIdentifier;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

public class Vsite extends Vobject implements VobjectInterface {
	private List<Vnode> vnodes;
	private HashMap<Node, Vnode> nodesToVisuals;
	private List<Vlink> vlinks;
	
	private Site site;
		
	public Vsite(PerfVis perfvis, VisualManager visman, Vobject parent, Site site) {
		super(perfvis, visman);
		this.parent = parent;
		
		this.site = site;
		this.currentCollectionForm = Vobject.COLLECTION_CITYSCAPE;
		
		//Register the new object with the Performance visualization object
		this.glName = visman.registerSite(this);
		
		//Preparing the vnodes
		Node[] nodes = site.getSubConcepts();
		vnodes = new ArrayList<Vnode>();
		vlinks = new ArrayList<Vlink>();
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
		for (Map.Entry<Node, Vnode> entry : nodesToVisuals.entrySet()) {
			Node node = entry.getKey();
			Vnode from = entry.getValue();
			
			for (IbisIdentifier ibis : node.getConnections()) {				
				Vnode to = nodesToVisuals.get(site.getNode(ibis));
				vlinks.add(new Vlink(perfvis, visman, this, node, from, to));						
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
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);		

		//Move towards the intended location
		gl.glTranslatef(location[0], location[1], location[2]);
		
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
		
		drawLinks(gl, glMode);
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawLinks(GL gl, int glMode) {
		for (Vlink vlink : vlinks) {
			vlink.setLocation(location);
			vlink.setSeparation(0.0f);
			
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
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == Vobject.METRICS_BAR || newForm == Vobject.METRICS_TUBE || newForm == Vobject.METRICS_SPHERE) {
			currentMetricForm = newForm;			
		} else if (newForm == Vobject.COLLECTION_CITYSCAPE || newForm == Vobject.COLLECTION_CIRCLE) {
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
		String[] collectionsgroup = {"Cityscape", "Circle"};
		
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
}

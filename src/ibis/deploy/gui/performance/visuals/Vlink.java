package ibis.deploy.gui.performance.visuals;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.dataholders.Node;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.swing.SetCollectionFormAction;
import ibis.deploy.gui.performance.swing.SetMetricFormAction;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;

public class Vlink extends Vobject implements VobjectInterface {		
	private Node node;
	private Vobject from;
	private Vobject to;	
	
	public Vlink(PerfVis perfvis, VisualManager visman, Vobject parent, Node node, Vobject from, Vobject to) {
		super(perfvis, visman);
		this.parent = parent;
		this.from = from;
		this.to = to;
		
		this.node = node;
		this.currentCollectionForm = Vobject.COLLECTION_CITYSCAPE;

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
		Map<String, Float> stats = node.getLinkValueMap(node.getName());
		if (stats != null) {
			for (Entry<String, Float> entry : stats.entrySet()) {
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
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();
	}
	
	protected void drawCityscape(GL gl, int glMode) {		
		//get the breakoff point for rows and columns
		int rows 		= (int)Math.ceil(Math.sqrt(shownMetrics.size()));
		int columns 	= (int)Math.floor(Math.sqrt(shownMetrics.size()));
		
		//Center the drawing around the location		
		Float[] shift = new Float[3];
		shift[0] =  ((((scaleXZ+separation)*rows   )-separation)-(0.5f*scaleXZ))*0.5f;
		shift[1] = 0.0f;
		shift[2] = -((((scaleXZ+separation)*columns)-separation)-(0.5f*scaleXZ))*0.5f;
		setRelativeLocation(shift);
		
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
	}
	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == Vobject.METRICS_BAR || newForm == Vobject.METRICS_TUBE || newForm == Vobject.METRICS_SPHERE) {
			currentMetricForm = newForm;			
		} else if (newForm == Vobject.COLLECTION_CITYSCAPE || newForm == Vobject.COLLECTION_CIRCLE) {
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
}
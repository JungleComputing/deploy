package ibis.deploy.gui.gridvision.visuals;

import ibis.deploy.gui.gridvision.GridVision;
import ibis.deploy.gui.gridvision.VisualManager;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.interfaces.IbisConcept;

import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public abstract class VisualElement implements ibis.deploy.gui.gridvision.interfaces.VisualElement {
	private GridVision gv;
	private VisualManager visman;
	private ibis.deploy.gui.gridvision.interfaces.VisualElement parent;
	private ArrayList<ibis.deploy.gui.gridvision.interfaces.VisualElement> children;
	private IbisConcept dataHolder;
	
	private GLU glu;
	private GL gl;

	private int currentMetricForm;
	private int currentCollectionForm;

	private float radius;

	private boolean showAverages;
	
	private int glName;
	
	private float scale;
	
	private HashMap<String, Vmetric> vmetrics;
	private ArrayList<String> shownMetrics;
	
	public VisualElement(GridVision gv, ibis.deploy.gui.gridvision.interfaces.VisualElement parent, IbisConcept dataHolder) {
		this.gv = gv;
		this.visman = gv.getVisualManager();		
		this.dataHolder = dataHolder;
		this.parent = parent;
		this.children = new ArrayList<ibis.deploy.gui.gridvision.interfaces.VisualElement>();
		
		this.glu = gv.getGLU();
		this.gl = gv.getGL();
		
		this.currentCollectionForm = COLLECTION_CITYSCAPE;
		this.currentMetricForm = METRICS_BAR;
		
		this.radius = 0.0f;
		
		this.showAverages = false;
		
		this.glName = visman.registerElement();
		this.scale = 1.0f;
		
		this.vmetrics = new HashMap<String, Vmetric>();
		this.shownMetrics = new ArrayList<String>();
		
		//get the currently shown metrics from the VisualManager
		visman.getShownMetrics();
		
		//Fill the vmetrics map with the gathered and shown metrics for the first time
		initializeMetrics();
	}
	
	public void update() {
		for (Map.Entry<String, Vmetric> entry : vmetrics.entrySet()) {
			try {
				dataHolder.getNodeMetricsValue(entry.getKey(), IbisConcept.AVG);
			} catch (StatNotRequestedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModeUnknownException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void drawThis(Float[] location, int glMode) {
		
	}
	
	public void setForm(int newForm) throws ModeUnknownException {
		// TODO Auto-generated method stub		
	}
	
	public PopupMenu getMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	public void toggleMetricShown(String key) throws StatNotRequestedException {
		// TODO Auto-generated method stub
		
	}
	
	private void initializeMetrics() {
		vmetrics.clear();
		
		HashMap<String, Float[]> colors = dataHolder.getNodeMetricColors();
		
		for (Map.Entry<String, Float[]> entry : colors.entrySet()) {
			if (shownMetrics.contains(entry.getKey())) {
				vmetrics.put(entry.getKey(), new Vmetric(gv, visman, this, entry.getValue()));
			}
		}
	}
}

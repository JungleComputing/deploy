package ibis.deploy.gui.performance.visuals;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.util.HashMap;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.VisualManager;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.swing.SetCollectionFormAction;
import ibis.deploy.gui.performance.swing.SetMetricFormAction;

import javax.media.opengl.glu.GLU;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

public class Vobject {		
	public static Float[] CPU_HIGH_COLOR	= {1.0f, 0.0f, 0.0f};
	public static Float[] CPU_AVG_COLOR		= {1.0f, 0.5f, 0.0f};
	public static Float[] CPU_LOW_COLOR		= {1.0f, 1.0f, 0.0f};
	
	public static Float[] MEM_HIGH_COLOR	= {0.5f, 1.0f, 0.0f};
	public static Float[] MEM_AVG_COLOR		= {0.0f, 1.0f, 0.0f};
	public static Float[] MEM_LOW_COLOR		= {0.0f, 1.0f, 0.5f};	
	
	public static Float[] _0FF	= {0.0f, 1.0f, 1.0f};
	public static Float[] _08F	= {0.0f, 0.5f, 1.0f};
	public static Float[] _00F	= {0.0f, 0.0f, 1.0f};
	
	public static Float[] NETWORK_LINK_COLOR= {0.5f, 0.0f, 1.0f};
	public static Float[] _F0F	= {1.0f, 0.0f, 1.0f};
	public static Float[] _F08	= {1.0f, 0.0f, 0.5f};
	
	public static final int METRICS_BAR = 123;
	public static final int METRICS_TUBE = 124;
	public static final int METRICS_SPHERE = 125;
	
	public static final int COLLECTION_CITYSCAPE = 345;
	public static final int COLLECTION_CIRCLE = 346;
	
	PerfVis perfvis;
	VisualManager visman;
	
	protected Float[] location;
	protected float radius;
	protected int currentMetricForm;
	protected int currentCollectionForm;
	
	protected int glName;
	protected float scaleXZ;
	protected float scaleY;	
	
	GLU glu;
	protected float separation;
	
	protected HashMap<String, Vmetric> vmetrics;
	protected Vobject parent;
		
	public Vobject(PerfVis perfvis, VisualManager visman) {
		glu = new GLU();
		this.perfvis = perfvis;
		this.visman = visman;
		
		this.location = new Float[3];
		this.location[0] = 0.0f;
		this.location[1] = 0.0f;
		this.location[2] = 0.0f;
		
		this.separation = 0.0f;
		
		//set the size and radius to default
		//setSize(1.0f, 0.25f);
		scaleXZ = 0.25f;
		scaleY = 1.0f;
				
		this.vmetrics 	= new HashMap<String, Vmetric>();
	}
	
	public void setSize(float width, float height) {
		this.scaleXZ = width;
		this.scaleY = height;
		
		//3d across
		this.radius = (float) Math.sqrt(  Math.pow(width, 2)
			 							+ Math.pow(height, 2)
			 							+ Math.pow(width, 2));
	}
		
	public Float[] getLocation() {
		return location;
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
	
	public void setRadius() {
		radius = Math.max(vmetrics.size()*(scaleXZ), scaleY);
	}

	public float getRadius() {		
		return radius;
	}
	
	public int getGLName() {
		return glName;
	}
	

	
	public void setForm(int newForm) throws ModeUnknownException {
		if (newForm == Vobject.METRICS_BAR || newForm == Vobject.METRICS_TUBE || newForm == Vobject.METRICS_SPHERE) {
			currentMetricForm = newForm;
		} else if (newForm == Vobject.COLLECTION_CITYSCAPE || newForm == Vobject.COLLECTION_CIRCLE) {
			currentCollectionForm = newForm;
		} else {
			throw new ModeUnknownException();
		}
	}	
	
	public PopupMenu getMenu() {
		
		String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
		String[] collectionsgroup = {"Cityscape", "Circle"};
		
		PopupMenu newMenu = new PopupMenu();
		//newMenu.setLightWeightPopupEnabled(false);
		//PopupMenu.setDefaultLightWeightPopupEnabled(false);		
		
		Menu metricsForms 		= makeRadioGroup("Metrics", elementsgroup);
		Menu collectionForms 	= makeRadioGroup("Collection", collectionsgroup);
		newMenu.add(metricsForms);
		newMenu.add(collectionForms);
		
		return newMenu;		
	}	
	
	protected Menu makeRadioGroup(String menuName, String[] itemNames) {
		Menu result = new Menu(menuName);
		
		for (String item : itemNames) {
			MenuItem newMenuItem = new MenuItem(item);
			if (menuName.equals("Metrics")) {
				newMenuItem.addActionListener(new SetMetricFormAction(this, item));
			} else if (menuName.equals("Collection")) {
				newMenuItem.addActionListener(new SetCollectionFormAction(this.getParent(), item));
			}
			result.add(newMenuItem);			
		}
				
		return result;
	}
	
	public Vobject getParent() {		
		return parent;
	}
}

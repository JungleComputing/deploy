package ibis.deploy.gui.performance.visuals;

import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.StatNotRequestedException;

import java.awt.Menu;
import java.awt.PopupMenu;

import javax.media.opengl.GL;

public interface VisualElementInterface {
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
	public static final int COLLECTION_SPHERE = 347;
		
	public void setSize(float width, float height);
		
	public Float[] getLocation();
	
	public void setLocation(Float[] newLocation);
	
	public void setSeparation(float newSeparation);
	
	public float getRadius();
	
	public int getGLName();
	
	public void setForm(int newForm) throws ModeUnknownException;
	
	public PopupMenu getMenu();
		
	public VisualElementInterface getParent();
	
	public void toggleMetricShown(String key) throws StatNotRequestedException;
	
	public void toggleAverages();	
	
	public Menu getAveragesMenu(String label);	
	
	//Specific to each Vobject type
	public void drawThis(GL gl, int glName);
}

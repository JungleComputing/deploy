package ibis.deploy.gui.gridvision.interfaces;

import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;

import java.awt.PopupMenu;

import javax.media.opengl.GL;

/**
 * The interface for visual elements in the GridVision project.
 * @author maarten
 *
 */
public interface VisualElement {	
	public static final int METRICS_BAR = 123;
	public static final int METRICS_TUBE = 124;
	public static final int METRICS_SPHERE = 125;
	
	public static final int COLLECTION_CITYSCAPE = 345;
	public static final int COLLECTION_CIRCLE = 346;
	public static final int COLLECTION_SPHERE = 347;
		
	/**
	 * Updates this visual element with the new values presented in its IbisConcept counterpart
	 */
	public void update();
		
	/**
	 * Draws this element in the 3D world.
	 * @param gl
	 * 		The JOGL instance this element is to be drawn in.
	 * @param glMode
	 * 		The drawing mode, either GL_SELECT or GL_RENDER
	 */
	public void drawThis(GL gl, int glMode);
	
	/**
	 * Set the form of this visual element, metric forms and collection forms are both permitted as arguments
	 * @param newForm
	 * 		One of the METRICS or COLLECTION constants defined in this interface.
	 * @throws ModeUnknownException
	 * 		thrown if the new form parameter is not one of the constants given. 
	 */
	public void setForm(int newForm) throws ModeUnknownException;	
	
	/**
	 * Returns the context-sensitive menu specific to this element.
	 * @return
	 */
	public PopupMenu getMenu();	
		
	/**
	 * Toggles a metric to be shown or not shown in this element and all of its children.
	 * @param key
	 * 		The String representation of the metric to be toggled
	 * @throws StatNotRequestedException
	 * 		thrown if the metric requested is not gathered by the data gathering module
	 */
	public void toggleMetricShown(String key) throws StatNotRequestedException;
}
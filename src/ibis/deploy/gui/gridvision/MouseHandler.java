package ibis.deploy.gui.gridvision;

import java.awt.PopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GLCanvas;
import javax.swing.SwingUtilities;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener{
	GridVision gv;
	GLCanvas canvas;
	
	private float viewDistOrigin; 
	private float viewDist = -6; 
	
	private Float[] rotation;
	private Float[] translation;
	
	private float dragRightXorigin;
	private float rotationXorigin = 0; 
	private float rotationX;
	
	private float dragRightYorigin;
	private float rotationYorigin = 0; 
	private float rotationY;
	
	private float dragLeftXorigin;
	private float translationXorigin = 0; 
	private float translationX = 0;
	
	private float dragLeftYorigin;
	private float translationYorigin = 0; 
	private float translationY = 0;
	
	private float translationZ = 0;
	
	MouseHandler(GridVision gv, GLCanvas canvas) {
		this.gv = gv;
		this.canvas = canvas;
		
		rotation = new Float[3];
		translation = new Float[3];		
	}
	
	public void resetTranslation() {
		this.translation = new Float[3];
		this.translationX = 0.0f;
		this.translationY = 0.0f;
		this.translationZ = 0.0f;
		
		this.translationXorigin = 0.0f;
		this.translationYorigin = 0.0f;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 1) {	
				gv.pickRequest(e.getPoint());				
			} else {
				gv.relocateOrigin(e.getPoint());
			}
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			
		} else if (SwingUtilities.isRightMouseButton(e)) {		
			if (e.getClickCount() == 1) {
				PopupMenu popup = gv.menuRequest();
				canvas.add(popup);
				popup.show(gv.canvas, e.getX(), e.getY());
			}
		}		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {			
			dragLeftXorigin = e.getPoint().x;
			dragLeftYorigin = e.getPoint().y;
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			dragRightXorigin = e.getPoint().y;
			dragRightYorigin = e.getPoint().x;
		}
	}

	public void mouseReleased(MouseEvent e) {
		rotationXorigin = rotationX;
		rotationYorigin = rotationY;	
		
		translationXorigin = translationX;
		translationYorigin = translationY;
	}

	public void mouseDragged(MouseEvent e) { 
		if (SwingUtilities.isMiddleMouseButton(e)) {
			rotationX = ((e.getPoint().y - dragRightXorigin) + rotationXorigin) % 360;
			rotationY = ((e.getPoint().x - dragRightYorigin) + rotationYorigin) % 360;
			rotation[0] = rotationX;				 
			rotation[1] = rotationY;
			rotation[2] = 0.0f;
			gv.setRotation(rotation);			
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			translationX =  ((e.getPoint().x - dragLeftXorigin) + translationXorigin);
			translationY = -((e.getPoint().y - dragLeftYorigin) + translationYorigin);
			translation[0] = translationX/10;				 
			translation[1] = translationY/10;
			translation[2] = 0.0f;
			gv.setTranslation(translation);
		}
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseWheelMoved(MouseWheelEvent e) {	
		viewDist += viewDistOrigin + e.getWheelRotation();	
		gv.setViewDist(viewDist);
	}

}

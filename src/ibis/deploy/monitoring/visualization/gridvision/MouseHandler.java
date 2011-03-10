package ibis.deploy.monitoring.visualization.gridvision;

import java.awt.PopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


import javax.media.opengl.awt.GLJPanel;
import javax.swing.SwingUtilities;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener{	
	private JungleGoggles jv;
		
	private float viewDist = -6;
	
	private Float[] rotation;
	
	private float rotationXorigin = 0; 
	private float rotationX;
	
	private float rotationYorigin = 0; 
	private float rotationY;
	
	private float dragLeftXorigin;	
	private float dragLeftYorigin;
	
	public MouseHandler(JungleGoggles jv) {
		this.jv = jv;
		
		rotation = new Float[3];
	}
	
	public void mouseClicked(MouseEvent e) {		
		if (SwingUtilities.isLeftMouseButton(e)) {
			jv.doPickRequest(e.getPoint());
			if (e.getClickCount() != 1) {
				jv.doRecenterRequest();
			}			
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			//Nothing yet
		} else if (SwingUtilities.isRightMouseButton(e)) {
			PopupMenu popup = jv.menuRequest();
			GLJPanel canvas = jv.getPanel();
			canvas.add(popup);
			popup.show(canvas, e.getX(), e.getY());
		}		
	}

	public void mouseEntered(MouseEvent e) {
		//Empty - unneeded		
	}

	public void mouseExited(MouseEvent e) {
		//Empty - unneeded		
	}

	public void mousePressed(MouseEvent e) {		
		if (SwingUtilities.isLeftMouseButton(e)) {			
			dragLeftXorigin = e.getPoint().x;
			dragLeftYorigin = e.getPoint().y;
		}
	}

	public void mouseReleased(MouseEvent e) {
		rotationXorigin = rotationX;
		rotationYorigin = rotationY;
	}

	public void mouseDragged(MouseEvent e) { 
		if (SwingUtilities.isLeftMouseButton(e)) {
			// x/y reversed because of axis orientation
			rotationY = ((e.getPoint().x - dragLeftXorigin) + rotationYorigin) % 360;
			rotationX = ((e.getPoint().y - dragLeftYorigin) + rotationXorigin) % 360;
			rotation[0] = rotationX;
			rotation[1] = rotationY;
			rotation[2] = 0.0f;
			jv.setRotation(rotation);
		}
	}

	public void mouseMoved(MouseEvent e) {
		//Empty - unneeded		
	}

	public void mouseWheelMoved(MouseWheelEvent e) {	
		viewDist += e.getWheelRotation();
		if (viewDist > 0) { viewDist = 0; }
		
		//System.out.println("viewdist "+ viewDist + " dragCoefficient " + dragCoefficient);
		jv.setViewDist(viewDist);
	}
}

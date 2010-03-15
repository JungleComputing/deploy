package ibis.deploy.gui.performance;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener{
	PerfVis perfvis;
	
	private float viewDistOrigin; 
	private float viewDist = -6; 
	
	private float dragXorigin;
	private float viewXorigin = 0; 
	private float viewX;
	
	private float dragYorigin;
	private float viewYorigin = 0; 
	private float viewY;
	
	MouseHandler(PerfVis perfvis) {
		this.perfvis = perfvis;
	}

	public void mouseClicked(MouseEvent e) {
		
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			perfvis.pickPoint = e.getPoint();
			perfvis.doPickNextCycle = true;
		} else if (SwingUtilities.isRightMouseButton(e)) {
			dragXorigin = e.getPoint().y;
			dragYorigin = e.getPoint().x;
		}
	}

	public void mouseReleased(MouseEvent e) {
		viewXorigin = viewX;
		viewYorigin = viewY;		
	}

	public void mouseDragged(MouseEvent e) { 
		if (SwingUtilities.isRightMouseButton(e)) {
			viewX = (e.getPoint().y - dragXorigin + viewXorigin) % 360;
			perfvis.viewX = viewX;
			viewY = (e.getPoint().x - dragYorigin + viewYorigin) % 360;
			perfvis.viewY = viewY;
		}		
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseWheelMoved(MouseWheelEvent e) {	
		viewDist += viewDistOrigin + e.getWheelRotation();	
		perfvis.viewDist = viewDist;
	}

}

package ibis.deploy.gui.outputViz.common;

import ibis.deploy.gui.outputViz.GLWindow;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GLEventListener;
import javax.swing.SwingUtilities;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener{	
	private GLEventListener window;
		
	
	private Vec3 rotation;
	
	private float rotationXorigin = 0; 
	private float rotationX;
	
	private float rotationYorigin = 0; 
	private float rotationY;
	
	private float dragLeftXorigin;	
	private float dragLeftYorigin;
	
	public MouseHandler(GLEventListener window) {
		this.window = window;
		
		rotation = new Vec3();
	}
	
	public void mouseClicked(MouseEvent e) {	
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
			if (rotationY < 0) rotationY = 360f+rotationY % 360;
			rotationX = ((e.getPoint().y - dragLeftYorigin) + rotationXorigin) % 360;
			if (rotationX < 0) rotationX = 360f+rotationX % 360;
			rotation.set(0, rotationX);
			rotation.set(1, rotationY);
			rotation.set(2, 0f);
			((GLWindow) window).setRotation(rotation);
		}
	}

	public void mouseMoved(MouseEvent e) {
		//Empty - unneeded		
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		float viewDist = ((GLWindow) window).getViewDist();
		viewDist += e.getWheelRotation()*10;
		
		((GLWindow) window).setViewDist(viewDist);
	}
}

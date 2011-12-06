package amuseVisualization.openglCommon;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import amuseVisualization.GLWindow;
import amuseVisualization.openglCommon.math.Vec3;

public class InputHandler implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	private GLWindow window;
	
	private Vec3 rotation;
	
	private float rotationXorigin = 0; 
	private float rotationX;
	
	private float rotationYorigin = 0; 
	private float rotationY;
	
	private float dragLeftXorigin;	
	private float dragLeftYorigin;
	
	public InputHandler(GLWindow window) {
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
			// x/y reversed because of axis orientation. (up/down => x axis rotation in OpenGL)
			if (e.isShiftDown()) {
				rotationX = ((e.getPoint().x - dragLeftXorigin) /10f + rotationXorigin) % 360;
				rotationY = ((e.getPoint().y - dragLeftYorigin) /10f + rotationYorigin) % 360;
			} else {				
				rotationX = ((e.getPoint().x - dragLeftXorigin) + rotationXorigin) % 360;
				rotationY = ((e.getPoint().y - dragLeftYorigin) + rotationYorigin) % 360;
			}
			//Make sure the numbers are always positive (so we can determine the octant we're in more easily)
			if (rotationX < 0) rotationX = 360f+rotationX % 360;
			if (rotationY < 0) rotationY = 360f+rotationY % 360;
			
			rotation.set(0, rotationY);
			rotation.set(1, rotationX);
			rotation.set(2, 0f); // We never rotate around the Z axis.
			((GLWindow) window).setRotation(rotation);
		}
	}

	public void mouseMoved(MouseEvent e) {
		//Empty - unneeded		
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		float viewDist = ((GLWindow) window).getViewDist();
		
		if (e.isShiftDown()) {
			viewDist -= e.getWheelRotation()*2;
		} else {
			viewDist -= e.getWheelRotation()*10;
		}
		((GLWindow) window).setViewDist(viewDist);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}

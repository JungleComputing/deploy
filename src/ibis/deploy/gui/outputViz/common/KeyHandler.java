package ibis.deploy.gui.outputViz.common;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GLEventListener;

public class KeyHandler implements KeyListener {
	GLEventListener window;
	
	public KeyHandler(GLEventListener window) {
		this.window = window;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
//		int code = e.getKeyCode();
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

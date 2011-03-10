package ibis.deploy.monitoring.visualization.gridvision.swing;

import java.awt.BorderLayout;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.KeyHandler;
import ibis.deploy.monitoring.visualization.gridvision.MouseHandler;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JPanel;

import com.jogamp.opengl.util.FPSAnimator;

public class GogglePanel extends JPanel {
	private static final long serialVersionUID = 4754345291079348455L;
	
	GLJPanel gljpanel;

	public GogglePanel(final Collector collector) {
		setLayout(new BorderLayout(0, 0));
		
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {
        //    public void run() {
            	//Make the GLEventListener
            	JungleGoggles gog = new JungleGoggles(collector); 
            	
            	//Standard GL2 capabilities
            	GLProfile glp = GLProfile.get(GLProfile.GL2);
            	glp = GLProfile.get(GLProfile.GL2);
        		GLCapabilities glCapabilities = new GLCapabilities(glp);
        		
        		//glCapabilities.setDoubleBuffered(true);
        		glCapabilities.setHardwareAccelerated(true);
        		
        		//Anti-Aliasing
        		glCapabilities.setSampleBuffers(true);
        		glCapabilities.setNumSamples(4);
        		            	
            	gljpanel = new GLJPanel(glCapabilities);    	
        		gljpanel.addGLEventListener(gog);
        		
        		//Add Mouse event listener
        		MouseHandler mouseHandler = new MouseHandler(gog);
        		gljpanel.addMouseListener(mouseHandler);
        		gljpanel.addMouseMotionListener(mouseHandler);
        		gljpanel.addMouseWheelListener(mouseHandler);
        		
        		//Add key event listener
        		KeyHandler keyHandler = new KeyHandler(gog);
        		gljpanel.addKeyListener(keyHandler);
        		
        		//Set up animator
        		final FPSAnimator animator = new FPSAnimator(gljpanel, 60);
        		
        		//Start drawing
        		animator.start();
        		
        		//Set up the window	
        		add(gljpanel);
        		
        		gljpanel.requestFocusInWindow();
        //    }
		//});
	}
}

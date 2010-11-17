package ibis.deploy.gui.gridvision;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.gridvision.swing.*;

import java.awt.BorderLayout;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import com.sun.opengl.util.FPSAnimator;

public class GridVisionPanel extends JPanel {	
	private static final long serialVersionUID = 2947480963529559102L;
	
	protected GLCanvas canvas;
	protected FPSAnimator animator;
	GridVision perfvis;
	
	public GridVisionPanel(GUI gui) {
		//Add the option to enable this feature to the Ibis Deploy menu bar	
		JMenuBar menuBar = gui.getMenuBar();
        JMenu menu = null;
        Action menuAction = new GridVisionAction(gui, this);
                
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            if (menuBar.getMenu(i).getText().equals("View")) {
                menu = menuBar.getMenu(i);
            }
        }
        if (menu == null) {
            menu = new JMenu("View");
            menu.add(menuAction);            
            menuBar.add(menu, Math.max(0, menuBar.getMenuCount() - 1));
        } else {
            boolean found = false;
            for (int i = 0; i < menu.getComponentCount(); i++) {
                if (menu.getComponent(i) == menuAction) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                menu.add(menuAction);
            }
        }	
	}
	
	public void initialize(GUI gui) {
		//JOGL initializers
		GLCapabilities glCapabilities = new GLCapabilities();		
		glCapabilities.setDoubleBuffered(true);
		glCapabilities.setHardwareAccelerated(true);
		
		//Anti-Aliasing
		glCapabilities.setSampleBuffers(true);
		glCapabilities.setNumSamples(4);
		
		
		canvas = new GLCanvas(glCapabilities);
		animator = new FPSAnimator(canvas,60);
		
		perfvis = new GridVision(gui, canvas, this);				
		
		setLayout(new BorderLayout());		
		
		//Add the GridVision Menu bar to the gui
		JMenuBar bar = new JMenuBar();
			String[] refreshgroup = {"5000", "2000", "1000", "500", "200", "100"};
			JMenu refresh = makeRadioGroup(perfvis, "Refresh delay (in ms)", refreshgroup, "500");
		bar.add(refresh);
		this.add(bar, BorderLayout.NORTH);	    
		
		//Add the GLcanvas							
		canvas.addGLEventListener(perfvis);			
		this.add(canvas, BorderLayout.CENTER);
		animator.start();
	}
	
	public void shutdown() {
		animator.stop();
		perfvis.shutdown();
		perfvis = null;
	}	
	
	private JMenu makeRadioGroup(GridVision perfvis, String menuName, String[] itemNames, String initial) {
		JMenu result = new JMenu(menuName);
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem firstButton = null;
		
		for (int i=0; i<itemNames.length; i++) {
			JRadioButtonMenuItem action;
			
			if (itemNames[i].compareTo(initial) == 0) {
				action = new JRadioButtonMenuItem(itemNames[i], true);
			} else {
				action = new JRadioButtonMenuItem(itemNames[i], false);
			}
			
			if (i == 0) firstButton = action;
				if (menuName.compareTo("Refresh delay (in ms)") == 0) {
					action.setAction(new SetRefreshrateAction(perfvis, itemNames[i]));						
				}
			group.add(action);
			result.add(action);			
		}
		
		group.setSelected(firstButton.getModel(), true);
		
		return result;
	}	
}

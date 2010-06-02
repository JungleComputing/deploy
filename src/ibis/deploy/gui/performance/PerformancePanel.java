package ibis.deploy.gui.performance;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.performance.swing.*;

import java.awt.BorderLayout;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import com.sun.opengl.util.Animator;

public class PerformancePanel extends JPanel {	
	private static final long serialVersionUID = 2947480963529559102L;
	
	protected GLCanvas canvas;
	protected Animator animator;
	public PerformancePanel(GUI gui) {
		//JOGL initializers
		GLCapabilities glCapabilities = new GLCapabilities();		
		glCapabilities.setDoubleBuffered(true);
		glCapabilities.setHardwareAccelerated(true);
		canvas = new GLCanvas(glCapabilities);
		animator = new Animator(canvas);
		
		PerfVis perfvis = new PerfVis(gui, canvas, this);
				
		
		setLayout(new BorderLayout());
		
		
		//Add the Menu bar
		JMenuBar bar = new JMenuBar();
			String[] refreshgroup = {"5000", "2000", "1000", "500", "200", "100"};
			JMenu refresh = makeRadioGroup(perfvis, "Refresh delay (in ms)", refreshgroup);
		bar.add(refresh);
		/*
			String[] zoomgroup = {"Pools","Sites", "Nodes"};
			JMenu zoom = makeRadioGroup(perfvis, "Zoom", zoomgroup);
		bar.add(zoom);
			
			String[] statsgroup = {"All","CPU Usage", "Memory Usage"};
			JMenu stats = makeRadioGroup(perfvis, "Statistics", statsgroup);
		bar.add(stats);
		
		bar.add(Box.createHorizontalGlue());
		
			String[] collectionsgroup = {"Cityscapes", "Circles"};
			JMenu collections = makeRadioGroup(perfvis, "Collections", collectionsgroup);
		bar.add(collections);
		
			String[] elementsgroup = {"Bars", "Tubes", "Spheres"};
			JMenu elements = makeRadioGroup(perfvis, "Elements", elementsgroup);
		bar.add(elements);
		*/
		this.add(bar, BorderLayout.NORTH);	    
		
		//Add the GLcanvas							
		canvas.addGLEventListener(perfvis);			
		this.add(canvas, BorderLayout.CENTER);
		animator.start();
	}
	
	
	private JMenu makeRadioGroup(PerfVis perfvis, String menuName, String[] itemNames) {
		JMenu result = new JMenu(menuName);
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem firstButton = null;
		
		for (int i=0; i<itemNames.length; i++) {
			JRadioButtonMenuItem action = new JRadioButtonMenuItem(itemNames[i]);
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

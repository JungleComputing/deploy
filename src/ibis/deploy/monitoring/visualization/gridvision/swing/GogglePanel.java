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

import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class GogglePanel extends JPanel {
	private static final long serialVersionUID = 4754345291079348455L;

	GLJPanel gljpanel;

	public GogglePanel(final Collector collector) {
		setLayout(new BorderLayout(0, 0));

		// Make the GLEventListener
		JungleGoggles goggles = new JungleGoggles(collector, this);

		// Standard GL2 capabilities
		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities glCapabilities = new GLCapabilities(glp);

		// glCapabilities.setDoubleBuffered(true);
		glCapabilities.setHardwareAccelerated(true);

		// Anti-Aliasing
		glCapabilities.setSampleBuffers(true);
		glCapabilities.setNumSamples(4);

		gljpanel = new GLJPanel(glCapabilities);
		gljpanel.addGLEventListener(goggles);

		// Add Mouse event listener
		MouseHandler mouseHandler = new MouseHandler(goggles);
		gljpanel.addMouseListener(mouseHandler);
		gljpanel.addMouseMotionListener(mouseHandler);
		gljpanel.addMouseWheelListener(mouseHandler);

		// Add key event listener
		KeyHandler keyHandler = new KeyHandler(goggles);
		gljpanel.addKeyListener(keyHandler);

		// Set up animator
		final FPSAnimator animator = new FPSAnimator(gljpanel, 60);

		// Start drawing
		animator.start();

		// Set up the window
		add(gljpanel);
		
		//Create Menus
			JMenuBar menuBar = new JMenuBar();
				JMenu mnCollections = new JMenu("Collections");
			menuBar.add(mnCollections);
			
				String[] metricItems = {"Bars","Tubes","Spheres"};
				ButtonGroup metricGroup = new ButtonGroup();
				GoggleAction al1 = new SetMetricFormAction(goggles, "Bars");
			menuBar.add(makeRadioMenu("Metrics", metricGroup, metricItems, "Bars", al1));
			
				String[] networkItems = {"Tubes","AlphaTubes","Particles"};
				ButtonGroup networkGroup = new ButtonGroup();
				GoggleAction al2 = new SetNetworkFormAction(goggles, "Particles");
			menuBar.add(makeRadioMenu("Network", networkGroup, networkItems, "Particles", al2));
						
		add(menuBar, BorderLayout.NORTH);

		gljpanel.requestFocusInWindow();
	}
	
	public GLJPanel getPanel() {
		return gljpanel;
	}
	
	private JMenu makeRadioMenu(String name, ButtonGroup group, String[] labels, String currentSelection, GoggleAction al) {
		JMenu result = new JMenu(name);
		
		for (String label : labels) {
			JRadioButtonMenuItem current = new JRadioButtonMenuItem(label);
			current.addActionListener(al.clone(label));
			result.add(current);
			group.add(current);
			if (currentSelection.compareTo(label) == 0) {
				group.setSelected(current.getModel(), true);
			} else {
				group.setSelected(current.getModel(), false);
			}
		}
		
		return result;
	}
}

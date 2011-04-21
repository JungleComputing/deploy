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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class GogglePanel extends JPanel {
	private static final long serialVersionUID = 4754345291079348455L;
	public static enum TweakState { NONE, GLOBAL, METRICS, NETWORK };

	private JungleGoggles goggles;
	private GLJPanel gljpanel;
	
	private JPanel tweakPanel;
	private JPanel globalTweaks;
	private JPanel networkTweaks;
	private JPanel metricTweaks;
	
	private TweakState currentTweakState = TweakState.NONE;
	
	private JLabel thresholdText;
	private JLabel refreshrateText;

	public GogglePanel(final Collector collector) {
		setLayout(new BorderLayout(0, 0));

		// Make the GLEventListener
		goggles = new JungleGoggles(collector, this);

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
		
		//Add the Menu bar
		createMenus();
		
		//Add the tweaks panels
		tweakPanel = new JPanel();
		add(tweakPanel, BorderLayout.WEST);
		tweakPanel.setLayout(new BoxLayout(tweakPanel, BoxLayout.Y_AXIS));
		tweakPanel.setVisible(false);
		
		
		networkTweaks = new JPanel();
		networkTweaks.setLayout(new BoxLayout(networkTweaks, BoxLayout.Y_AXIS));		
		createNetworkTweakPanel();
		
		globalTweaks = new JPanel();
		globalTweaks.setLayout(new BoxLayout(globalTweaks, BoxLayout.Y_AXIS));		
		createGlobalTweakPanel();
		
		metricTweaks = new JPanel();
		metricTweaks.setLayout(new BoxLayout(metricTweaks, BoxLayout.Y_AXIS));		
		createMetricTweakPanel();	
		
		// Set up the window
		add(gljpanel, BorderLayout.CENTER);
				
		gljpanel.setFocusable(true);
		gljpanel.requestFocusInWindow();
	}
	
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();		
				String[] tweakItems = {"None" , "Global", "Metrics", "Network"};
				ButtonGroup tweakGroup = new ButtonGroup();
				GoggleAction al0 = new SetTweakStateAction(this, "None");
			menuBar.add(makeRadioMenu("Tweaks", tweakGroup, tweakItems, "None", al0));
						
		add(menuBar, BorderLayout.NORTH);
	}
	
	private void createGlobalTweakPanel() {
			Box refreshrateHBOX = Box.createHorizontalBox();
				Box refreshRateVBOX = Box.createVerticalBox();
				
				refreshRateVBOX.setBorder(new LineBorder(new Color(0, 0, 0)));
				refreshRateVBOX.setAlignmentY(Component.TOP_ALIGNMENT);
			
					Component verticalStrut = Box.createVerticalStrut(5);
				refreshRateVBOX.add(verticalStrut);
			
					JLabel thresholdlabel = new JLabel("Refreshrate");
					thresholdlabel.setVerticalAlignment(SwingConstants.TOP);
					thresholdlabel.setAlignmentY(Component.TOP_ALIGNMENT);
				refreshRateVBOX.add(thresholdlabel);
			
					JSlider slider = new JSlider();			
					slider.setMinimum(100);
					slider.setMaximum(5000);				
					slider.setMinorTickSpacing(100);				
					slider.setPaintTicks(true);
					slider.setSnapToTicks(true);					
					slider.setValue(500);				
					slider.addChangeListener(new RefreshrateSliderChangeListener(this));
				refreshRateVBOX.add(slider);
			
					refreshrateText = new JLabel("Refreshing every 500 ms");
				refreshRateVBOX.add(refreshrateText);
				
			refreshrateHBOX.add(refreshRateVBOX);
		globalTweaks.add(refreshrateHBOX);		
	}
	
	private void createNetworkTweakPanel() {
		Box linkMetricHrzBox = Box.createHorizontalBox();
			Box linkMetricVrtBox = Box.createVerticalBox();
				
				linkMetricVrtBox.setBorder(new LineBorder(new Color(0, 0, 0)));
				linkMetricVrtBox.setAlignmentY(Component.TOP_ALIGNMENT);
				
				Component verticalStrut_1 = Box.createVerticalStrut(5);
				linkMetricVrtBox.add(verticalStrut_1);
				
				JLabel lblNetworkMetricDisplay = new JLabel("Network metrics display method");
				linkMetricVrtBox.add(lblNetworkMetricDisplay);
				
					JRadioButton rdbtnParticles = new JRadioButton("Particles");
					JRadioButton rdbtnAlphaTubes = new JRadioButton("Alpha Tubes");
					JRadioButton rdbtnTubes = new JRadioButton("Tubes");
					
					ButtonGroup linkMetricGroup = new ButtonGroup();
					linkMetricGroup.add(rdbtnParticles);
					linkMetricGroup.add(rdbtnAlphaTubes);
					linkMetricGroup.add(rdbtnTubes);
					
					GoggleAction particlesAction = new SetNetworkFormAction(goggles, "Particles");
					GoggleAction alphaTubesAction = new SetNetworkFormAction(goggles, "AlphaTubes");
					GoggleAction tubesAction = new SetNetworkFormAction(goggles, "Tubes");
					
					rdbtnParticles.addActionListener(particlesAction);
					rdbtnAlphaTubes.addActionListener(alphaTubesAction);
					rdbtnTubes.addActionListener(tubesAction);
					
					rdbtnParticles.setSelected(true);
					
				linkMetricVrtBox.add(rdbtnParticles);			
				linkMetricVrtBox.add(rdbtnAlphaTubes);			
				linkMetricVrtBox.add(rdbtnTubes);
				
			linkMetricHrzBox.add(linkMetricVrtBox);	
		networkTweaks.add(linkMetricHrzBox);
		
			Component verticalStrut_2 = Box.createVerticalStrut(5);
		networkTweaks.add(verticalStrut_2);
		
			Box thresholdHrzBox = Box.createHorizontalBox();
				Box thresholdVrtBox = Box.createVerticalBox();
			
				thresholdVrtBox.setBorder(new LineBorder(new Color(0, 0, 0)));
				thresholdVrtBox.setAlignmentY(Component.TOP_ALIGNMENT);
			
					Component verticalStrut = Box.createVerticalStrut(5);
				thresholdVrtBox.add(verticalStrut);
			
					JLabel thresholdlabel = new JLabel("Network bandwidth threshold.");
					thresholdlabel.setVerticalAlignment(SwingConstants.TOP);
					thresholdlabel.setAlignmentY(Component.TOP_ALIGNMENT);
				thresholdVrtBox.add(thresholdlabel);
			
					JSlider slider = new JSlider();			
					slider.setMinimum(1000);
					slider.setMaximum(1001000);				
					slider.setMinorTickSpacing(100000);				
					slider.setPaintTicks(true);
					slider.setSnapToTicks(true);					
					slider.setValue(501000);				
					slider.addChangeListener(new ThresholdSliderChangeListener(this));	
				thresholdVrtBox.add(slider);
			
					thresholdText = new JLabel("500 kb/s");
				thresholdVrtBox.add(thresholdText);
			
			thresholdHrzBox.add(thresholdVrtBox);
		networkTweaks.add(thresholdHrzBox);	
		
	}
	
	private void createMetricTweakPanel() {
		Box metricHrzBox = Box.createHorizontalBox();
			Box metricVrtBox = Box.createVerticalBox();
				
				metricVrtBox.setBorder(new LineBorder(new Color(0, 0, 0)));
				metricVrtBox.setAlignmentY(Component.TOP_ALIGNMENT);
				
				Component verticalStrut_1 = Box.createVerticalStrut(5);
				metricVrtBox.add(verticalStrut_1);
				
				JLabel metricDisplay = new JLabel("Node metrics display method");
				metricVrtBox.add(metricDisplay);
				
					JRadioButton rdbtnBars = new JRadioButton("Bars");
					JRadioButton rdbtnTubes = new JRadioButton("Tubes");
					JRadioButton rdbtnSpheres = new JRadioButton("Spheres");
					
					ButtonGroup linkMetricGroup = new ButtonGroup();
					linkMetricGroup.add(rdbtnBars);
					linkMetricGroup.add(rdbtnTubes);
					linkMetricGroup.add(rdbtnSpheres);
					
					GoggleAction barsAction = new SetMetricFormAction(goggles, "Bars");
					GoggleAction tubesAction = new SetMetricFormAction(goggles, "Tubes");
					GoggleAction spheresAction = new SetMetricFormAction(goggles, "Spheres");
					
					rdbtnBars.addActionListener(barsAction);
					rdbtnTubes.addActionListener(tubesAction);
					rdbtnSpheres.addActionListener(spheresAction);
					
					rdbtnBars.setSelected(true);
					
				metricVrtBox.add(rdbtnBars);			
				metricVrtBox.add(rdbtnTubes);			
				metricVrtBox.add(rdbtnSpheres);
				
			metricHrzBox.add(metricVrtBox);	
		metricTweaks.add(metricHrzBox);
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
	
	public void setTweakState(TweakState newState) {
		tweakPanel.setVisible(false);
		tweakPanel.remove(globalTweaks);
		tweakPanel.remove(networkTweaks);
		tweakPanel.remove(metricTweaks);
		
		currentTweakState = newState;		
		
		if (currentTweakState == TweakState.NONE) {
		} else if (currentTweakState == TweakState.GLOBAL) {
			tweakPanel.setVisible(true);
			tweakPanel.add(globalTweaks, BorderLayout.WEST);
		} else if (currentTweakState == TweakState.METRICS) {
			tweakPanel.setVisible(true);
			tweakPanel.add(metricTweaks, BorderLayout.WEST);
		} else if (currentTweakState == TweakState.NETWORK) {
			tweakPanel.setVisible(true);
			tweakPanel.add(networkTweaks, BorderLayout.WEST);
		}
	}
	
	public JungleGoggles getGoggles() {
		return goggles;
	}

	public void setNetworkThresholdText(int newMax) {
		//Transform into human-readable text (kb/s)
		newMax = (newMax-1000) / 1000;
		if (newMax == 0) newMax = 1;
		
		String text = String.valueOf(newMax);
		thresholdText.setText(text + " kb/s");
	}
	
	public void setRefreshrateText(int newRate) {
		String text = String.valueOf(newRate);
		refreshrateText.setText("Refreshing every " +text+ " ms");
	}
}

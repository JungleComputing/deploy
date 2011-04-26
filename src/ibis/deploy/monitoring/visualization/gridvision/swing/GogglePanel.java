package ibis.deploy.monitoring.visualization.gridvision.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.KeyHandler;
import ibis.deploy.monitoring.visualization.gridvision.MouseHandler;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.MetricDescriptionNotAvailableException;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.GoggleAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.GoggleListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.IbisSpacingSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.MetricListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.MetricSpacingSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.RefreshrateSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetMetricFormAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetNetworkFormAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetTweakStateAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.LocationSpacingSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.ThresholdSliderChangeListener;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JPanel;

import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.util.ArrayList;

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
	private JLabel locationSpacerText;
	private JLabel ibisSpacerText;
	private JLabel metricSpacerText;

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
		tweakPanel.setPreferredSize(new Dimension(200, 0));
		tweakPanel.setVisible(false);
		
		
		networkTweaks = new JPanel();
		networkTweaks.setLayout(new BoxLayout(networkTweaks, BoxLayout.Y_AXIS));
		networkTweaks.setMinimumSize(tweakPanel.getPreferredSize());
		createNetworkTweakPanel();
		
		globalTweaks = new JPanel();
		globalTweaks.setLayout(new BoxLayout(globalTweaks, BoxLayout.Y_AXIS));
		globalTweaks.setMinimumSize(tweakPanel.getPreferredSize());
		createGlobalTweakPanel();
		
		metricTweaks = new JPanel();
		metricTweaks.setLayout(new BoxLayout(metricTweaks, BoxLayout.Y_AXIS));
		metricTweaks.setMinimumSize(tweakPanel.getPreferredSize());
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
		ArrayList<Component> components = new ArrayList<Component>();		
				JLabel thresholdlabel = new JLabel("Refreshrate");
			components.add(thresholdlabel);
		
				JSlider slider = new JSlider();			
				slider.setMinimum(100);
				slider.setMaximum(5000);				
				slider.setMinorTickSpacing(100);				
				slider.setPaintTicks(true);
				slider.setSnapToTicks(true);					
				slider.setValue(500);				
				slider.addChangeListener(new RefreshrateSliderChangeListener(this));
			components.add(slider);
		
				refreshrateText = new JLabel("Refreshing every 500 ms");
			components.add(refreshrateText);		
		createBoxedComponents(globalTweaks, components);
		
			Component verticalStrut = Box.createVerticalStrut(5);
		globalTweaks.add(verticalStrut);
		
		ArrayList<Component> components2 = new ArrayList<Component>();		
				JLabel locationSpacingLabel = new JLabel("Location spacing");
			components2.add(locationSpacingLabel);
		
				JSlider slider2 = new JSlider();			
				slider2.setMinimum(1);
				slider2.setMaximum(32);				
				slider2.setMinorTickSpacing(1);				
				slider2.setPaintTicks(true);
				slider2.setSnapToTicks(true);					
				slider2.setValue(16);				
				slider2.addChangeListener(new LocationSpacingSliderChangeListener(this));
			components2.add(slider2);
		
				locationSpacerText = new JLabel("Location spacing at 16 units");
			components2.add(locationSpacerText);		
		createBoxedComponents(globalTweaks, components2);
		
			Component verticalStrut2 = Box.createVerticalStrut(5);
		globalTweaks.add(verticalStrut2);
		
		ArrayList<Component> components3 = new ArrayList<Component>();		
				JLabel ibisSpacingLabel = new JLabel("Ibis spacing");
			components3.add(ibisSpacingLabel);
		
				JSlider slider3 = new JSlider();			
				slider3.setMinimum(0);
				slider3.setMaximum(20);				
				slider3.setMinorTickSpacing(1);				
				slider3.setPaintTicks(true);
				slider3.setSnapToTicks(true);					
				slider3.setValue(12);				
				slider3.addChangeListener(new IbisSpacingSliderChangeListener(this));
			components3.add(slider3);
		
				ibisSpacerText = new JLabel("Ibis spacing at 1.2 units");
			components3.add(ibisSpacerText);		
		createBoxedComponents(globalTweaks, components3);
		
			Component verticalStrut3 = Box.createVerticalStrut(5);
		globalTweaks.add(verticalStrut3);
		
		ArrayList<Component> components4 = new ArrayList<Component>();		
				JLabel metricSpacingLabel = new JLabel("Metrics spacing");
			components4.add(metricSpacingLabel);
		
				JSlider slider4 = new JSlider();			
				slider4.setMinimum(0);
				slider4.setMaximum(20);				
				slider4.setMinorTickSpacing(1);
				slider4.setPaintTicks(true);
				slider4.setSnapToTicks(true);					
				slider4.setValue(1);				
				slider4.addChangeListener(new MetricSpacingSliderChangeListener(this));
			components4.add(slider4);
		
				metricSpacerText = new JLabel("Metrics spacing at 0.05 units");
			components4.add(metricSpacerText);		
		createBoxedComponents(globalTweaks, components4);
	}
	
	private void createNetworkTweakPanel() {		
		ButtonGroup linkMetricGroup = new ButtonGroup();
		String[] metricLabels = {"Particles", "AlphaTubes", "Tubes"};
		GoggleAction templateAction = new SetNetworkFormAction(goggles, "Particles");
		createRadioBox(networkTweaks, "Network metrics display method", metricLabels, linkMetricGroup, templateAction);
		
			Component verticalStrut = Box.createVerticalStrut(5);
		networkTweaks.add(verticalStrut);
		
		ArrayList<Component> components = new ArrayList<Component>();		
				JLabel thresholdlabel = new JLabel("Network bandwidth threshold.");
			components.add(thresholdlabel);
			
				JSlider slider = new JSlider();
				slider.setMinimum(0);
				slider.setMaximum(9);
				slider.setMinorTickSpacing(1);				
				slider.setPaintTicks(true);
				slider.setSnapToTicks(true);					
				slider.setValue(4);	
				slider.addChangeListener(new ThresholdSliderChangeListener(this));	
			components.add(slider);
			
				thresholdText = new JLabel("100 kb/s");
			components.add(thresholdText);		
		createBoxedComponents(networkTweaks, components);
	}
	
	private void createMetricTweakPanel() {		
		ButtonGroup metricGroup = new ButtonGroup();
		String[] metricLabels = {"Bars", "Tubes"};
		GoggleAction barsAction = new SetMetricFormAction(goggles, "Bars");
		createRadioBox(metricTweaks, "Node metrics display method", metricLabels, metricGroup, barsAction);
		
			Component verticalStrut = Box.createVerticalStrut(5);
		metricTweaks.add(verticalStrut);
		
		String[] toBeSelectedMetrics = {"CPU Usage", "System Memory Usage", "Java Heap Memory Usage", "Java Nonheap Memory Usage"};
		boolean[] selections = {true, true};
		GoggleListener selectionListener = new MetricListener(goggles, "");
		
		try {
			createCheckboxBox(metricTweaks, "Selected metrics", toBeSelectedMetrics, selections, selectionListener);
		} catch (MetricDescriptionNotAvailableException e) {
			e.printStackTrace();
		}
	}
	
	private void createRadioBox(JPanel parent, String name, String[] labels, ButtonGroup group, GoggleAction actionTemplate) {		
		ArrayList<Component> components = new ArrayList<Component>();		
			components.add(new JLabel(name));
			
			for (int i=0; i< labels.length; i++) {				
				JRadioButton btn = new JRadioButton(labels[i]);
				group.add(btn);
				GoggleAction action = actionTemplate.clone(labels[i]);
				btn.addActionListener(action);		
				if (i ==0 ) btn.setSelected(true);
				components.add(btn);
			}		
		createBoxedComponents(parent, components);
	}
	
	private void createCheckboxBox(JPanel parent, String name, String[] labels, boolean[] selections, GoggleListener listenerTemplate) throws MetricDescriptionNotAvailableException {		
		ArrayList<Component> components = new ArrayList<Component>();		
			components.add(new JLabel(name));
			
			for (int i=0; i< labels.length; i++) {
				MetricDescription desc = goggles.getMetricDescription(labels[i]);				
				JCheckBox btn = new JCheckBox(labels[i], true);
				btn.setSelectedIcon(new ColorIcon(desc));
				btn.setIcon(new ColorIcon(0,0,0));
				GoggleListener listener = listenerTemplate.clone(labels[i]);
				btn.addItemListener(listener);
				components.add(btn);
			}		
		createBoxedComponents(parent, components);
	}
	
	private void createBoxedComponents(JPanel parent, ArrayList<Component> components) {
		Box hrzBox = Box.createHorizontalBox();
			Box vrtBox = Box.createVerticalBox();
				
				vrtBox.setBorder(new LineBorder(new Color(0, 0, 0)));
				vrtBox.setAlignmentY(Component.TOP_ALIGNMENT);
				
				Component verticalStrut = Box.createVerticalStrut(5);
				vrtBox.add(verticalStrut);
				
				for (int i=0; i<components.size(); i++) {
					Component current = components.get(i);
					
					vrtBox.add(current);
				}
			
			hrzBox.add(vrtBox);	
		parent.add(hrzBox);
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
		String text = String.valueOf(newMax);
		thresholdText.setText(text + " kb/s");
	}
	
	public void setRefreshrateText(int newRate) {
		String text = String.valueOf(newRate);
		refreshrateText.setText("Refreshing every " +text+ " ms");
	}

	public void setLocationSpacerText(int sliderSetting) {
		String text = String.valueOf(sliderSetting);
		locationSpacerText.setText("Location spacing at "+text+" units");
	}
	
	public void setIbisSpacerText(float sliderSetting) {
		String text = String.valueOf(sliderSetting);
		ibisSpacerText.setText("Ibis spacing at "+text+" units");
	}
	
	public void setMetricSpacerText(float sliderSetting) {
		String text = String.valueOf(sliderSetting);
		metricSpacerText.setText("Metrics spacing at "+text+" units");
	}
}

package ibis.deploy.monitoring.visualization.gridvision.swing;

import ibis.deploy.gui.GUI;
import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.CollectionShape;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.KeyHandler;
import ibis.deploy.monitoring.visualization.gridvision.MouseHandler;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.MetricDescriptionNotAvailableException;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.GoggleAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetIbisCollectionFormAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetLocationCollectionFormAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetMetricFormAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetNetworkFormAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.actions.SetTweakStateAction;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.ExitListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.GoggleListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.IbisSpacingSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.LocationSpacingSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.MetricListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.MetricSpacingSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.ParentSkipListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.RefreshrateSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.listeners.ThresholdSliderChangeListener;
import ibis.deploy.monitoring.visualization.gridvision.swing.util.GoggleSwing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.util.FPSAnimator;

public class GogglePanel extends JPanel {
    private static final long serialVersionUID = 4754345291079348455L;

    public static enum TweakState {
        NONE, GATHERING, METRICS, NETWORK, VISUAL
    };

    private JungleGoggles goggles;
    private GLJPanel gljpanel;

    private JPanel tweakPanel;
    private JPanel gatheringTweaks;
    private JPanel networkTweaks;
    private JPanel metricTweaks;
    private JPanel visualTweaks;

    private TweakState currentTweakState = TweakState.NONE;

    private JLabel thresholdText;
    private JLabel refreshrateText;
    private JLabel locationSpacerText;
    private JLabel ibisSpacerText;
    private JLabel metricSpacerText;

    public GogglePanel(final GUI gui, final Collector collector) {
        final JButton initButton = new JButton("Initialize 3D Visualization");
        add(initButton);

        initButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeAll();

                // DEBUG
                // RegistryServiceInterface emptyReg = new
                // FakeRegistryService(1,1,1,1,1);
                // ManagementServiceInterface emptyMan = new
                // FakeManagementService(emptyReg);
                // Collector emptyCollector =
                // CollectorImpl.getCollector(emptyMan, emptyReg);
                initialize(gui, collector);
            }
        });
    }

    public void initialize(GUI gui, Collector collector) {

        if (collector == null) {
            JOptionPane.showMessageDialog(this, "No 3D visualization: data collection is disabled", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = false;
        try {
            // File[] nativeLibs = getNativeLibTargets();
            gui.loadNativeLibs();
            ok = true;
        } catch (IOException e) {
            System.err.println("Your OS is not supported by JOGL. 3D visualization will be disabled.");
            System.err.println(e.getMessage());
        } catch (Throwable e) {
            System.err.println("Something went wrong while loading JOGL natives. 3D visualization will be disabled.");
            System.err.println(e.getMessage());
        }

        if (!ok) {
            JOptionPane.showMessageDialog(this, "No 3D visualization: native library initialization failed", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

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

        // Add the Menu bar
        createMenus();

        initLabels();

        // Add the tweaks panels
        tweakPanel = new JPanel();
        add(tweakPanel, BorderLayout.WEST);
        tweakPanel.setLayout(new BoxLayout(tweakPanel, BoxLayout.Y_AXIS));
        tweakPanel.setPreferredSize(new Dimension(200, 0));
        tweakPanel.setVisible(false);

        networkTweaks = new JPanel();
        networkTweaks.setLayout(new BoxLayout(networkTweaks, BoxLayout.Y_AXIS));
        networkTweaks.setMinimumSize(tweakPanel.getPreferredSize());
        createNetworkTweakPanel();

        gatheringTweaks = new JPanel();
        gatheringTweaks.setLayout(new BoxLayout(gatheringTweaks, BoxLayout.Y_AXIS));
        gatheringTweaks.setMinimumSize(tweakPanel.getPreferredSize());
        createGatheringTweakPanel();

        metricTweaks = new JPanel();
        metricTweaks.setLayout(new BoxLayout(metricTweaks, BoxLayout.Y_AXIS));
        metricTweaks.setMinimumSize(tweakPanel.getPreferredSize());
        createMetricTweakPanel();

        visualTweaks = new JPanel();
        visualTweaks.setLayout(new BoxLayout(visualTweaks, BoxLayout.Y_AXIS));
        visualTweaks.setMinimumSize(tweakPanel.getPreferredSize());
        createVisualTweakPanel();

        // Set up the window
        add(gljpanel, BorderLayout.CENTER);

        gljpanel.setFocusable(true);
        gljpanel.requestFocusInWindow();
    }

    private void initLabels() {
        refreshrateText = new JLabel("Refreshing every " + goggles.getRefreshrate() + " ms");
        thresholdText = new JLabel("1 kb/s");
        locationSpacerText = new JLabel("Location spacing at 16 units");
        ibisSpacerText = new JLabel("Ibis spacing at 1.2 units");
        metricSpacerText = new JLabel("Metrics spacing at 0.05 units");
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        String[] tweakItems = { "None", "Gathering", "Metrics", "Network", "Visual" };
        ButtonGroup tweakGroup = new ButtonGroup();
        GoggleAction al0 = new SetTweakStateAction(this, "None");
        menuBar.add(makeRadioMenu("Tweaks", tweakGroup, tweakItems, "None", al0));

        add(menuBar, BorderLayout.NORTH);
    }

    private void createGatheringTweakPanel() {
        GoggleListener listener = new ExitListener(this);
        gatheringTweaks.add(GoggleSwing.titleBox("Gathering Tweaks", listener));

        String[] items = { "Skip" };
        boolean[] selections = { true };

        GoggleListener selectionListener = new ParentSkipListener(goggles);
        GoggleListener[] listeners = new GoggleListener[items.length];
        for (int i = 0; i < items.length; i++) {
            listeners[i] = selectionListener.clone(items[i]);
        }
        gatheringTweaks.add(GoggleSwing.checkboxBox("Skip lowest location", items, selections, listeners));

        gatheringTweaks.add(GoggleSwing.verticalStrut(5));

        ChangeListener listener2 = new RefreshrateSliderChangeListener(this);
        gatheringTweaks.add(GoggleSwing.sliderBox("Refreshrate", listener2, 100, 5000, 100, 5000, refreshrateText));
    }

    private void createNetworkTweakPanel() {
        GoggleListener listener = new ExitListener(this);
        networkTweaks.add(GoggleSwing.titleBox("Network Tweaks", listener));

        String[] linkLabels = { "Particles", "AlphaTubes", "Tubes" };
        GoggleAction templateAction = new SetNetworkFormAction(goggles, "Particles");
        GoggleAction[] actions = new GoggleAction[linkLabels.length];
        for (int i = 0; i < linkLabels.length; i++) {
            actions[i] = templateAction.clone(linkLabels[i]);
        }
        networkTweaks.add(GoggleSwing.radioBox("Node metrics display method", linkLabels, actions));

        networkTweaks.add(GoggleSwing.verticalStrut(5));

        ChangeListener listener2 = new ThresholdSliderChangeListener(this, goggles);
        networkTweaks.add(GoggleSwing.sliderBox("Network bandwidth threshold.", listener2, 0, 9, 1, 0, thresholdText));
    }

    private void createMetricTweakPanel() {
        GoggleListener listener = new ExitListener(this);
        metricTweaks.add(GoggleSwing.titleBox("Metric Tweaks", listener));

        String[] metricLabels = { "Bars", "Tubes" };
        GoggleAction barsAction = new SetMetricFormAction(goggles, "Bars");
        GoggleAction[] actions = new GoggleAction[metricLabels.length];
        for (int i = 0; i < metricLabels.length; i++) {
            actions[i] = barsAction.clone(metricLabels[i]);
        }
        metricTweaks.add(GoggleSwing.radioBox("Node metrics display method", metricLabels, actions));

        metricTweaks.add(GoggleSwing.verticalStrut(5));
        String[] toBeSelectedMetrics = { "CPU Usage", "System Load Average", "System Memory Usage",
                "Java Heap Memory Usage", "Java Nonheap Memory Usage" };
        boolean[] selections = { true, false, true, true, true };
        GoggleListener selectionListener = new MetricListener(goggles, "");
        Float[][] colors = new Float[toBeSelectedMetrics.length][3];
        GoggleListener[] listeners = new GoggleListener[toBeSelectedMetrics.length];

        try {
            for (int i = 0; i < toBeSelectedMetrics.length; i++) {
                MetricDescription desc = goggles.getMetricDescription(toBeSelectedMetrics[i]);
                colors[i] = desc.getColor();
                listeners[i] = selectionListener.clone(toBeSelectedMetrics[i]);
            }

        } catch (MetricDescriptionNotAvailableException e) {
            e.printStackTrace();
        }
        metricTweaks.add(GoggleSwing.legendBox("Selected Metrics", toBeSelectedMetrics, colors, selections, listeners));
    }

    private void createVisualTweakPanel() {
        GoggleListener listener = new ExitListener(this);
        visualTweaks.add(GoggleSwing.titleBox("Visual Tweaks", listener));

        String[] labels = { "CityScape", "Cube", "Sphere" };
        CollectionShape[] shapes = { CollectionShape.CITYSCAPE, CollectionShape.CUBE, CollectionShape.SPHERE };
        SetLocationCollectionFormAction action = new SetLocationCollectionFormAction(goggles, "Locations",
                CollectionShape.CITYSCAPE);
        SetLocationCollectionFormAction[] actions = new SetLocationCollectionFormAction[labels.length];
        for (int i = 0; i < labels.length; i++) {
            actions[i] = action.clone(labels[i], shapes[i]);
        }
        visualTweaks.add(GoggleSwing.buttonBox("All Locations", labels, actions));

        visualTweaks.add(GoggleSwing.verticalStrut(5));

        String[] labels1 = { "CityScape", "Cube", "Sphere" };
        CollectionShape[] shapes1 = { CollectionShape.CITYSCAPE, CollectionShape.CUBE, CollectionShape.SPHERE };
        SetIbisCollectionFormAction action1 = new SetIbisCollectionFormAction(goggles, "Locations",
                CollectionShape.CITYSCAPE);
        SetIbisCollectionFormAction[] actions1 = new SetIbisCollectionFormAction[labels1.length];
        for (int i = 0; i < labels1.length; i++) {
            actions1[i] = action1.clone(labels1[i], shapes1[i]);
        }
        visualTweaks.add(GoggleSwing.buttonBox("All Ibises", labels1, actions1));

        visualTweaks.add(GoggleSwing.verticalStrut(5));

        ChangeListener listener2 = new LocationSpacingSliderChangeListener(this);
        visualTweaks.add(GoggleSwing.sliderBox("Location spacing", listener2, 1, 200, 1, 16, locationSpacerText));

        visualTweaks.add(GoggleSwing.verticalStrut(5));

        ChangeListener listener3 = new IbisSpacingSliderChangeListener(this);
        visualTweaks.add(GoggleSwing.sliderBox("Ibis spacing", listener3, 0, 50, 1, 12, ibisSpacerText));

        visualTweaks.add(GoggleSwing.verticalStrut(5));

        ChangeListener listener4 = new MetricSpacingSliderChangeListener(this);
        visualTweaks.add(GoggleSwing.sliderBox("Metrics spacing", listener4, 0, 20, 1, 5, metricSpacerText));
    }

    public GLJPanel getPanel() {
        return gljpanel;
    }

    private JMenu makeRadioMenu(String name, ButtonGroup group, String[] labels, String currentSelection,
            GoggleAction al) {
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

    // Callback methods for the various ui actions and listeners
    public void setTweakState(TweakState newState) {
        tweakPanel.setVisible(false);
        tweakPanel.remove(gatheringTweaks);
        tweakPanel.remove(networkTweaks);
        tweakPanel.remove(metricTweaks);
        tweakPanel.remove(visualTweaks);

        currentTweakState = newState;

        if (currentTweakState == TweakState.NONE) {
        } else if (currentTweakState == TweakState.GATHERING) {
            tweakPanel.setVisible(true);
            tweakPanel.add(gatheringTweaks, BorderLayout.WEST);
        } else if (currentTweakState == TweakState.METRICS) {
            tweakPanel.setVisible(true);
            tweakPanel.add(metricTweaks, BorderLayout.WEST);
        } else if (currentTweakState == TweakState.NETWORK) {
            tweakPanel.setVisible(true);
            tweakPanel.add(networkTweaks, BorderLayout.WEST);
        } else if (currentTweakState == TweakState.VISUAL) {
            tweakPanel.setVisible(true);
            tweakPanel.add(visualTweaks, BorderLayout.WEST);
        }
    }

    public void setNetworkThreshold(int newMax) {
        String text = String.valueOf(newMax);
        thresholdText.setText(text + " kb/s");
    }

    public void setRefreshrate(int newRate) {
        goggles.setRefreshrate(newRate);
        String text = String.valueOf(newRate);
        refreshrateText.setText("Refreshing every " + text + " ms");
    }

    public void setLocationSpacer(int sliderSetting) {
        goggles.setLocationSpacing(sliderSetting);
        String text = String.valueOf(sliderSetting);
        locationSpacerText.setText("Location spacing at " + text + " units");
    }

    public void setIbisSpacer(float sliderSetting) {
        goggles.setIbisSpacing(sliderSetting);
        String text = String.valueOf(sliderSetting);
        ibisSpacerText.setText("Ibis spacing at " + text + " units");
    }

    public void setMetricSpacer(float sliderSetting) {
        goggles.setMetricSpacing(sliderSetting);
        String text = String.valueOf(sliderSetting);
        metricSpacerText.setText("Metrics spacing at " + text + " units");
    }
}

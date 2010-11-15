package ibis.deploy.gui.deployViz;

import ibis.deploy.gui.GUI;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ibis.deploy.gui.deployViz.edgeBundles.*;
import ibis.deploy.gui.deployViz.helpers.*;
import ibis.deploy.gui.gridvision.swing.GridVisionAction;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ui.JFastLabel;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

public class DeployVizPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final String DATA_FILE = "das3.xml";

    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String AGGR = "aggregates";
    public static final String NODE_NAME = "name";
    public static final String NODE_TYPE = "type";

    private static Visualization vis;
    private static Tree tree = null;

    public static final String TREE_LAYOUT = "treeLayout";
    public static final String RADIAL_TREE_LAYOUT = "radialTreeLayout";

    private BundledEdgeRenderer edgeRenderer;
    private JSlider slider;
    private JCheckBox cbox;
    private JColorChooser chooser = null;
    private JDialog colorDialog = null;
    private JButton lastSelectedButton = null, buttonStart = null,
            buttonStop = null;

    private HashMap<String, Color> nodeColorMap;

    private Action updateVisualizationAction;

    private TreeLayout lastSelectedLayout = null;
    private RadialTreeLayout radialTreeLayout;
    private NodeLinkTreeLayout treeLayout;
    private RadialGraphView graphDisplay;
    private JPanel vizPanel;

    // JMX variables
    private RegistryServiceInterface regInterface;
    private ManagementServiceInterface manInterface;

    private MetricsManager statman;

    public DeployVizPanel(GUI gui) {
        // Add the option to enable this feature to the Ibis Deploy menu bar
        JMenuBar menuBar = gui.getMenuBar();
        JMenu menu = null;

//        Action menuAction = new DeployVizAction(gui, this);
//
//        for (int i = 0; i < menuBar.getMenuCount(); i++) {
//            if (menuBar.getMenu(i).getText().equals("View")) {
//                menu = menuBar.getMenu(i);
//            }
//        }
//        if (menu == null) {
//            menu = new JMenu("View");
//            menu.add(menuAction);
//            menuBar.add(menu, Math.max(0, menuBar.getMenuCount() - 1));
//        } else {
//            boolean found = false;
//            for (int i = 0; i < menu.getComponentCount(); i++) {
//                if (menu.getComponent(i) == menuAction) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                menu.add(menuAction);
//            }
//        }

        setLayout(new BorderLayout());
        vizPanel = new JPanel(new BorderLayout());

        String infile = DATA_FILE;
        String label = "name";
        vizPanel.add(createVisualizationPanels(infile, label),
                BorderLayout.CENTER);

        // retrieve the JMX interfaces
        try {
            this.regInterface = gui.getDeploy().getServer()
                    .getRegistryService();
            this.manInterface = gui.getDeploy().getServer()
                    .getManagementService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        statman = new MetricsManager(manInterface, regInterface);
        // new Thread(statman).start();

        add(vizPanel, BorderLayout.CENTER);
    }

    public void shutdown() {
        statman.stopTimer();
    }

    class RadialGraphView extends Display {

        private static final long serialVersionUID = 1L;
        private LabelRenderer m_nodeRenderer;

        public RadialGraphView(Graph g, String label) {

            super(new BundledEdgeVisualization());

            // set up visualization
            m_vis.add(GRAPH, g);
            m_vis.setInteractive(EDGES, null, false);
            vis = m_vis;

            // draw the "name" label for NodeItems
            m_nodeRenderer = new LabelRenderer(NODE_NAME);
            m_nodeRenderer
                    .setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);

            edgeRenderer = new BundledEdgeRenderer(VizUtils.BSPLINE_EDGE_TYPE);

            DefaultRendererFactory rf = new DefaultRendererFactory(
                    m_nodeRenderer);
            rf.add(new InGroupPredicate(EDGES), edgeRenderer);
            m_vis.setRendererFactory(rf);

            // create stroke for drawing nodes
            ColorAction nStroke = new ColorAction(NODES,
                    VisualItem.STROKECOLOR, ColorLib.gray(100));

            // use black for node text
            ColorAction text = new ColorAction(NODES, VisualItem.TEXTCOLOR,
                    ColorLib.gray(100));
            text.add("_hover", ColorLib.rgb(255, 0, 0));

            ColorAction fill = new ColorAction(NODES, VisualItem.FILLCOLOR,
                    ColorLib.gray(200));
            fill.add("_hover", ColorLib.gray(220, 230));

            // create an action list containing all color assignments
            ActionList color = new ActionList();
            color.add(nStroke);
            color.add(text);
            color.add(fill);

            m_vis.putAction("color", color);

            // create the radial tree layout action
            radialTreeLayout = new RadialTreeLayout(GRAPH);
            m_vis.putAction(RADIAL_TREE_LAYOUT, radialTreeLayout);
            lastSelectedLayout = radialTreeLayout;

            // create the tree layout action
            treeLayout = new NodeLinkTreeLayout(GRAPH,
                    Constants.ORIENT_TOP_BOTTOM, 200, 3, 20);
            m_vis.putAction(TREE_LAYOUT, treeLayout);

            // initialize the display
            setSize(600, 600);
            setAlignmentX(SwingConstants.CENTER);
            setItemSorter(new TreeDepthItemSorter());
            addControlListener(new DragControl());
            addControlListener(new ZoomToFitControl());
            addControlListener(new ZoomControl());
            addControlListener(new PanControl());
            setDamageRedraw(false);
            addControlListener(new HoverActionControl("repaint"));
            addControlListener(new DisplayControlAdaptor(vis));

            computeVisualParameters(edgeRenderer);
            // color the graph and perform layout
            m_vis.run("color");
            m_vis.run(RADIAL_TREE_LAYOUT);

        }
    }

    // reads the graph from a GraphML file and loads it into the visualization
    public void initializeGraph(String datafile, String label) {
        Graph graph = null;
        try {
            graph = new GraphMLReader().readGraph(datafile);
        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Error loading graph.");
            System.exit(1);
        }

        try {
            // create a new radial tree view
            if (graphDisplay == null) {
                graphDisplay = new RadialGraphView(graph, label);
                vis = graphDisplay.getVisualization();
            } else {

                vis.addGraph(GRAPH, graph);
            }

        } catch (IllegalArgumentException exc) {

            // an exception will be thrown when the GRAPH group already exists
            // in the visualization. In this case, remove the existing data
            // before adding new information

            vis.removeGroup(EDGES);
            vis.removeGroup(NODES);
            vis.removeGroup(GRAPH);
            vis.add(GRAPH, graph);
        }
    }

    // redoes the layout and assigns edge colors and alphas.
    // It is called during the simulation when new data is loaded
    public void computeVisualParameters(BundledEdgeRenderer edgeRenderer) {
        // vis.run("color"); // assign the colors
        if (lastSelectedLayout == radialTreeLayout) {
            vis.run(RADIAL_TREE_LAYOUT); // start up the tree layout
        } else {
            vis.run(TREE_LAYOUT);
        }

        // recompute spanning tree based on the new layout
        TupleSet ts = vis.getGroup(GRAPH);
        if (ts instanceof Graph) {
            tree = ((Graph) ts).getSpanningTree();
        }

        // pass the new spanning tree reference to the renderer for later use
        edgeRenderer.setSpanningTree(tree);

        // set the fillColor for the nodes
        // assignNodeColours(tree);

        // compute alphas for the edges, according to their length
        VizUtils.computeEdgeAlphas(vis, tree);
    }

    public JPanel createVisualizationPanels(String datafile, final String label) {

        initializeGraph(datafile, label);

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        graphDisplay.addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                if (item.canGetString(label))
                    title.setText(item.getString(label));
            }

            public void itemExited(VisualItem item, MouseEvent e) {
                title.setText(null);
            }
        });

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalStrut(3));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanels(), BorderLayout.NORTH);
        panel.add(graphDisplay, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        return panel;
    }

    // creates the panels and the controls that are used to customize the
    // visualization
    private JPanel createControlPanels() {

        JPanel topPanel = new JPanel();
        BoxLayout blayout = new BoxLayout(topPanel, BoxLayout.LINE_AXIS);
        topPanel.setLayout(blayout);
        topPanel.add(Box.createRigidArea(new Dimension(30, 30)));

        // initialize left panel
        JPanel verticalpaJPanel = new JPanel();
        BoxLayout verticalLayout = new BoxLayout(verticalpaJPanel,
                BoxLayout.PAGE_AXIS);
        verticalpaJPanel.setLayout(verticalLayout);

        // checkbox to add / remove shared ancestor
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Remove shared ancestor:"));
        cbox = new JCheckBox();
        cbox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                edgeRenderer.setRemoveSharedAncestor(cbox.isSelected());
                VizUtils.forceEdgeUpdate(vis);
                vis.repaint();
            }
        });
        panel.add(cbox);
        verticalpaJPanel.add(panel);

        // color picker for edge start color
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Start color:"));
        buttonStart = new JButton("  ");
        buttonStart.setBackground(VizUtils.DEFAULT_START_COLOR);
        buttonStart.setFocusPainted(false);
        buttonStart.addActionListener(new ButtonActionListener());
        buttonStart.setToolTipText("Click to select color");
        panel.add(buttonStart);

        // color picker for edge stop color
        panel.add(new JLabel("Stop color:"));
        buttonStop = new JButton("  ");
        buttonStop.setBackground(VizUtils.DEFAULT_STOP_COLOR);
        buttonStop.setFocusPainted(false);
        buttonStop.addActionListener(new ButtonActionListener());
        buttonStop.setToolTipText("Click to select color");
        panel.add(buttonStop);

        verticalpaJPanel.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // button for managing simulation
        final JButton refreshDataButton = new JButton("Start simulation");
        refreshDataButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // if (simulationTimer.isRunning()) {
                // simulationTimer.stop();
                // refreshDataButton.setText("Start simulation");
                // } else {
                // simulationTimer.start();
                // refreshDataButton.setText("Stop simulation");
                // }
            }
        });

        panel.add(refreshDataButton);
        verticalpaJPanel.add(panel);

        // the left panel is initialized, so add it to the main panel
        topPanel.add(verticalpaJPanel);

        topPanel.add(new JSeparator(JSeparator.VERTICAL));

        // initialize right panel
        verticalpaJPanel = new JPanel();
        verticalLayout = new BoxLayout(verticalpaJPanel, BoxLayout.PAGE_AXIS);
        verticalpaJPanel.setLayout(verticalLayout);

        // slider for changing the bundling factor
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Change bundling factor:"));
        slider = new JSlider(0, 20,
                (int) (20 * VizUtils.INITIAL_BUNDLING_FACTOR));
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent event) {
                edgeRenderer.setBundlingFactor(slider.getValue() / 20.0);
                VizUtils.forceEdgeUpdate(vis);
                vis.repaint();
            }
        });
        panel.add(slider);

        verticalpaJPanel.add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Layout:"));

        // radio buttons for switching between layouts
        ButtonGroup radioGroup = new ButtonGroup();
        JRadioButton circleRadio = new JRadioButton("Radial tree");
        radioGroup.add(circleRadio);
        circleRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    vis.run(RADIAL_TREE_LAYOUT); // start up the tree layout
                    lastSelectedLayout = radialTreeLayout;
                } catch (ClassCastException exc) {
                    exc.printStackTrace();
                }
                VizUtils.forceEdgeUpdate(vis);
                vis.repaint();
            }
        });
        circleRadio.setSelected(true);
        panel.add(circleRadio);

        JRadioButton treeRadio = new JRadioButton("Tree");
        radioGroup.add(treeRadio);
        treeRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                vis.run(TREE_LAYOUT);
                lastSelectedLayout = treeLayout;
                VizUtils.forceEdgeUpdate(vis);
                vis.repaint();
            }
        });
        panel.add(treeRadio);
        verticalpaJPanel.add(panel);

        // radio buttons for switching between edge color encoding methods:
        // based on edge weight or based on start and end nodes
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Edge color encoding:"));

        ButtonGroup colorRadioGroup = new ButtonGroup();
        JRadioButton weightRadio = new JRadioButton("Edge weight");
        colorRadioGroup.add(weightRadio);
        weightRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                edgeRenderer.setColorEncoding(true);
                vis.repaint();
            }
        });
        weightRadio.setSelected(true);
        panel.add(weightRadio);

        JRadioButton startEndRadio = new JRadioButton("Start to end node");
        colorRadioGroup.add(startEndRadio);
        startEndRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                edgeRenderer.setColorEncoding(false);
                vis.repaint();
            }
        });
        panel.add(startEndRadio);
        verticalpaJPanel.add(panel);

        topPanel.add(verticalpaJPanel);

        return topPanel;
    }

    // displays the color chooser
    private void showColorChooser(Color color) {
        if (chooser == null && colorDialog == null) {
            chooser = new JColorChooser();
            colorDialog = JColorChooser.createDialog(vizPanel, "Choose color",
                    true, chooser, new DialogActionlistener(), null);
        }

        chooser.setColor(color);
        colorDialog.setVisible(true);
    }

    // action listener for the color choosing button
    private class ButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            lastSelectedButton = (JButton) event.getSource();
            if (lastSelectedButton == buttonStart) {
                showColorChooser(edgeRenderer.getStartColor());
            } else {
                showColorChooser(edgeRenderer.getStopColor());
            }
        }
    }

    // action listener the color chooser dialog
    private class DialogActionlistener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Color newcolor = chooser.getColor();
            lastSelectedButton.setBackground(newcolor);
            if (lastSelectedButton == buttonStart) {
                edgeRenderer.setStartColor(newcolor);
            } else {
                edgeRenderer.setStopColor(newcolor);
            }
            vis.repaint();
        }
    }
}

package ibis.deploy.gui.deployViz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Timer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ibis.deploy.gui.deployViz.edgeBundles.*;
import ibis.deploy.gui.deployViz.helpers.VizUtils;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class Main {

    private static Visualization vis;
    private static Tree tree = null;

    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String AGGR = "aggregates";
    public static final String NODE_NAME = "name";
    public static final String NODE_TYPE = "type";

    public static final String TREE_LAYOUT = "treeLayout";
    public static final String RADIAL_TREE_LAYOUT = "radialTreeLayout";

    private JFrame frame;
    private BundledEdgeRenderer edgeRenderer;
    private JSlider slider;
    private JCheckBox cbox;
    private JColorChooser chooser = null;
    private JDialog colorDialog = null;
    private JButton lastSelectedButton = null, buttonStart = null,
            buttonStop = null;

    private HashMap<String, Color> nodeColorMap;

    private Timer simulationTimer;
    private Action updateVisualizationAction;

    private TreeLayout lastSelectedLayout = null;
    private RadialTreeLayout radialTreeLayout;
    private NodeLinkTreeLayout treeLayout;

    public Main() {
    }

    public static void main(String arg[]) {
        Main visualization = new Main();
        visualization.initGUI();
    }

    // reads the graph from a GraphML file and loads it into the visualization
    public void readGraph() {
        Graph graph = null;
        try {
            // InputDataGenerator.generateInputFile();
            graph = new GraphMLReader().readGraph("images/das3.xml");
            // } catch (IOException e) {
            // System.err.println("Problem generating input file.");
        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Error loading graph.");
            System.exit(1);
        }

        try {
            vis.addGraph(GRAPH, graph);
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
        vis.run("color"); // assign the colors
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
        assignNodeColours(tree);

        // compute alphas for the edges, according to their length
        VizUtils.computeEdgeAlphas(vis, tree);
    }

    public void initGUI() {

        vis = new BundledEdgeVisualization();

        // read the graph data and add it to the visualization
        readGraph();

        // draw the "name" label for NodeItems
        LabelRenderer nodeRenderer = new LabelRenderer(NODE_NAME);
        nodeRenderer.setRenderType(LabelRenderer.RENDER_TYPE_DRAW_AND_FILL);

        // use custom edge renderer to draw curved edges
        edgeRenderer = new BundledEdgeRenderer(
        // Constants.EDGE_TYPE_CURVE, tree);
                VizUtils.BSPLINE_EDGE_TYPE);

        // create a new default renderer factory and initialize it
        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(nodeRenderer);
        drf.setDefaultEdgeRenderer(edgeRenderer);

        // add the renderer factory to the visualization
        vis.setRendererFactory(drf);

        // create stroke for drawing nodes
        ColorAction nStroke = new ColorAction(NODES, VisualItem.STROKECOLOR);
        nStroke.setDefaultColor(ColorLib.gray(100));

        // use black for node text
        ColorAction text = new ColorAction(NODES, VisualItem.TEXTCOLOR,
                ColorLib.gray(50));

        // create an action list containing all color assignments
        ActionList color = new ActionList();
        color.add(nStroke);
        color.add(text);

        // add the action list to the visualization
        vis.putAction("color", color);

        // create the radial tree layout action
        radialTreeLayout = new RadialTreeLayout(GRAPH);
        vis.putAction(RADIAL_TREE_LAYOUT, radialTreeLayout);
        lastSelectedLayout = radialTreeLayout;

        // create the tree layout action
        treeLayout = new NodeLinkTreeLayout(GRAPH, Constants.ORIENT_TOP_BOTTOM,
                200, 3, 20);
        vis.putAction(TREE_LAYOUT, treeLayout);

        // create a new Display
        Display display = new Display(vis);
        display.setSize(700, 500); // set display size
        display.addControlListener(new DragControl(true)); // drag items around
        display.addControlListener(new PanControl()); // pan
        display.addControlListener(new ZoomControl()); // zoom
        display.setHighQuality(true);
        display.setDamageRedraw(false);
        display.addControlListener(new DisplayControlAdaptor(vis));

        // create a new window to hold the visualization
        frame = new JFrame("Edge bundles");

        // ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout lmanager = new BorderLayout();
        frame.setLayout(lmanager);

        // create the panels together with the controls
        createPanels(frame);

        frame.add(display, BorderLayout.CENTER);
        frame.add(new JLabel("Use the left mouse button to pan "
                + "and right mouse button to zoom"), BorderLayout.SOUTH);

        computeVisualParameters(edgeRenderer);

        frame.pack(); // layout components in window
        frame.setVisible(true); // show the window

        // create an Action element that is triggered during simulation
        updateVisualizationAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                readGraph();
                computeVisualParameters(edgeRenderer);
                vis.repaint();
            }

        };

        // create the timer that will trigger the data refresh
        // during the simulation
        simulationTimer = new Timer(500, updateVisualizationAction);
    }

    // creates the panels and the controls that are used to customize the
    // visualization
    private void createPanels(JFrame frame) {

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
                if (simulationTimer.isRunning()) {
                    simulationTimer.stop();
                    refreshDataButton.setText("Start simulation");
                } else {
                    simulationTimer.start();
                    refreshDataButton.setText("Stop simulation");
                }
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

        frame.add(topPanel, BorderLayout.NORTH);
    }

    // displays the color chooser
    private void showColorChooser(Color color) {
        if (chooser == null && colorDialog == null) {
            chooser = new JColorChooser();
            colorDialog = JColorChooser.createDialog(frame, "Choose color",
                    true, chooser, new DialogActionlistener(), null);
        }

        chooser.setColor(color);
        colorDialog.setVisible(true);
    }

    // assigns node colors based on the cluster they are in.
    // At the moment, the colors are picked at random from a list of available
    // colors
    @SuppressWarnings("unchecked")
	private void assignNodeColours(Tree tree) {
        NodeItem item, parent = null;

        String nextColor;
        Color color;

        ArrayList<VisualItem> mainNodes = new ArrayList<VisualItem>();
        if (nodeColorMap == null) {
            nodeColorMap = new HashMap<String, Color>();
        }

        // add nodes to aggregates
        Iterator<NodeItem> nodes = vis.visibleItems(NODES);

        while (nodes.hasNext()) {
            item = nodes.next();

            // pick the color at random if it's a head node
            if (item.getString(NODE_TYPE).equals(VizUtils.NODE_TYPE_SITE_NODE)) {
                if (!mainNodes.contains(item)) {
                    mainNodes.add(item);

                    // check if the node already had a color assigned to it or
                    // not
                    if (!nodeColorMap.containsKey(item.getString(NODE_NAME))) {
                        nextColor = VizUtils.getRandomColor();
                        color = Color.decode(nextColor).brighter();
                        nodeColorMap.put(item.getString(NODE_NAME), color);
                    } else {
                        // just use the existing color
                        color = nodeColorMap.get(item.getString(NODE_NAME));
                    }
                    item.setFillColor(ColorLib.color(color));
                }
            } else {
                // in the case of compute nodes, look at the color of the parent
                // first
                parent = (NodeItem) tree.getParent((NodeItem) item);

                // if the parent is defined and has a color assigned, use this
                // color
                if (parent != null
                        && parent.getString(NODE_TYPE).equals(
                                VizUtils.NODE_TYPE_SITE_NODE)) {

                    int idx = mainNodes.indexOf(parent);
                    if (idx < 0) { // the parent is not in the list
                        mainNodes.add(parent);
                        if (!nodeColorMap
                                .containsKey(item.getString(NODE_NAME))) {
                            nextColor = VizUtils.getRandomColor();
                            color = Color.decode(nextColor).brighter();
                            nodeColorMap.put(item.getString(NODE_NAME), color);
                        } else {
                            color = nodeColorMap.get(item.getString(NODE_NAME));
                        }
                        item.setFillColor(ColorLib.color(color));
                    }
                    item.setFillColor(parent.getFillColor());
                } else { // the cluster node
                    // TODO find a more appropriate color for the main node,
                    // maybe draw it differently
                    item.setFillColor(ColorLib.hex("#00FFFF"));
                }
            }
        }
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
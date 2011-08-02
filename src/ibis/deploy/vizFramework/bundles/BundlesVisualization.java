package ibis.deploy.vizFramework.bundles;

import ibis.deploy.gui.GUI;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ibis.deploy.vizFramework.IVisualization;
import ibis.deploy.vizFramework.bundles.data.GraphGenerator;
import ibis.deploy.vizFramework.bundles.edgeBundles.BundledEdgeRenderer;
import ibis.deploy.vizFramework.bundles.edgeBundles.BundlesPrefuseVisualization;
import ibis.deploy.vizFramework.bundles.edgeBundles.DisplayControlAdapter;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;
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
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ui.JFastLabel;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

public class BundlesVisualization extends JPanel implements IVisualization {

    private static final long serialVersionUID = 1L;

    private static Visualization vis;
    private static Tree tree = null;

    public static final String TREE_LAYOUT = "treeLayout";
    public static final String RADIAL_TREE_LAYOUT = "radialTreeLayout";

    private EdgeRenderer edgeRenderer;
    private JSlider slider;
    private JCheckBox cbox;
    private JColorChooser chooser = null;
    private JDialog colorDialog = null;
    private JButton lastSelectedButton = null, buttonStart = null,
            buttonStop = null;

    private TreeLayout lastSelectedLayout = null;
    private RadialTreeLayout radialTreeLayout;
    private NodeLinkTreeLayout treeLayout;
    private RadialGraphDisplay radialGraphDisplay;
    private JPanel vizPanel;
    
    private GraphGenerator generator;

    public BundlesVisualization(GUI gui) {

        setLayout(new BorderLayout());
        vizPanel = new JPanel(new BorderLayout());

        // create the graph generator - this needs to be created before the
        // visualization
        generator = new GraphGenerator(gui);

        // the panels are created and so is the Visualization object
        String label = "name";
        vizPanel.add(createVisualizationPanels(label), BorderLayout.CENTER);

        add(vizPanel, BorderLayout.CENTER);
    }

    public void setCollectData(boolean collect) {
        // dataCollector.setCollectingState(collect);
    }

    public synchronized void updateVisualization(
            HashMap<String, Set<String>> ibisesPerSite,
            HashMap<String, HashMap<String, Double>> edgesPerIbis) {
        // try to update the graph, and only redo the visualization if changes
        // have occurred in the meanwhile
        int result;
        synchronized (vis) { // synchronize on the visualization to make sure
                             // that the layout computation and the graph update
                             // don't occur at the same time
            result = generator.updatePrefuseGraph(ibisesPerSite, edgesPerIbis,
                    vis);
        }
        if (result > GraphGenerator.UPDATE_NONE) {
            if (result == GraphGenerator.UPDATE_REDO_LAYOUT) {
                computeVisualParameters(edgeRenderer, true);
            } else {
                computeVisualParameters(edgeRenderer, false);
            }
            vis.repaint();
        }
    }

    // redoes the layout and assigns edge colors and alphas.
    // It is called when the graph structure changes
    public synchronized void computeVisualParameters(EdgeRenderer edgeRenderer,
            boolean redoLayout) {

        TupleSet ts = vis.getGroup(UIConstants.GRAPH);

        if (ts instanceof Graph) {
            Graph g = (Graph) ts;

            // redo computations only if the graph contains nodes
            if (g.getNodeCount() > 0) {
                vis.run("color"); // assign the colors
                if (redoLayout) {
                    synchronized (vis) { // make sure that this section is
                                         // synchnorized
                        if (lastSelectedLayout == radialTreeLayout) {
                            vis.run(RADIAL_TREE_LAYOUT); 
                        } else {
                            vis.run(TREE_LAYOUT);
                        }

                        // recompute spanning tree based on the new layout
                        if (g.getNodeCount() > 0) {
                            tree = g.getSpanningTree();
                        }

                        // pass the new spanning tree reference to the renderer
                        // for later use
                        if (edgeRenderer instanceof BundledEdgeRenderer) {
                            ((BundledEdgeRenderer) edgeRenderer)
                                    .setSpanningTree(tree);
                        }
                    }
                }
               
                // compute alphas for the edges, according to their length
                Utils.computeEdgeAlphas(vis, tree);

                // if the graph structure changed, it's necessary to update the
                // selections also
                if (radialGraphDisplay != null) {
                    radialGraphDisplay.forceSelectedNodeUpdate();
                }
            }
        }

    }

    public JPanel createVisualizationPanels(final String label) {

        try {
            if (vis == null) {
                vis = new BundlesPrefuseVisualization();
                vis.addGraph(UIConstants.GRAPH, generator.getGraph());
            }

        } catch (IllegalArgumentException exc) {

            System.err
                    .println("An exception occurred while creating the visualization");
        }
        radialGraphDisplay = new RadialGraphDisplay();

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        radialGraphDisplay.addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                if (item instanceof NodeItem) {
                    if (item.canGetString(UIConstants.NODE_NAME)) {
                        title.setText(item.getString(UIConstants.NODE_NAME));
                    }
                }
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
        panel.add(radialGraphDisplay, BorderLayout.CENTER);
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
                if (edgeRenderer instanceof BundledEdgeRenderer) {
                    ((BundledEdgeRenderer) edgeRenderer)
                            .setRemoveSharedAncestor(cbox.isSelected());
                }
                Utils.forceEdgeUpdate(vis);
                vis.repaint();
            }
        });
        panel.add(cbox);
        verticalpaJPanel.add(panel);

        // color picker for edge start color
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Start color:"));
        buttonStart = new JButton("  ");
        buttonStart.setBackground(UIConstants.DEFAULT_START_COLOR);
        buttonStart.setFocusPainted(false);
        buttonStart.addActionListener(new ButtonActionListener());
        buttonStart.setToolTipText("Click to select color");
        panel.add(buttonStart);

        // color picker for edge stop color
        panel.add(new JLabel("Stop color:"));
        buttonStop = new JButton("  ");
        buttonStop.setBackground(UIConstants.DEFAULT_STOP_COLOR);
        buttonStop.setFocusPainted(false);
        buttonStop.addActionListener(new ButtonActionListener());
        buttonStop.setToolTipText("Click to select color");
        panel.add(buttonStop);

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
                (int) (20 * UIConstants.INITIAL_BUNDLING_FACTOR));
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent event) {
                if (edgeRenderer instanceof BundledEdgeRenderer) {
                    ((BundledEdgeRenderer) edgeRenderer)
                            .setBundlingFactor(slider.getValue() / 20.0);
                }
                Utils.forceEdgeUpdate(vis);
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
                    synchronized (vis) {
                        vis.run(RADIAL_TREE_LAYOUT); // start up the tree layout
                    }
                    lastSelectedLayout = radialTreeLayout;
                } catch (ClassCastException exc) {
                    exc.printStackTrace();
                }
                Utils.forceEdgeUpdate(vis);
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
                synchronized (vis) {
                    vis.run(TREE_LAYOUT);
                }
                lastSelectedLayout = treeLayout;
                Utils.forceEdgeUpdate(vis);
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
                if (edgeRenderer instanceof BundledEdgeRenderer) {
                    ((BundledEdgeRenderer) edgeRenderer).setColorEncoding(true);
                }
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
                if (edgeRenderer instanceof BundledEdgeRenderer) {
                    ((BundledEdgeRenderer) edgeRenderer)
                            .setColorEncoding(false);
                }
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
            if (edgeRenderer instanceof BundledEdgeRenderer) {
                if (lastSelectedButton == buttonStart) {
                    showColorChooser(((BundledEdgeRenderer) edgeRenderer)
                            .getStartColor());
                } else {
                    showColorChooser(((BundledEdgeRenderer) edgeRenderer)
                            .getStopColor());
                }
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
                if (edgeRenderer instanceof BundledEdgeRenderer) {
                    ((BundledEdgeRenderer) edgeRenderer)
                            .setStartColor(newcolor);
                }
            } else {
                if (edgeRenderer instanceof BundledEdgeRenderer) {
                    ((BundledEdgeRenderer) edgeRenderer).setStopColor(newcolor);
                }
            }
            vis.repaint();
        }
    }

    class RadialGraphDisplay extends Display {

        private static final long serialVersionUID = 1L;
        private LabelRenderer m_nodeRenderer;
        private DisplayControlAdapter displayAdaptor;

        public RadialGraphDisplay() {

            super(vis);
            m_vis.setInteractive(UIConstants.EDGES, null, false);

            // draw the "name" label for NodeItems
            m_nodeRenderer = new LabelRenderer(UIConstants.NODE_NAME);
            m_nodeRenderer
                    .setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
            m_nodeRenderer.setRoundedCorner(7, 7);

            edgeRenderer = new BundledEdgeRenderer(
                    UIConstants.BSPLINE_EDGE_TYPE);

            DefaultRendererFactory rf = new DefaultRendererFactory(
                    m_nodeRenderer);
            rf.add(new InGroupPredicate(UIConstants.EDGES), edgeRenderer);
            m_vis.setRendererFactory(rf);

            // create stroke for drawing nodes
            ColorAction nStroke = new ColorAction(UIConstants.NODES,
                    VisualItem.STROKECOLOR, ColorLib.gray(100));

            // use black for node text
            ColorAction text = new ColorAction(UIConstants.NODES,
                    VisualItem.TEXTCOLOR, UIConstants.DEFAULT_TEXT_COLOR);

            // use this to color the root node
            ColorAction fill = new ColorAction(UIConstants.NODES,
                    VisualItem.FILLCOLOR, UIConstants.DEFAULT_ROOT_NODE_COLOR);

            ColorAction edges = new ColorAction(UIConstants.EDGES,
                    VisualItem.STROKECOLOR, ColorLib.hex("00FF00"));


            // create an action list containing all color assignments
            ActionList initialColor = new ActionList();
            initialColor.add(text);
            initialColor.add(fill);
            // initialColor.add(edges);

            m_vis.putAction("initialColor", initialColor);
            m_vis.putAction("color", nStroke);

            // create the radial tree layout action
            radialTreeLayout = new RadialTreeLayout(UIConstants.GRAPH);
            m_vis.putAction(RADIAL_TREE_LAYOUT, radialTreeLayout);
            lastSelectedLayout = radialTreeLayout;

            // create the tree layout action
            treeLayout = new NodeLinkTreeLayout(UIConstants.GRAPH,
                    Constants.ORIENT_TOP_BOTTOM, 200, 3, 3);
            m_vis.putAction(TREE_LAYOUT, treeLayout);

            // initialize the display
            setAlignmentX(SwingConstants.CENTER);
            setAlignmentY(SwingConstants.CENTER);
            setHighQuality(true);
            setItemSorter(new TreeDepthItemSorter());
            addControlListener(new DragControl());
            addControlListener(new ZoomToFitControl());
            addControlListener(new ZoomControl());
            addControlListener(new PanControl());
            setDamageRedraw(false);
            addControlListener(new HoverActionControl("repaint"));
            addControlListener(displayAdaptor = new DisplayControlAdapter(m_vis));

            computeVisualParameters(edgeRenderer, true);
            // color the graph and perform layout
            m_vis.run("initialColor");
//
//            //TODO - here we measure time it takes to draw
//            addPaintListener(new DebugStatsPainter());
           
        }

        public void forceSelectedNodeUpdate() {
            displayAdaptor.forceSelectedNodeUpdate();
        }
    }
}
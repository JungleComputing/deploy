package ibis.deploy.gui.deployViz.data;

import ibis.deploy.Grid;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.deployViz.helpers.VizUtils;
import ibis.deploy.gui.misc.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;

public class GraphGenerator {

    /** Node table schemas used for generated Graphs */
    public final Schema NAME_SCHEMA = new Schema();
    public final Schema TYPE_SCHEMA = new Schema();
    public final Schema WEIGHT_SCHEMA = new Schema();

    private HashMap<String, Node> nodeMap = new HashMap<String, Node>();
    private Graph internalGraph;
    private Node root;
    private Grid grid;

    public GraphGenerator(GUI gui) {
        // initialize the schemas
        NAME_SCHEMA.addColumn(VizUtils.NODE_NAME, String.class, "");
        TYPE_SCHEMA.addColumn(VizUtils.NODE_TYPE, String.class, "");
        WEIGHT_SCHEMA.addColumn(VizUtils.WEIGHT, int.class, 0);

        internalGraph = new Graph();
        internalGraph.getNodeTable().addColumns(NAME_SCHEMA);
        internalGraph.getNodeTable().addColumns(TYPE_SCHEMA);
        internalGraph.getEdgeTable().addColumns(WEIGHT_SCHEMA);

        // create root node - any graph will have this, including an empty
        // one
        root = internalGraph.addNode();
        root.set(VizUtils.NODE_NAME, "Ibis Deploy");
        root.set(VizUtils.NODE_TYPE, VizUtils.NODE_TYPE_ROOT_NODE);
        nodeMap.put("Ibis Deploy", root);

        grid = gui.getWorkSpace().getGrid();
    }

    public Graph getGraph() {
        return internalGraph;
    }

    public boolean updatePrefuseGraph(
            HashMap<String, Set<String>> ibisesPerSite, Visualization vis) {

        Node tempNode, siteNode, ibisNode;
        String siteName;

        boolean situationChanged = false;

        if (ibisesPerSite == null) {
            situationChanged = true;
        } else {

            // remove the sites which are no longer active from the nodeMap
            Set<String> nodeKeys = nodeMap.keySet();
            ArrayList<String> nodesToRemove = new ArrayList<String>();

            // remove all the nodes which no longer exist
            for (String nodeName : nodeKeys) {

                tempNode = nodeMap.get(nodeName);

                // remove all ibis nodes which are no longer up-to-date
                if (tempNode.getString(VizUtils.NODE_TYPE).equals(
                        VizUtils.NODE_TYPE_IBIS_NODE)) {

                    // get the name of the location - that's the name of the
                    // parent node
                    if (tempNode.getParent() != null) {
                        siteName = tempNode.getParent().getString(
                                VizUtils.NODE_NAME);
                        if (ibisesPerSite.get(siteName) == null
                                || !ibisesPerSite.get(siteName).contains(
                                        nodeName)) {
                            nodesToRemove.add(nodeName);
                            // System.out
                            // .println("Ibis removed!! --->" + nodeName);
                        }
                    }
                }

                // remove all the site nodes which are no longer up-to-date
                if (tempNode.getString(VizUtils.NODE_TYPE).equals(
                        VizUtils.NODE_TYPE_SITE_NODE)) {
                    if (!ibisesPerSite.containsKey(nodeName)) {
                        nodesToRemove.add(nodeName);
                        // System.out.println("Location removed!!----> "
                        // + nodeName);
                    }
                }

            }

            // remove all the marked nodes from the map and from the actual
            // graph
            if (nodesToRemove.size() > 0) {
                situationChanged = true;
                for (String nodeName : nodesToRemove) {
                    tempNode = nodeMap.remove(nodeName);
                    internalGraph.removeNode(tempNode);
                }
                nodesToRemove.clear();
            }

            // check the other way round too - if the new hash map contains new
            // sites or ibises, the situation has changed and we need to update
            // the graph
            for (String site : ibisesPerSite.keySet()) {
                if (!nodeMap.containsKey(site)) {

                    situationChanged = true;
                    // System.out.println("New location!! --> " + site);

                    siteNode = addNodeToGraph(site, root,
                            VizUtils.NODE_TYPE_SITE_NODE);
                    assignNodeColor(siteNode, null, vis);

                } else {
                    siteNode = nodeMap.get(site);
                }

                if (ibisesPerSite.get(site) != null) {
                    for (String ibisName : ibisesPerSite.get(site)) {
                        if (!nodeMap.containsKey(ibisName)) {

                            situationChanged = true;
                            // System.out.println("New ibis!!--> " + ibisName);

                            ibisNode = addNodeToGraph(ibisName, siteNode,
                                    VizUtils.NODE_TYPE_IBIS_NODE);
                            assignNodeColor(ibisNode, siteNode, vis);
                        }
                    }
                }
            }
        }

        return situationChanged;
    }

    private Node addNodeToGraph(String name, Node parent, String type) {
        Node newNode;
        Edge edge;

        newNode = internalGraph.addNode();
        newNode.set(VizUtils.NODE_NAME, name);
        newNode.set(VizUtils.NODE_TYPE, type);
        nodeMap.put(name, newNode);
        edge = internalGraph.addEdge(parent, newNode);
        edge.setInt(VizUtils.WEIGHT, VizUtils.DEFAULT_WEIGHT);

        return newNode;
    }

    private void assignNodeColor(Node node, Node parent, Visualization vis) {
        NodeItem visualItem;
        String colorCode;
        int color;

        visualItem = (NodeItem) vis.getVisualItem(VizUtils.NODES, node);

        if (visualItem.getString(VizUtils.NODE_TYPE).equals(
                VizUtils.NODE_TYPE_SITE_NODE)) {
            colorCode = grid.getCluster(
                    visualItem.getString(VizUtils.NODE_NAME)).getColorCode();
            color = Utils.getColor(colorCode).getRGB();
            visualItem.setFillColor(color);
            visualItem.setStartFillColor(color);
            visualItem.setTextColor(VizUtils.DEFAULT_TEXT_COLOR);
        } else if (visualItem.getString(VizUtils.NODE_TYPE).equals(
                VizUtils.NODE_TYPE_IBIS_NODE)) {
            // we've already received the parent as a parameter, no need to
            // retrieve it again
            // parent = visualItem.getParent();
            if (parent != null) {
                colorCode = grid.getCluster(
                        parent.getString(VizUtils.NODE_NAME)).getColorCode();
                color = Utils.getColor(colorCode).getRGB();
                visualItem.setFillColor(color);
                visualItem.setStartFillColor(color);
                visualItem.setTextColor(VizUtils.DEFAULT_TEXT_COLOR);
            }
        } else {
            visualItem.setFillColor(ColorLib.gray(200));
            visualItem.setStartFillColor(ColorLib.gray(200));
            visualItem.setTextColor(VizUtils.DEFAULT_TEXT_COLOR);
        }

    }
}

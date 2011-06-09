package ibis.deploy.vizFramework.bundles.data;

import ibis.deploy.Grid;
import ibis.deploy.gui.GUI;
import ibis.deploy.util.Colors;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    public static final int UPDATE_REDO_LAYOUT = 2;
    public static final int UPDATE_JUST_REPAINT = 1;
    public static final int UPDATE_NONE = 0;

    private HashMap<String, Node> nodeMap = new HashMap<String, Node>();
    private HashMap<String, HashMap<String, Edge>> edgeMap = new HashMap<String, HashMap<String, Edge>>();
    private Graph internalGraph;
    private Node root;
    private Grid grid;

    public GraphGenerator(GUI gui) {
        // initialize the schemas
        NAME_SCHEMA.addColumn(UIConstants.NODE_NAME, String.class, "");
        TYPE_SCHEMA.addColumn(UIConstants.NODE_TYPE, String.class, "");
        WEIGHT_SCHEMA.addColumn(UIConstants.WEIGHT, long.class, 0);

        internalGraph = new Graph();
        internalGraph.getNodeTable().addColumns(NAME_SCHEMA);
        internalGraph.getNodeTable().addColumns(TYPE_SCHEMA);
        internalGraph.getEdgeTable().addColumns(WEIGHT_SCHEMA);

        // create root node - any graph will have this, including an empty
        // one
        root = internalGraph.addNode();
        root.set(UIConstants.NODE_NAME, "IbisDeploy");
        root.set(UIConstants.NODE_TYPE, UIConstants.NODE_TYPE_ROOT_NODE);
        nodeMap.put("IbisDeploy", root);

        grid = gui.getWorkSpace().getGrid();
    }

    public Graph getGraph() {
        return internalGraph;
    }

    public int updatePrefuseGraph(HashMap<String, Set<String>> ibisesPerSite,
            HashMap<String, HashMap<String, Double>> edgesPerIbis,
            Visualization vis) {

        Node siteNode, ibisNode, startNode, stopNode;

        int currentSituation = UPDATE_NONE;

        if (ibisesPerSite == null) {
            currentSituation = UPDATE_REDO_LAYOUT;
        } else {

            currentSituation = removeOldNodes(ibisesPerSite);

            // check the other way round too - if the new hash map contains new
            // sites or ibises, the situation has changed and we need to update
            // the graph
            for (String site : ibisesPerSite.keySet()) {
                if (!nodeMap.containsKey(site)) {

                    currentSituation = UPDATE_REDO_LAYOUT;

                    siteNode = addNodeToGraph(site, root,
                            UIConstants.NODE_TYPE_SITE_NODE);
                    assignNodeColor(siteNode, null, vis);

                } else {
                    siteNode = nodeMap.get(site);
                }

                if (ibisesPerSite.get(site) != null) {
                    for (String ibisName : ibisesPerSite.get(site)) {
                        if (!nodeMap.containsKey(ibisName)) {

                            currentSituation = UPDATE_REDO_LAYOUT;

                            ibisNode = addNodeToGraph(ibisName, siteNode,
                                    UIConstants.NODE_TYPE_IBIS_NODE);
                            assignNodeColor(ibisNode, siteNode, vis);
                        }
                    }
                }
            }
        }

        HashMap<String, Double> neighbours;
        HashMap<String, Edge> tempHash;
        Edge newEdge;
        boolean edgesChanged = false;

        if (edgesPerIbis != null) {
            for (String startNodeName : edgesPerIbis.keySet()) {
                startNode = nodeMap.get(startNodeName);
                neighbours = edgesPerIbis.get(startNodeName);

                // first remove the edges which are no longer in the graph
                edgesChanged = removeOldEdges(startNodeName, neighbours);

                // only continue if the node is still in the graph
                if (startNode != null) {

                    if (neighbours != null) {

                        // now update the remaining edges or add new ones
                        for (String stopNodeName : neighbours.keySet()) {
                            stopNode = nodeMap.get(stopNodeName);

                            newEdge = null;

                            // only continue if the second node is also still in
                            // the graph
                            if (stopNode != null) {

                                // first check if there is already an edge in
                                // the graph between these nodes
                                if (edgeMap.get(startNodeName) != null) {
                                    newEdge = edgeMap.get(startNodeName).get(
                                            stopNodeName);
                                }

                                // check in the other direction also
                                if (newEdge == null
                                        && edgeMap.get(stopNodeName) != null) {
                                    newEdge = edgeMap.get(stopNodeName).get(
                                            startNodeName);
                                }

                                // if there really isn't any edge, create a new
                                // one
                                if (newEdge == null) {
                                    newEdge = internalGraph.addEdge(startNode,
                                            stopNode);

                                    // add the new edge to the hash map
                                    if (edgeMap.get(startNodeName) == null) {
                                        tempHash = new HashMap<String, Edge>();
                                        edgeMap.put(startNodeName, tempHash);
                                    } else {
                                        tempHash = edgeMap.get(startNodeName);
                                    }
                                    tempHash.put(stopNodeName, newEdge);
                                }

                                if (newEdge != null
                                        && internalGraph.containsTuple(newEdge)) {
                                    newEdge.setLong(UIConstants.WEIGHT, neighbours
                                            .get(stopNodeName).longValue());
                                    
                                    edgesChanged = true;
                                }
                            }
                        }
                    }
                }
            }

            if (edgesChanged && currentSituation == UPDATE_NONE) {
                currentSituation = UPDATE_JUST_REPAINT;
            }
        }

        return currentSituation;
    }

    @SuppressWarnings("unchecked")
    private int removeOldNodes(HashMap<String, Set<String>> ibisesPerSite) {
        Node tempNode, neighbourNode;
        String siteName;
        HashSet<String> nodesToRemove = new HashSet<String>();
        int currentSituation = UPDATE_NONE;

        // remove all the nodes which no longer exist in the graph
        for (String nodeName : nodeMap.keySet()) {

            tempNode = nodeMap.get(nodeName);

            // when we're dealing with a site node
            if (tempNode.getString(UIConstants.NODE_TYPE).equals(
                    UIConstants.NODE_TYPE_SITE_NODE)) {
                if (!ibisesPerSite.containsKey(nodeName)) {
                    nodesToRemove.add(nodeName);

                    Iterator<Node> nodeIter = tempNode.neighbors();
                    while (nodeIter.hasNext()) {
                        try {
                            neighbourNode = nodeIter.next();
                            if (neighbourNode.getString(UIConstants.NODE_TYPE)
                                    .equals(UIConstants.NODE_TYPE_IBIS_NODE)) {
                                nodesToRemove.add(neighbourNode
                                        .getString(UIConstants.NODE_NAME));
                            }
                        } catch (IllegalArgumentException exc) {
                            System.err.println(exc.getMessage());
                        }
                    }
                }
                
            } else {
                // when we're dealing with a Ibis node
                if (tempNode.getString(UIConstants.NODE_TYPE).equals(
                        UIConstants.NODE_TYPE_IBIS_NODE)) {
                    
                    // get the name of the location - that's the name of the
                    // parent node
                    if (tempNode.getParent() != null) {
                        siteName = tempNode.getParent().getString(
                                UIConstants.NODE_NAME);
                        
                        if (ibisesPerSite.get(siteName) == null
                                || !ibisesPerSite.get(siteName).contains(
                                        nodeName)) {
                            nodesToRemove.add(nodeName);
                        }
                    }
                }
            }
        }

        // remove all the marked nodes from the map and from the actual
        // graph
        if (nodesToRemove.size() > 0) {
            currentSituation = UPDATE_REDO_LAYOUT;
            for (String nodeName : nodesToRemove) {
                // remove the node from the map
                tempNode = nodeMap.remove(nodeName);

                // remove the edges that are connected to it, both from the
                // graph and the map
                removeAllAdjacentEdges(tempNode);

                // remove the node from the graph
                if (tempNode != null && internalGraph.containsTuple(tempNode)) {
                    internalGraph.removeNode(tempNode);
                }
            }
            nodesToRemove.clear();
        }

        return currentSituation;
    }

    // remove the edges that are adjacent to a node, both from the edgemap and
    // from the internalgraph
    private boolean removeAllAdjacentEdges(Node node) {
        if (node != null) {
            String nodeName = node.getString(UIConstants.NODE_NAME);
            String neighbourName;
            Edge reverseEdge;
            if (edgeMap.get(nodeName) != null
                    && edgeMap.get(nodeName).size() > 0) {
                for (Edge edge : edgeMap.get(nodeName).values()) {

                    if (internalGraph.containsTuple(edge)) {
                        // check if the edge is also present in the map for the
                        // adjacent node and remove it if it is
                        neighbourName = edge.getAdjacentNode(node).getString(
                                UIConstants.NODE_NAME);

                        if (edgeMap.get(neighbourName) != null) {
                            reverseEdge = edgeMap.get(neighbourName).remove(
                                    nodeName);

                            // remove reverse edge from the graph
                            if (reverseEdge != null
                                    && internalGraph.containsTuple(reverseEdge)) {
                                internalGraph.removeEdge(reverseEdge);
                            }

                            // remove direct edge from the graph
                            internalGraph.removeEdge(edge);
                        }

                    }
                }
                edgeMap.get(nodeName).clear();
                edgeMap.remove(nodeName);
                return true;
            }
        }
        return false;
    }

    private boolean removeOldEdges(String startNodeName,
            HashMap<String, Double> neighbours) {
        ArrayList<String> dataToRemove = new ArrayList<String>();
        boolean edgesChanged = false;
        Edge edgeToRemove;

        if (edgeMap.get(startNodeName) != null) {
            for (String stopNodeName : edgeMap.get(startNodeName).keySet()) {

                // temporarily store the data to remove in an
                // auxiliary array
                if (neighbours == null || !neighbours.containsKey(stopNodeName)) {
                    dataToRemove.add(stopNodeName);
                }
            }

            // remove the marked data
            for (String stopNodeName : dataToRemove) {
                edgeToRemove = edgeMap.get(startNodeName).remove(stopNodeName);
                // check if that edge is still in the graph
                // before removing it
                if (internalGraph.containsTuple(edgeToRemove)) {
                    internalGraph.removeEdge(edgeToRemove);
                    edgesChanged = true;
                }
            }
        }

        return edgesChanged;
    }

    private Node addNodeToGraph(String name, Node parent, String type) {
        Node newNode;
        Edge edge;

        newNode = internalGraph.addNode();
        newNode.set(UIConstants.NODE_NAME, name);
        newNode.set(UIConstants.NODE_TYPE, type);
        nodeMap.put(name, newNode);
        edge = internalGraph.addEdge(parent, newNode);
        edge.setLong(UIConstants.WEIGHT, UIConstants.DEFAULT_WEIGHT);

        return newNode;
    }

    private void assignNodeColor(Node node, Node parent, Visualization vis) {
        NodeItem visualItem;
        Color colorCode;
        int color;

        visualItem = (NodeItem) vis.getVisualItem(UIConstants.NODES, node);

        if (visualItem.getString(UIConstants.NODE_TYPE).equals(
                UIConstants.NODE_TYPE_SITE_NODE)) {
            if (grid.getCluster(visualItem.getString(UIConstants.NODE_NAME)) != null) {
                colorCode = grid.getCluster(
                        visualItem.getString(UIConstants.NODE_NAME))
                        .getColor();
                color = colorCode.getRGB();
            } else {
                color = Colors.fromLocation(visualItem.getString(UIConstants.NODE_NAME)).getRGB();
                //color = Color.decode(Utils.getRandomColor()).getRGB();
            }

            visualItem.setFillColor(color);
            visualItem.setStartFillColor(color);
            visualItem.setTextColor(UIConstants.DEFAULT_TEXT_COLOR);
        } else if (visualItem.getString(UIConstants.NODE_TYPE).equals(
                UIConstants.NODE_TYPE_IBIS_NODE)) {
            // we've already received the parent as a parameter, no need to
            // retrieve it again
            if (parent != null) {
                if (grid.getCluster(parent.getString(UIConstants.NODE_NAME)) != null) {
                    colorCode = grid.getCluster(
                            parent.getString(UIConstants.NODE_NAME))
                            .getColor();
                    color = colorCode.getRGB();
                } else {
                    color = Colors.fromLocation(parent.getString(UIConstants.NODE_NAME)).getRGB();
                    //color = Color.decode(Utils.getRandomColor()).getRGB();
                }
                visualItem.setFillColor(color);
                visualItem.setStartFillColor(color);
                visualItem.setTextColor(UIConstants.DEFAULT_TEXT_COLOR);
            }
        } else {
            visualItem.setFillColor(ColorLib.gray(200));
            visualItem.setStartFillColor(ColorLib.gray(200));
            visualItem.setTextColor(UIConstants.DEFAULT_TEXT_COLOR);
        }

    }
}

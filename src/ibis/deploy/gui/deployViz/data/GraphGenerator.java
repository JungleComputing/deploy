package ibis.deploy.gui.deployViz.data;

import ibis.deploy.Grid;
import ibis.deploy.gui.deployViz.helpers.VizUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.util.ColorLib;

public class GraphGenerator {


    /** Node table schemas used for generated Graphs */
    public static final Schema NAME_SCHEMA = new Schema();
    public static final Schema TYPE_SCHEMA = new Schema();
    public static final Schema WEIGHT_SCHEMA = new Schema();

    static {
        NAME_SCHEMA.addColumn(VizUtils.NODE_NAME, String.class, "");
        TYPE_SCHEMA.addColumn(VizUtils.NODE_TYPE, String.class, "");
        WEIGHT_SCHEMA.addColumn(VizUtils.WEIGHT, int.class, 0);
    }

    private static HashMap<String, Node> nodeMap = new HashMap<String, Node>();

    private static Graph internalGraph;
    private static Node root;

    // private static HashMap<String, Edge> edgeMap = new HashMap<String,
    // Edge>();

    public static Graph updatePrefuseGraph(
            HashMap<String, Set<String>> ibisesPerSite) {

        Node tempNode, siteNode, ibisNode;
        String siteName;
        Edge edge;

        boolean situationChanged = false;

        // TODO - handle remove operation also for ibises
        // better node management - reuse the nodes instead of creating new ones
        // in the future don't re-create graph, just add and delete nodes from
        // it

        if (internalGraph == null) {
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

        }

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
                        siteName = tempNode.getParent().getString(VizUtils.NODE_NAME);
                        if (ibisesPerSite.get(siteName) == null
                                || !ibisesPerSite.get(siteName).contains(
                                        nodeName)) {
                            nodesToRemove.add(nodeName);
//                            System.out
//                                    .println("Ibis removed!! --->" + nodeName);
                        }
                    }
                }

                // remove all the site nodes which are no longer up-to-date
                if (tempNode.getString(VizUtils.NODE_TYPE).equals(
                        VizUtils.NODE_TYPE_SITE_NODE)) {
                    if (!ibisesPerSite.containsKey(nodeName)) {
                        nodesToRemove.add(nodeName);
//                        System.out.println("Location removed!!----> "
//                                + nodeName);
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
//                    System.out.println("New location!! --> " + site);

                    siteNode = internalGraph.addNode();
                    siteNode.set(VizUtils.NODE_NAME, site);
                    siteNode.set(VizUtils.NODE_TYPE, VizUtils.NODE_TYPE_SITE_NODE);
                    nodeMap.put(site, siteNode);
                    edge = internalGraph.addEdge(siteNode, root);
                    edge.setInt(VizUtils.WEIGHT, VizUtils.DEFAULT_WEIGHT);

                } else {
                    siteNode = nodeMap.get(site);
                }

                if (ibisesPerSite.get(site) != null) {
                    for (String ibisName : ibisesPerSite.get(site)) {
                        if (!nodeMap.containsKey(ibisName)) {
                            
                            situationChanged = true;
//                            System.out.println("New ibis!!--> " + ibisName);

                            ibisNode = internalGraph.addNode();
                            ibisNode.set(VizUtils.NODE_NAME, ibisName);
                            ibisNode.set(VizUtils.NODE_TYPE, VizUtils.NODE_TYPE_IBIS_NODE);
                            nodeMap.put(ibisName, ibisNode);
                            edge = internalGraph.addEdge(siteNode, ibisNode);
                            edge.setInt(VizUtils.WEIGHT, VizUtils.DEFAULT_WEIGHT);
                        }
                    }
                }
            }
        }

        // only return the updated graph if something changed in the meanwhile
        if (situationChanged) {
            return internalGraph;
        }
        return null;
    }
}

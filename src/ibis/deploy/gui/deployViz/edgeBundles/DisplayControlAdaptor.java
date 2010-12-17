package ibis.deploy.gui.deployViz.edgeBundles;

import ibis.deploy.gui.deployViz.helpers.VizUtils;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class DisplayControlAdaptor extends ControlAdapter {

    private NodeItem lastSelectedNode = null;
    private Visualization vis;
    private Graph graph;

    public DisplayControlAdaptor(Visualization vis) {
        this.vis = vis;
        graph = (Graph) vis.getGroup(VizUtils.GRAPH);
    }

    public void itemClicked(VisualItem item, MouseEvent e) {
        highlightSelection(item);
    }

    public void itemDragged(VisualItem item, MouseEvent e) {
        highlightSelection(item);
    }

    /**
     * This function can be used when new data is reloaded, in order to make
     * sure that the last selected node remains properly selected after changes
     * **/
    public void forceSelectedNodeUpdate() {
        highlightSelection(lastSelectedNode);
    }

    @SuppressWarnings("unchecked")
    private void highlightSelection(VisualItem item) {
        BSplineEdgeItem edge;

        // if we've selected a node, highlight that node and the adjacent edges
        // and nodes
        if (item instanceof NodeItem && graph.containsTuple(item)) {
            NodeItem node = (NodeItem) item;
            Iterator<EdgeItem> edgeIter = node.edges();

            resetPreviousSelection(node);

            // temporarily store old color, but only if the node wasn't already
            // selected
            // if (node.getFillColor() != VizUtils.SELECTED_FILL_COLOR) {
            // node.setStartFillColor(node.getFillColor());
            // }
            node.setFillColor(VizUtils.SELECTED_FILL_COLOR);
            node.setTextColor(VizUtils.SELECTED_TEXT_COLOR);

            while (edgeIter.hasNext()) {
                try {
                    edge = (BSplineEdgeItem) edgeIter.next();
                    edge.setStartFillColor(VizUtils.SELECTED_FILL_COLOR);
                    edge.setEndFillColor(VizUtils.SELECTED_FILL_COLOR);
                    edge.setSelected(true);
                    edge.setHighlighted(true);

                    NodeItem nitem = edge.getAdjacentItem(node);
                    // temporarily store the old fill color in here until a new
                    // selection happens
                    // if (nitem.getFillColor() != VizUtils.SELECTED_FILL_COLOR)
                    // {
                    // nitem.setStartFillColor(nitem.getFillColor());
                    // }

                    // change color of the adjacent nodes
                    nitem.setFillColor(VizUtils.SELECTED_FILL_COLOR);
                    nitem.setTextColor(VizUtils.SELECTED_TEXT_COLOR);
                } catch (IllegalArgumentException exc) {
                    System.err.println(exc.getMessage());
                }
            }

            VizUtils.forceEdgeUpdate(vis);

        } else if (lastSelectedNode != null) {
            // if the selection is for something else other than a node, just
            // reset the selection
            resetPreviousSelection(null);
        }
        vis.repaint();
    }

    @SuppressWarnings("unchecked")
    private void resetPreviousSelection(NodeItem newNode) {

        Iterator<NodeItem> nodes = vis.visibleItems(VizUtils.NODES);
        NodeItem node;

        // just reset colors for all the nodes and edges in the graph
        while (nodes.hasNext()) {
            try {
                node = nodes.next();
                // treat the root differently
                if (node.getParent() != null) {
                    node.setFillColor(node.getStartFillColor());
                } else {
                    node.setFillColor(VizUtils.DEFAULT_ROOT_NODE_COLOR);
                }
                node.setTextColor(VizUtils.DEFAULT_TEXT_COLOR);
            } catch (IllegalArgumentException exc) {
                System.err.println(exc.getMessage());
            }
        }

        Iterator<EdgeItem> edges = vis.visibleItems(VizUtils.EDGES);
        BSplineEdgeItem edge;

        while (edges.hasNext()) {
            try {
                edge = (BSplineEdgeItem) edges.next();
                edge.setSelected(false);
                edge.setHighlighted(false);
            } catch (IllegalArgumentException exc) {
                System.err.println(exc.getMessage());
            }
        }

        // check if there is a selection and that the selection is still present
        // in the graph
        // if (lastSelectedNode != null) {
        // if (vis.getGroup(VizUtils.GRAPH).containsTuple(lastSelectedNode)) {
        //
        // Iterator<EdgeItem> edgeIter = lastSelectedNode.edges();
        //
        // lastSelectedNode.setFillColor(lastSelectedNode
        // .getStartFillColor());
        // lastSelectedNode.setTextColor(VizUtils.DEFAULT_TEXT_COLOR);
        //
        // while (edgeIter.hasNext()) {
        // BSplineEdgeItem edge = (BSplineEdgeItem) edgeIter.next();
        // edge.setSelected(false);
        // edge.setHighlighted(false);
        //
        // NodeItem nitem = edge.getAdjacentItem(lastSelectedNode);
        // nitem.setFillColor(nitem.getStartFillColor());
        // nitem.setTextColor(VizUtils.DEFAULT_TEXT_COLOR);
        // }
        // }
        // }

        lastSelectedNode = newNode;
    }
}

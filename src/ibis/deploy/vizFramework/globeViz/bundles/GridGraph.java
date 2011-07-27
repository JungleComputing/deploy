package ibis.deploy.vizFramework.globeViz.bundles;

import ibis.deploy.vizFramework.globeViz.data.GlobeEdge;
import ibis.deploy.vizFramework.globeViz.viz.BSpline3D;
import ibis.deploy.vizFramework.globeViz.viz.PieChartAnnotation;
import ibis.deploy.vizFramework.globeViz.viz.BSplinePolyline;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;

public class GridGraph {

    HashSet<Node> nodes;
    HashMap<Node, HashMap<Node, Double>> adjacencyLists;
    HashMap<Node, Position> realNodes;
    HashMap<Node, Position> nodeLocations;
    HashMap<Node, Node> parent = new HashMap<Node, Node>();

    public GridGraph() {
        nodes = new HashSet<Node>();
        adjacencyLists = new HashMap<Node, HashMap<Node, Double>>();
        realNodes = new HashMap<Node, Position>();
        nodeLocations = new HashMap<Node, Position>();

        generateGridGraph();
    }

    public void clear() {
        nodes.clear();
        adjacencyLists.clear();
        realNodes.clear();
    }

    public BSplinePolyline createArcBetween(ArrayList<Position> positions,
            Color color, int lineWidth) {
        BSplinePolyline polyline = new BSplinePolyline(positions);
        polyline.setColor(color);
        polyline.setLineWidth(lineWidth);
        polyline.setFollowTerrain(true);
        return polyline;
    }

    public void generateGrid(RenderableLayer layer, Globe globe) {
        ArrayList<Position> poly = new ArrayList<Position>();
        BSplinePolyline polyline;

        layer.removeAllRenderables();

        int i = 0;

        for (Node node : adjacencyLists.keySet()) {
            poly.clear();
            poly.add(getPositionFromNode(node));
            for (Node neighbour : adjacencyLists.get(node).keySet()) {
                if (poly.size() == 1) {
                    poly.add(1, getPositionFromNode(neighbour));
                } else {
                    poly.set(1, getPositionFromNode(neighbour));
                }
                poly = BSpline3D.computePolyline(globe, poly);
                polyline = createArcBetween(poly, Color.blue, 2);
                layer.addRenderable(polyline);
            }
        }
    }

    public void computePaths(ConcurrentHashMap<GlobeEdge, Double> globeEdges,
            Set<PieChartAnnotation> annotationList, RenderableLayer layer, Globe globe) {
        Node start, stop;
        HashMap<Node, HashMap<Node, GlobeEdge>> adjReal = new HashMap<Node, HashMap<Node, GlobeEdge>>();
        HashMap<Node, GlobeEdge> temp;
        ArrayList<Position> result;

        layer.removeAllRenderables();
        addRealNodes(annotationList);

        for (GlobeEdge edge : globeEdges.keySet()) {
            start = new Node(edge.getFirstPosition().getLatitude().degrees,
                    edge.getFirstPosition().getLongitude().degrees);
            stop = new Node(edge.getSecondPosition().getLatitude().degrees,
                    edge.getSecondPosition().getLongitude().degrees);

            if (adjReal.get(start) == null) {
                adjReal.put(start, new HashMap<Node, GlobeEdge>());
            }
            adjReal.get(start).put(stop, edge);
        }

        for (Node node : adjReal.keySet()) {
            Dijsktra(node);
            temp = adjReal.get(node);
            for (Node otherNode : temp.keySet()) {
                result = getPathFrom(node, otherNode, 0.8);
                result = BSpline3D.computePolyline(globe, result); // compute the b-spline
                BSplinePolyline polyline = createArcBetween(result,
                        Color.red, 2);
                layer.addRenderable(polyline);
            }
        }
    }

    public void addRealNodes(Set<PieChartAnnotation> annotationList) {
        Node node;
        Position pos;
        double lat, lon, lat1, lat2, lon1, lon2;
        HashMap<Node, Double> tempAdj;
        for (PieChartAnnotation annotation : annotationList) {
            pos = annotation.getPosition();
            lat = pos.getLatitude().degrees;
            lon = pos.getLongitude().degrees;
            node = new Node(lat, lon);
            realNodes.put(node, pos);

            if (!nodes.contains(node)) {
                nodes.add(node);
                tempAdj = new HashMap<Node, Double>();
                adjacencyLists.put(node, tempAdj);
                lat1 = lat - lat % UIConstants.GRID_CELL_SIZE;
                lat2 = (lat1 + UIConstants.GRID_CELL_SIZE) % 90;

                lon1 = lon - lon % UIConstants.GRID_CELL_SIZE;
                lon2 = (lon1 + UIConstants.GRID_CELL_SIZE) % 180;

                // add edges connecting this node to the closest grid nodes
                addEdge(node, lat1, lon1);
                addEdge(node, lat1, lon2);
                addEdge(node, lat2, lon2);
                addEdge(node, lat2, lon1);
            }
        }
    }

    private void addEdge(Node node, double lat, double lon) {
        Node neighbour = new Node(lat, lon);
        double dist = node.distanceTo(neighbour);
        adjacencyLists.get(node).put(neighbour, dist);
        adjacencyLists.get(neighbour).put(node, dist);
    }

    public void generateGridGraph() {
        int i, j, k, l, lat, lon;
        Node pos, other;
        double dist;
        boolean createArc = true;

        long time = System.currentTimeMillis();

        for (i = -90; i <= 90; i += UIConstants.GRID_CELL_SIZE) {
            for (j = -180; j <= 180; j += UIConstants.GRID_CELL_SIZE) {
                pos = new Node(i, j);
                nodes.add(pos);
                for (k = i - UIConstants.GRID_CELL_SIZE; k <= i
                        + UIConstants.GRID_CELL_SIZE; k += UIConstants.GRID_CELL_SIZE) {
                    for (l = j - UIConstants.GRID_CELL_SIZE; l <= j
                            + UIConstants.GRID_CELL_SIZE; l += UIConstants.GRID_CELL_SIZE) {
                        lat = k;
                        lon = l;
                        createArc = true;
                        // wrap around
                        if (k < -90 || k > 90 || (k == i && l == j)) {
                            createArc = false;
                        }
                        if (l < -180) {
                            lon = 180;
                        }
                        if (l > 180) {
                            lon = -180;
                        }
                        if (createArc) {
                            other = new Node(lat, lon);
                            if (adjacencyLists.get(pos) == null) {
                                adjacencyLists.put(pos,
                                        new HashMap<Node, Double>());
                            }
                            if (adjacencyLists.get(other) == null) {
                                adjacencyLists.put(other,
                                        new HashMap<Node, Double>());
                            }
                            dist = pos.distanceTo(other);
                            adjacencyLists.get(pos).put(other, dist);
                            adjacencyLists.get(other).put(pos, dist);
                        }
                    }
                }
            }
        }
        //System.out.println(System.currentTimeMillis() - time);
    }

    private ArrayList<Position> getPathFrom(Node start, Node stop,
            double bundlingFactor) {

        //System.out.println("----------------------------------->");
        ArrayList<Node> tempResult = new ArrayList<Node>();
        ArrayList<Position> result = new ArrayList<Position>();
        Node current = stop;
        Position pos;

        // add stop point three times in order to force the spline to go through
        // it
        // tempResult.add(stop);
        // tempResult.add(stop);

        while (current != null && !current.equals(start)) {
            tempResult.add(current);
            current = parent.get(current);
        }

        // tempResult.add(start);
        // tempResult.add(start);
        tempResult.add(start);

        double b = bundlingFactor, ib = 1 - bundlingFactor;
        double ux, uy, dx, dy;
        int N = tempResult.size(), i;
        Node temp;

        // //apply bundling factor
        // if (b < 1) {
        // Node o = stop;
        // ux = o.lat;
        // uy = o.lon;
        //
        // o = start;
        // dx = o.lat;
        // dy = o.lon;
        //
        // dx = (dx - ux) / (N + 2);
        // dy = (dy - uy) / (N + 2);
        //
        // // adjust the control points, with the exception of the first
        // // three and last three, which are just copies of the start and
        // // stop nodes and shouldn't be moved
        // for (i = 3; i < N - 3; i++) {
        // temp = tempResult.get(i);
        // temp.lat = b * temp.lat + ib * (ux + (i + 2) * dx);
        // temp.lon = b * temp.lon + ib * (uy + (i + 2) * dy);
        // tempResult.set(i, temp);
        // }
        // }

        for (i = 0; i < tempResult.size(); i++) {
            result.add(getPositionFromNode(tempResult.get(i)));
        }
        return result;
    }

    private Position getPositionFromNode(Node current) {
        Position pos;
        if (nodeLocations.get(current) != null) {
            pos = nodeLocations.get(current);
        } else {
            pos = new Position(LatLon.fromDegrees(current.lat, current.lon), 0);
            nodeLocations.put(current, pos);
        }
        return pos;
    }

    // TODO - to make this more efficient we could reuse the heap
    public void Dijsktra(Node start) {
        long time = System.currentTimeMillis();
        MinHeap distHeap = new MinHeap(nodes.size() + 1);
        HeapEntry current, temp;
        for (Node node : nodes) {
            parent.put(node, null);
        }

        distHeap.fastInsert(nodes.size(), nodes);
        distHeap.updateValue(start, null, 0);

        while (!distHeap.empty()) {
            current = distHeap.extractMin();

            // for every adjacent edge
            for (Node key : adjacencyLists.get(current.node).keySet()) {

                temp = distHeap.getNodeInHeap(key);
                if (temp != null
                        && temp.cost > current.cost
                                + adjacencyLists.get(current.node).get(key)) {
                    distHeap.updateValue(temp.node, current.node, current.cost
                            + adjacencyLists.get(current.node).get(key));
                    parent.put(temp.node, current.node);
                }
            }
        }
        // System.out.println("Time for Dijkstra: "
        // + (System.currentTimeMillis() - time));
    }
}

class Node {
    public double lat, lon;

    public Node(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public boolean equals(Object node) {
        if (node instanceof Node) {

            return ((Node) node).lat == lat && ((Node) node).lon == lon;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (41 * (41 + lat) + lon);
    }

    public String toString() {
        return "Lat: " + lat + " Lon: " + lon;
    }

    public double distanceTo(Node other) {
        double dx = other.lat - lat;
        if (dx > 5) { // wrap around the globe
            dx = 5;
        }
        double dy = other.lon - lon;
        if (dy > 5) { // wrap around the globe
            dy = 5;
        }
        return Math.sqrt(dx * dx + dy * dy);
    }
}

class Edge {
    Position start, stop;

    public Edge(Position start, Position stop) {
        this.start = start;
        this.stop = stop;
    }
}

class HeapEntry {
    public Node node, otherNode;
    public double cost;

    public HeapEntry(Node node, Node otherNode, double cost) {
        this.node = node;
        this.otherNode = otherNode;
        this.cost = cost;
    }

    // TODO if this is undirected?
    public boolean equals(HeapEntry e) {

        return (node.equals(e.node) && otherNode.equals(e.otherNode) || node
                .equals(e.otherNode) && otherNode.equals(e.node))
                && cost == e.cost;
    }

    public int hashCode() {
        return (41 + node.hashCode()) * 41 + otherNode.hashCode();
    }

    public String toString() {
        return node.toString() + " - " + cost + " - " + otherNode.toString();
    }
}

class MinHeap {
    private HeapEntry[] heap;
    private int n = 0;
    private final int minIndex = 1;

    public HashMap<Node, Integer> positionsInHeap;

    public MinHeap(int size) {
        heap = new HeapEntry[size];
        positionsInHeap = new HashMap<Node, Integer>();
    }

    public void fastInsert(int nNodes, HashSet<Node> nodes) {
        HeapEntry entry;
        if (nNodes > heap.length - 1) { // they don't fit
            System.err.println("Cannot insert in heap. Heap too small.");
            return;
        }
        int i = 0;

        for (Node node : nodes) {
            heap[++i] = new HeapEntry(node, null, Double.MAX_VALUE);
        }

        n = nNodes;

        for (i = n; i > 1; i--) {
            bubbleUp(i);
        }

        for (i = 1; i < nNodes; i++) {
            entry = heap[i];
            positionsInHeap.put(entry.node, i); // keep track of the position of
                                                // the node in the heap
        }
    }

    public HeapEntry getNodeInHeap(Node node) {
        int pos = positionsInHeap.get(node);
        if (pos > 0) { // node is in heap
            return heap[pos];
        }
        return null;
    }

    public void updateValue(Node node, Node other, double cost) {
        int pos = positionsInHeap.get(node);
        if (pos > 0) { // node is in the heap
            HeapEntry entry = heap[pos];
            if (entry.cost > cost) {
                entry.otherNode = other;
                entry.cost = cost;
                bubbleUp(pos);
            }
        }
    }

    // we are going to create a min heap
    public void insert(HeapEntry value) {
        if (n == heap.length - 1) { // the heap is full
            System.err.println("Cannot insert in heap. Full.");
            return;
        }
        n++;
        heap[n] = value;
        bubbleUp(n);
    }

    private void bubbleUp(int pos) {
        // the parent no longer dominates the kids and we haven't reached the
        // root yet
        if (pos > minIndex && heap[pos / 2].cost > heap[pos].cost) {
            swap(pos / 2, pos);
            bubbleUp(pos / 2);
        }
    }

    public HeapEntry extractMin() {
        if (n < 1) {
            System.err.println("Cannot delete from heap. Empty.");
            return null;
        }
        HeapEntry min = heap[minIndex];
        positionsInHeap.put(min.node, -1);
        heap[minIndex] = heap[n];
        n--;
        bubbleDown(minIndex);
        return min;
    }

    private void bubbleDown(int idx) {
        int replacementIdx = -1;
        if (2 * idx > n) { // node has no kids
            return;
        } else if (2 * idx + 1 > n) { // the node only has a left kid
            if (heap[2 * idx].cost < heap[idx].cost) {
                replacementIdx = 2 * idx;
            }
        } else { // two kids
            replacementIdx = idx;
            for (int i = 0; i <= 1; i++) {
                if (heap[2 * idx + i].cost < heap[replacementIdx].cost) {
                    replacementIdx = 2 * idx + i;
                }
            }
        }
        if (replacementIdx > 0 && replacementIdx != idx) {
            swap(idx, replacementIdx);
            bubbleDown(replacementIdx);
        }
    }

    private void swap(int i, int j) {
        HeapEntry temp;
        temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
        positionsInHeap.put(heap[i].node, i); // we also need to keep track of
                                              // the
                                              // positions in the heap
        positionsInHeap.put(heap[j].node, j);
    }

    public void print() {
        for (int i = 1; i <= n; i++) {
            System.out.println(heap[i] + " ");
        }
        System.out.println("-------------------------");
    }

    public boolean empty() {
        return n <= 0;
    }
}

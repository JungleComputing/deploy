package ibis.deploy.vizFramework.globeViz.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Timer;

import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.Marker;
import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Link.LinkDirection;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.collection.impl.LocationImpl;
import ibis.deploy.util.Colors;
import ibis.deploy.vizFramework.globeViz.viz.CircleAnnotation;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;

public class GlobeDataConvertor implements IDataConvertor {

    private final GlobeVisualization globeViz;
    private ConcurrentHashMap<String, Position> positionList;

    private double maximum = Double.MIN_VALUE;
    // a hash map containing active edges - used mostly for caching polylines
    private ConcurrentHashMap<GlobeEdge, Double> globeEdges;
    // hash map used for storing edges temporarily on update
    private ConcurrentHashMap<GlobeEdge, Double> tempEdges;
    
    private ConcurrentHashMap<GlobeEdge, Integer> numberOfSubedgesPerEdge;
    // the starting point of the Location tree, used for retrieving data
    private final Location root;
    // visible pie charts
    private Set<CircleAnnotation> pieChartWaypointSet;
    private GUI gui;
    private final Timer particleMovementTimer; // , particleReleaseTimer;
    private boolean initialized = false;
    private boolean showParticles = true;

    public GlobeDataConvertor(GlobeVisualization globeVizRef,
            Location rootRef) {

        this.globeViz = globeVizRef;
        globeViz.setDataConvertor(this);
        this.root = rootRef;

        positionList = new ConcurrentHashMap<String, Position>();
        globeEdges = new ConcurrentHashMap<GlobeEdge, Double>();
        tempEdges = new ConcurrentHashMap<GlobeEdge, Double>();
        numberOfSubedgesPerEdge = new ConcurrentHashMap<GlobeEdge, Integer>();
        
        pieChartWaypointSet = Collections
                .synchronizedSet(new HashSet<CircleAnnotation>());

        GlobeVisualization.getVisualization().getInputHandler()
                .addMouseWheelListener(new MouseWheelListener() {

                    @Override
                    public void mouseWheelMoved(MouseWheelEvent event) {
                        int amount = event.getWheelRotation();
                        refreshClusterData(amount < 0 ? true : false, true);
                    }
                });

        // this is to force cluster regrouping after the first render
        GlobeVisualization.getVisualization().addRenderingListener(
                new RenderingListener() {

                    @Override
                    public void stageChanged(RenderingEvent event) {
                        if (!event.getStage().equals(
                                RenderingEvent.BEFORE_BUFFER_SWAP))
                            return;

                        if (!initialized) {
                            refreshClusterData(false, true);
                            initialized = true;
                        }

                        // if(edgesChanged){
                        // // we can update the knots only now, because they are
                        // computed
                        // // once the edges are redrawn
                        // for (GlobeEdge edge : globeEdges.keySet()) {
                        // if (edge != null) {
                        // edge.updateKnots();
                        // }
                        // }
                        // edgesChanged = false;
                        // }
                    }
                });

        // MarkerMovementThread mTimer = new MarkerMovementThread(this);
        // new Thread(mTimer).start();

        GlobeVisualization.getVisualization().addMouseListener(
                new MouseListener() {

                    @Override
                    public void mouseReleased(MouseEvent arg0) {
                        //refreshClusterData(false, true);
                    }

                    @Override
                    public void mousePressed(MouseEvent arg0) {
                    }

                    @Override
                    public void mouseExited(MouseEvent arg0) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent arg0) {
                    }

                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                    }
                });

        particleMovementTimer = new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                moveMarkers();
            }
        });
        particleMovementTimer.start();

        // particleReleaseTimer = new Timer(1000, new ActionListener() {
        //
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // double d = Math.random();
        // // if (d > 0.5) {
        // launchMarkerOnEachEdge();
        // // }
        // }
        // });
        // particleReleaseTimer.start();

        // generateFixedLocations(globeViz.getAnnotationLayer(), globeViz);
    }

    public synchronized void refreshClusterData(boolean zoomIn,
            boolean regroupPies) {
        boolean pieChartsChanged = false;

        if (regroupPies) {
//            if (zoomIn) {
//                pieChartsChanged = splitPieCharts();
//                System.out.println("split");
            //} else {
                // TODO - see if we also keep the split before here or not
                pieChartsChanged = splitPieCharts();
                pieChartsChanged = pieChartsChanged || groupPieCharts();
            //}
        }
        try {
            if (pieChartsChanged) {
                // TODO - if there are any problems with edge caching, look here
                // globeEdges.clear();
                globeViz.clearPolylineLayer();
                // clear existing markers
                globeViz.getMarkerLayer().clearMarkers();
            }
            tempEdges.clear();
            numberOfSubedgesPerEdge.clear();
            connectClusters(root, UIConstants.FAKE_LEVELS, pieChartsChanged);

            redrawEdges(pieChartsChanged);
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }

    private synchronized void launchMarkerOnEachEdge() {
        for (GlobeEdge edge : globeEdges.keySet()) {
            launchMarker(edge);
        }
    }

    private synchronized void launchMarker(GlobeEdge edge) {
        Marker marker = edge.addMarkerToEdge();
        if (marker != null) {
            globeViz.getMarkerLayer().addMarker(marker);
        }
    }

    public synchronized void moveMarkers() {
        // Vector<Marker> markerVector = (Vector<Marker>) globeViz
        // .getMarkerVector();

        for (GlobeEdge edge : globeEdges.keySet()) {
            if (edge.getPolyline() != null) {
                // this forces a color change and translation for all the edge
                // markers
                edge.moveMarkers(globeViz);
            }
        }
        // force a redraw after every update, otherwise the particles won't move
        // until the next update
        globeViz.redraw();
    }

    public synchronized void updateData(Location root,
            boolean structureChanged, boolean forced) {
        if (structureChanged) {
            positionList.clear();
            globeViz.clearAnnotationLayer();

            generateInitialLocationAnnotations(root, UIConstants.FAKE_LEVELS,
                    structureChanged);
        }
        refreshClusterData(false, structureChanged);
        if (!forced) {
            launchMarkerOnEachEdge();
        }
    }

    private synchronized void redrawEdges(boolean pieChartsChanged) {
        Color newColor;

        maximum = Double.MIN_VALUE;

        double value;
        ArrayList<GlobeEdge> edgesToRemove = new ArrayList<GlobeEdge>();

        if (globeEdges != null && tempEdges != null) {

            // map the new edges to add - we just want to reuse the existing
            // edge values in the globeEdges map, because some of them already
            // have the polyline computed

            // mark the edges which no longer exist
            for (GlobeEdge edge : globeEdges.keySet()) {
                if (!tempEdges.keySet().contains(edge)) {
                    edgesToRemove.add(edge);
                }
            }

            // remove edges which no longer exist
            for (GlobeEdge edge : edgesToRemove) {
                globeEdges.remove(edge);
                // globeViz.removeMarkers(edge.getMarkers());
            }

            // update existing edges
            for (GlobeEdge edge : tempEdges.keySet()) {
                //System.out.println("** " + tempEdges.get(edge) + "  " + numberOfSubedgesPerEdge.get(edge));
                value = tempEdges.get(edge) / numberOfSubedgesPerEdge.get(edge);
                //System.out.println(value);
                //System.out.println(edge.getName());
                updateMax(value);
                globeEdges.put(edge, value);
            }
            //System.out.println("---------------------------------->");

            for (GlobeEdge edge : globeEdges.keySet()) {
                if (edge != null) {
                    if (globeEdges.get(edge) != null) {
                        double ratio = globeEdges.get(edge) / maximum;
                        newColor = Utils.blend(Color.red, Color.green, ratio,
                                0.7f);
                        edge.updateAssociatedPolyline(globeViz, newColor,
                                pieChartsChanged, showParticles);

                        if (pieChartsChanged) {
                            globeViz.getMarkerLayer().addAllMarkers(
                                    edge.getMarkers());
                        }
                    }
                }
            }
            globeViz.redraw();
        }
    }

    // creates an annotation for each existing cluster. These will later be
    // grouped into pie charts.
    private synchronized void generateInitialLocationAnnotations(Location root,
            int level, boolean structureChanged) {
        ArrayList<Location> dataChildren = root.getChildren();
        if (dataChildren == null || dataChildren.size() == 0) {
            return;
        }
        for (Location loc : dataChildren) {
            if (level == 0) {
                if (structureChanged) {
                    createAnnotation(loc, null);

                }
            }

            generateInitialLocationAnnotations(loc, level - 1, structureChanged);
        }
    }

    private void createAnnotation(Location loc, Color color) {
        CircleAnnotation annotation;
        LocationImpl finalLocation = (LocationImpl) loc;
        Position pos = new Position(LatLon.fromDegrees(
                finalLocation.getLatitude(), finalLocation.getLongitude()), 0);

        annotation = new CircleAnnotation(pos, new AnnotationAttributes(),
                finalLocation.getName());
        if (color == null) {
            Cluster cluster = gui.getWorkSpace().getGrid()
                    .getCluster(loc.getName());
            if (cluster != null) {
                annotation.getAttributes().setTextColor(
                        gui.getWorkSpace().getGrid().getCluster(loc.getName())
                                .getColor());
            } else {
                annotation.getAttributes().setTextColor(
                        Colors.fromLocation(loc.getName()));
            }
        } else {
            annotation.getAttributes().setTextColor(color);
        }

        globeViz.getAnnotationLayer().addRenderable(annotation);

        positionList.put(loc.getName(), pos);
        pieChartWaypointSet.add(annotation);
    }

    private boolean splitPieCharts() {
        HashMap<String, HashSet<String>> adjacencyList = new HashMap<String, HashSet<String>>();
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        HashMap<String, Position> positionsInPieChart = new HashMap<String, Position>();
        ArrayList<String> connectedComponent = new ArrayList<String>();
        HashSet<CircleAnnotation> pieChartsToErase = new HashSet<CircleAnnotation>();
        HashSet<CircleAnnotation> pieChartsToAdd = new HashSet<CircleAnnotation>();
        CircleAnnotation newPieChart;
        boolean pieChartsChanged = false;

        for (CircleAnnotation oldPieChart : pieChartWaypointSet) {
            adjacencyList.clear();
            visited.clear();
            positionsInPieChart.clear();

            for (String cluster : oldPieChart.getClusters()) {
                positionsInPieChart.put(cluster, positionList.get(cluster));
            }

            // create a graph from the clusters in the pie chart;
            createAdjacencyList(positionsInPieChart, adjacencyList, visited);

            for (String cluster : oldPieChart.getClusters()) {
                connectedComponent.clear(); // reuse the same structure

                // compute the connected component for each unvisited node
                if (!visited.get(cluster)) {
                    DFS(cluster, adjacencyList, visited, connectedComponent);

                    // if there is more than one connected component, it means
                    // that we need to re-create the pie charts
                    if (connectedComponent.size() != oldPieChart.getClusters()
                            .size()) {

                        pieChartsToErase.add(oldPieChart);

                        ArrayList<Position> positions = new ArrayList<Position>();
                        for (String location : connectedComponent) {
                            positions.add(positionList.get(location));
                        }
                        newPieChart = new CircleAnnotation(connectedComponent,
                                positions, new AnnotationAttributes(),
                                GlobeVisualization.getVisualization());

                        pieChartsToAdd.add(newPieChart);

                        pieChartsChanged = true;
                    }
                }
            }
        }

        pieChartWaypointSet.removeAll(pieChartsToErase);

        pieChartWaypointSet.addAll(pieChartsToAdd);

        if (pieChartsChanged) {
            globeViz.clearAnnotationLayer();
            globeViz.getAnnotationLayer().addRenderables(pieChartWaypointSet);
        }

        return pieChartsChanged;
    }

    private boolean groupPieCharts() {
        HashMap<String, HashSet<String>> adjacencyList = new HashMap<String, HashSet<String>>();
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        HashMap<String, Position> positionsForPieCharts = new HashMap<String, Position>();
        ArrayList<String> connectedComponent = new ArrayList<String>();
        HashSet<CircleAnnotation> invisiblePieCharts = new HashSet<CircleAnnotation>();
        HashSet<CircleAnnotation> pieChartsToAdd = new HashSet<CircleAnnotation>();
        CircleAnnotation newPieChart;
        boolean pieChartsChanged = false;
        ArrayList<Position> positionsInConnectedComponent = new ArrayList<Position>();

        Set<CircleAnnotation> visiblePieChartWaypointSet = new HashSet<CircleAnnotation>();

        // add all the annotations to the set
        for (Renderable renderable : globeViz.getAnnotationLayer()
                .getRenderables()) {

            if (renderable instanceof CircleAnnotation) {

                CircleAnnotation oldPieChart = (CircleAnnotation) renderable;

                visiblePieChartWaypointSet.add(oldPieChart);

                visited.put(oldPieChart.getName(), false);

                positionsForPieCharts.put(oldPieChart.getName(),
                        oldPieChart.getPosition());

                //TODO - decide later if we leave this here or not - it optimizes computations, but it messes up grouping
//                if (!positionIsVisible(oldPieChart.getPosition())) {
//                    // don't take hidden pie charts into consideration
//                    invisiblePieCharts.add(oldPieChart);
//                }
            }
        }

        // remove all piecharts that aren't visible - we still keep them in the
        // invisiblePieCharts, so that we can render them again in the end
        //visiblePieChartWaypointSet.removeAll(invisiblePieCharts);

        // create a graph from the clusters in the pie chart;
        createAdjacencyList(positionsForPieCharts, adjacencyList, visited);

        for (CircleAnnotation waypoint : visiblePieChartWaypointSet) {
            connectedComponent.clear(); // reuse the same structure
            positionsInConnectedComponent.clear();

            // compute the connected component for each unvisited node
            if (!visited.get(waypoint.getName())) {
                DFS(waypoint.getName(), adjacencyList, visited,
                        connectedComponent);

                // the component does not consist of a single cluster
                if (connectedComponent.size() > 1) {
                    newPieChart = null;

                    for (CircleAnnotation oldPie : visiblePieChartWaypointSet) {
                        if (oldPie.containsSameClustersAs(connectedComponent)) {
                            // we can reuse the existing pie chart
                            newPieChart = oldPie;
                            break;
                        }
                    }

                    // that pie chart doesn't exist yet, we need to create it
                    // TODO - attributes might be wrong
                    if (newPieChart == null) {

                        for (String location : connectedComponent) {
                            positionsInConnectedComponent
                                    .add(retrievePositionFromPieList(location));
                        }
                        newPieChart = new CircleAnnotation(connectedComponent,
                                positionsInConnectedComponent,
                                new AnnotationAttributes(),
                                GlobeVisualization.getVisualization());

                        pieChartsChanged = true;
                    }

                    pieChartsToAdd.add(newPieChart);
                    // pieChartsToErase.add(waypoint);
                } else {
                    pieChartsToAdd.add(waypoint);
                }
            }
        }

        if (pieChartsChanged) {

            pieChartWaypointSet.clear();
            pieChartWaypointSet.addAll(pieChartsToAdd);

            // re-add all the invisible pie charts
            pieChartWaypointSet.addAll(invisiblePieCharts);

            globeViz.clearAnnotationLayer();
            globeViz.getAnnotationLayer().addRenderables(pieChartWaypointSet);
        }

        return pieChartsChanged;
    }

    private boolean positionIsVisible(Position pos) {
        Vec4 vecPos = GlobeVisualization.getVisualization().getModel()
                .getGlobe().computePointFromPosition(pos);

        return GlobeVisualization.getVisualization().getView()
                .getFrustumInModelCoordinates().contains(vecPos);
    }

    private Position retrievePositionFromPieList(String name) {
        for (CircleAnnotation annotation : pieChartWaypointSet) {
            if (annotation.getName().equals(name)) {
                return annotation.getPosition();
            }
        }

        return null;
    }

    private void createAdjacencyList(HashMap<String, Position> positionList,
            HashMap<String, HashSet<String>> adjacencyList,
            HashMap<String, Boolean> visited) {
        double distance, minDistance;

        for (String location : positionList.keySet()) {
            adjacencyList.put(location, new HashSet<String>());
            visited.put(location, false);
        }

        // create adjacency lists for all visible clusters (build the graph)
        for (String waypoint : positionList.keySet()) {

            for (String secondWaypoint : positionList.keySet()) {
                if (secondWaypoint != waypoint) {

                    Position p1 = positionList.get(waypoint);
                    Position p2 = positionList.get(secondWaypoint);

                    distance = Utils.computeDistance(p1, p2, GlobeVisualization
                            .getVisualization().getModel().getGlobe(),
                            GlobeVisualization.getVisualization().getView());

                    minDistance = 2 * UIConstants.LOCATION_CIRCLE_SIZE;

                    // if the two clusters overlap
                    if (distance > 0 && distance <= minDistance) {
                        adjacencyList.get(waypoint).add(secondWaypoint);
                        adjacencyList.get(secondWaypoint).add(waypoint);
                        // System.out.println(waypoint.getLocationName() + "-->"
                        // + secondWaypoint.getLocationName() + " " + distance);
                    }
                }
            }
        }
    }

    private void connectClusters(Location root, int level,
            boolean structureChanged) {
        ArrayList<Location> dataChildren = root.getChildren();
        if (dataChildren == null || dataChildren.size() == 0) {
            return;
        }
        for (Location loc : dataChildren) {
            if (level == 0) {
                generatePieChartConnections(loc, structureChanged);
            }

            connectClusters(loc, level - 1, structureChanged);
        }
    }

    private synchronized void generatePieChartConnections(Location loc,
            boolean structureChanged) {
        Link[] links = loc.getLinks();
        String startLocation, stopLocation;
        Position pos1, pos2;
        CircleAnnotation startPie, stopPie;
        double value1 = 0, value2 = 0;

        for (Link link : links) {

            // only create arcs between locations
            if ((link.getSource() instanceof LocationImpl)
                    && (link.getDestination() instanceof LocationImpl)) {
                startLocation = ((LocationImpl) link.getSource()).getName();
                stopLocation = ((LocationImpl) link.getDestination()).getName();

                startPie = stopPie = null;

                for (CircleAnnotation pie : pieChartWaypointSet) {
                    if (pie.containsCluster(startLocation)) {
                        startPie = pie;
                    }

                    if (pie.containsCluster(stopLocation)) {
                        stopPie = pie;
                    }

                    // if we already found the right pie charts, stop
                    if (startPie != null && stopPie != null) {
                        break;
                    }
                }

                if (startPie != null && stopPie != null && startPie != stopPie) {

                    value1 = value2 = 0;

                    for (Metric metric : link
                            .getMetrics(LinkDirection.SRC_TO_DST)) {
                        try {
                            value1 = (Float) metric.getValue(
                                    MetricModifier.NORM, MetricOutput.PERCENT);
                        } catch (OutputUnavailableException e) {
                            System.out.println(e.getMessage());
                        }
                    }

//                    for (Metric metric : link
//                            .getMetrics(LinkDirection.DST_TO_SRC)) {
//                        try {
//                            value2 = (Float) metric.getValue(
//                                    MetricModifier.NORM, MetricOutput.PERCENT);
//                        } catch (OutputUnavailableException e) {
//                            System.out.println(e.getMessage());
//                        }
//                    }

                    pos1 = startPie.getPosition();
                    pos2 = stopPie.getPosition();

                    if (pos1 != null && pos2 != null) {
                        GlobeEdge reverseEdge = new GlobeEdge(pos2, pos1,
                                stopPie.getName() + " -> " + startPie.getName(), false);
                        boolean isSecondEdge = tempEdges.containsKey(reverseEdge);
                        //System.out.println(isSecondEdge + reverseEdge.getName());
                        GlobeEdge edge = new GlobeEdge(pos1, pos2,
                                startPie.getName() + " -> " + stopPie.getName(), isSecondEdge);
                        // if the edge is already in the map, this just updates
                        // the value
                        if (tempEdges.get(edge) != null) {
                            //System.out.println(tempEdges.get(edge) + "  " + value1);
                            tempEdges.put(edge, tempEdges.get(edge) + value1
                                    + value2);
                            numberOfSubedgesPerEdge.put(edge, numberOfSubedgesPerEdge.get(edge) + 1);
                        } else {
                            //System.out.println(tempEdges.get(edge) + "  " + value1);
                            tempEdges.put(edge, value1 + value2);
                            numberOfSubedgesPerEdge.put(edge, 1);
                        }
                    }
                }
            }
        }
    }

    private synchronized boolean checkIfEdgeInMap(GlobeEdge edge) {
        GlobeEdge secondEdge;
        for (Enumeration<GlobeEdge> e = globeEdges.keys(); e.hasMoreElements();) {
            secondEdge = e.nextElement();
            if (secondEdge.positionsEqual(edge.getFirstPosition(),
                    edge.getSecondPosition())) {
                return true;
            }
        }

        return false;
    }

    // recursive depth-first search
    private void DFS(String node, HashMap<String, HashSet<String>> graph,
            HashMap<String, Boolean> visited, ArrayList<String> component) {
        if (visited.get(node)) {
            return;
        }

        component.add(node); // add the node to the current connected component
        visited.put(node, true);

        for (String neighbour : graph.get(node)) {
            if (!visited.get(neighbour)) {
                DFS(neighbour, graph, visited, component);
            }
        }
    }

    private void updateMax(double value) {
        if (value > maximum) {
            maximum = value;
        }
    }

    public synchronized void forceUpdate() {
        updateData(root, true, true);
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    public void setShowParticles(boolean value) {
        showParticles = value;
        if (showParticles) {
            if (!particleMovementTimer.isRunning()) { // start the particle
                                                      // timers
                particleMovementTimer.start();
                // particleReleaseTimer.start();
                globeViz.getMarkerLayer().setEnabled(true);
            }
        } else { // stop the particle timers
            if (particleMovementTimer.isRunning()) {
                particleMovementTimer.stop();
                // particleReleaseTimer.stop();
                globeViz.getMarkerLayer().setEnabled(false);
            }
        }
        updateData(root, true, true);
    }

    // TODO - remove this, it's used only for low level testing
    public void generateFixedLocations(RenderableLayer layer,
            GlobeVisualization globe) {

        Position pos1, pos2, pos3;
        CircleAnnotation annotation;

        AnnotationAttributes dotAttributes = new AnnotationAttributes();
        dotAttributes.setDrawOffset(new Point(0, -16));
        dotAttributes.setSize(new Dimension(15, 15));
        dotAttributes.setBorderWidth(0);
        dotAttributes.setCornerRadius(0);
        dotAttributes.setImageSource(null);
        dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));

        pos1 = Position.ZERO;
        pos2 = new Position(LatLon.fromDegrees(-10, -5), 0);
        pos3 = new Position(LatLon.fromDegrees(5, -5), 0);

        annotation = new CircleAnnotation(pos1, dotAttributes,
                "Location1@Location1@ASD");
        annotation.getAttributes().setTextColor(Color.blue);
        layer.addRenderable(annotation);

        pieChartWaypointSet.add(annotation);

        annotation = new CircleAnnotation(pos2, dotAttributes,
                "Location2@Location2@ASD");
        annotation.getAttributes().setTextColor(Color.blue);
        layer.addRenderable(annotation);

        pieChartWaypointSet.add(annotation);

        annotation = new CircleAnnotation(pos3, dotAttributes,
                "Location3@Location3@ASD");
        annotation.getAttributes().setTextColor(Color.blue);
        layer.addRenderable(annotation);

        positionList.put("Location1@Location1@ASD", pos1);
        positionList.put("Location2@Location2@ASD", pos2);
        positionList.put("Location3@Location3@ASD", pos3);

        pieChartWaypointSet.add(annotation);
    }
}

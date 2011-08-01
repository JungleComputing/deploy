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
import ibis.deploy.vizFramework.globeViz.bundles.GridGraph;
import ibis.deploy.vizFramework.globeViz.viz.PieChartAnnotation;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;

public class GlobeDataConvertor implements IDataConvertor {

    private final GlobeVisualization globeViz;

    // positions before clusters are grouped
    private ConcurrentHashMap<String, Position> positionList;

    // a hash map containing active edges - used mostly for caching polylines
    private ConcurrentHashMap<GlobeEdge, Double> globeEdges;
    // hash map used for storing edges temporarily on update
    private ConcurrentHashMap<GlobeEdge, Double> tempEdges;

    private ConcurrentHashMap<GlobeEdge, Integer> numberOfSubedgesPerEdge;
    // the starting point of the Location tree, used for retrieving data
    private Location root;
    // visible pie charts
    private Set<PieChartAnnotation> pieChartWaypointSet;
    private ConcurrentHashMap<String, PieChartAnnotation> initialClusterAnnotations;
    private GUI gui;
    private final Timer particleMovementTimer;
    private boolean initialized = false;
    private boolean showParticles = true;

    private GridGraph bundlesGenerator;

    public GlobeDataConvertor(GlobeVisualization globeVizRef, Location rootRef) {

        this.globeViz = globeVizRef;
        globeViz.setDataConvertor(this);
        this.root = rootRef;

        positionList = new ConcurrentHashMap<String, Position>();
        globeEdges = new ConcurrentHashMap<GlobeEdge, Double>();
        tempEdges = new ConcurrentHashMap<GlobeEdge, Double>();
        numberOfSubedgesPerEdge = new ConcurrentHashMap<GlobeEdge, Integer>();

        pieChartWaypointSet = Collections
                .synchronizedSet(new HashSet<PieChartAnnotation>());

        initialClusterAnnotations = new ConcurrentHashMap<String, PieChartAnnotation>();

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
                    }
                });

        particleMovementTimer = new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                moveMarkers();
            }
        });
        particleMovementTimer.start();

        // TODO - maybe remove?
        bundlesGenerator = new GridGraph();
    }

    public synchronized void refreshClusterData(boolean zoomIn,
            boolean structureChanged) {
        boolean pieChartsChanged = structureChanged;

        if (structureChanged) {
            if (zoomIn) {
                pieChartsChanged |= splitPieCharts();
            } else {
                pieChartsChanged |= groupPieCharts();
            }
        }

        try {
            if (pieChartsChanged) {
                // TODO - if there are any problems with edge caching, look here
                // globeEdges.clear();
                globeViz.clearPolylineLayer();
                globeViz.getMarkerLayer().clearMarkers();
            }
            tempEdges.clear();
            numberOfSubedgesPerEdge.clear();
            connectClusters(root, GUI.fakeData ? UIConstants.FAKE_LEVELS
                    : UIConstants.REAL_LEVELS_GLOBE, pieChartsChanged);
            redrawEdges(pieChartsChanged);

            // if (structureChanged) {
            // // bundlesGenerator.computePaths(globeEdges,
            // // pieChartWaypointSet,
            // // globeViz.mickeyMouseLayer, GlobeVisualization
            // // .getVisualization().getModel().getGlobe());
            // // bundlesGenerator.generateGrid(globeViz.mickeyMouseLayer,
            // // GlobeVisualization.getVisualization().getModel().getGlobe());
            // }
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
            initialClusterAnnotations.clear();
            this.root = root;
            generateInitialLocationAnnotations(root,
                    GUI.fakeData ? UIConstants.FAKE_LEVELS
                            : UIConstants.REAL_LEVELS_GLOBE);

        }
        refreshClusterData(false, structureChanged);
        if (!forced) {
            launchMarkerOnEachEdge();
        }
    }

    private synchronized void redrawEdges(boolean pieChartsChanged) {
        Color newColor;

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
                edge.removeAssociatedPolylineFromDisplay(globeViz);
            }

            // update existing edges
            for (GlobeEdge edge : tempEdges.keySet()) {
                value = tempEdges.get(edge) / numberOfSubedgesPerEdge.get(edge);
                globeEdges.put(edge, value);
            }

            for (GlobeEdge edge : globeEdges.keySet()) {
                if (edge != null) {
                    if (globeEdges.get(edge) != null) {
                        double ratio = globeEdges.get(edge);
                        newColor = Utils.blend(Color.red, Color.green, ratio,
                                0.7f);
                        edge.updateAssociatedPolyline(globeViz, newColor,
                                pieChartsChanged, showParticles, false);

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
            int level) {
        ArrayList<Location> dataChildren = root.getChildren();
        if (dataChildren == null || dataChildren.size() == 0) {
            return;
        }
        for (Location loc : dataChildren) {
            if (level == 0) {
                createAnnotation(loc, null);
            }

            generateInitialLocationAnnotations(loc, level - 1);
        }
    }

    private void createAnnotation(Location loc, Color color) {
        PieChartAnnotation annotation;
        LocationImpl finalLocation = (LocationImpl) loc;

        Position pos = new Position(LatLon.fromDegrees(
                finalLocation.getLatitude(), finalLocation.getLongitude()), 0);

        String locationName = Utils
                .extractLocationName(finalLocation.getName());

        annotation = new PieChartAnnotation(pos, new AnnotationAttributes(),
                locationName);
        if (color == null) {
            Cluster cluster = gui.getWorkSpace().getGrid()
                    .getCluster(locationName);
            if (cluster != null) {
                annotation.getAttributes().setTextColor(
                        gui.getWorkSpace().getGrid().getCluster(locationName)
                                .getColor());
            } else {
                annotation.getAttributes().setTextColor(
                        Colors.fromLocation(locationName));
            }
        } else {
            annotation.getAttributes().setTextColor(color);
        }

        globeViz.getAnnotationLayer().addRenderable(annotation);
        positionList.put(locationName, pos);
        pieChartWaypointSet.add(annotation);
        initialClusterAnnotations.put(locationName, annotation);
    }

    private boolean splitPieCharts2() {
        HashMap<String, HashSet<String>> adjacencyList = new HashMap<String, HashSet<String>>();
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        HashMap<String, Position> positionsInPieChart = new HashMap<String, Position>();
        ArrayList<String> connectedComponent = new ArrayList<String>();
        HashSet<PieChartAnnotation> pieChartsToErase = new HashSet<PieChartAnnotation>();
        HashSet<PieChartAnnotation> pieChartsToAdd = new HashSet<PieChartAnnotation>();
        PieChartAnnotation newPieChart;
        ArrayList<String> leftoverClusters = new ArrayList<String>();
        boolean pieChartsChanged = false;
        Position currentPos;

        for (PieChartAnnotation oldPie : pieChartWaypointSet) {
            adjacencyList.clear();
            visited.clear();
            positionsInPieChart.clear();
            leftoverClusters.clear();

            for (String cluster : oldPie.getClusters()) {
                currentPos = positionList.get(cluster);
                if (currentPos != null) {
                    positionsInPieChart.put(cluster, currentPos);
                } else {
                    // System.out.println("Found a dud! " + cluster);
                    leftoverClusters.add(cluster);
                    // System.out.println(positionList.toString());
                }
            }

            createAdjacencyList(positionsInPieChart, adjacencyList, visited);
            oldPie.getClusters().removeAll(leftoverClusters);

            for (String cluster : oldPie.getClusters()) {
                connectedComponent.clear();

                DFS(cluster, adjacencyList, visited, connectedComponent);

                // if there is more than one connected component, it means
                // that we need to re-create the pie charts
                if (connectedComponent.size() != oldPie.getClusters().size()) {

                    pieChartsToErase.add(oldPie);

                    ArrayList<Position> positions = new ArrayList<Position>();
                    for (String location : connectedComponent) {
                        if (positionList.get(location) != null) {
                            positions.add(positionList.get(location));
                        }
                    }

                    newPieChart = new PieChartAnnotation(connectedComponent,
                            positions, new AnnotationAttributes(),
                            GlobeVisualization.getVisualization());
                    pieChartsToAdd.add(newPieChart);
                    pieChartsChanged = true;
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

    // TODO - remove this or fix this
    private boolean splitPieCharts() {
        HashMap<String, HashSet<String>> adjacencyList = new HashMap<String, HashSet<String>>();
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        HashMap<String, Position> positionsInPieChart = new HashMap<String, Position>();
        ArrayList<String> connectedComponent = new ArrayList<String>();
        HashSet<PieChartAnnotation> pieChartsToErase = new HashSet<PieChartAnnotation>();
        HashSet<PieChartAnnotation> pieChartsToAdd = new HashSet<PieChartAnnotation>();
        PieChartAnnotation newPieChart;
        boolean pieChartsChanged = false;

        for (PieChartAnnotation oldPieChart : pieChartWaypointSet) {
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
                        newPieChart = new PieChartAnnotation(
                                connectedComponent, positions,
                                new AnnotationAttributes(),
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

    // TODO - there is still a bug with grouping - sometimes locations are
    // grouped with locations on the other side of the globe ... may be due to
    // the may points are projected
    private boolean groupPieCharts() {
        HashMap<String, HashSet<String>> adjacencyList = new HashMap<String, HashSet<String>>();
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        HashMap<String, Position> positionsForPieCharts = new HashMap<String, Position>();
        ArrayList<String> connectedComponent = new ArrayList<String>();
        HashSet<PieChartAnnotation> invisiblePieCharts = new HashSet<PieChartAnnotation>();
        HashSet<PieChartAnnotation> pieChartsToAdd = new HashSet<PieChartAnnotation>();
        PieChartAnnotation newPieChart;
        boolean pieChartsChanged = false;
        ArrayList<Position> positionsInConnectedComponent = new ArrayList<Position>();

        Set<PieChartAnnotation> visiblePieChartWaypointSet = new HashSet<PieChartAnnotation>();

        for (PieChartAnnotation renderable : initialClusterAnnotations.values()) {
            visiblePieChartWaypointSet.add(renderable);
            visited.put(renderable.getName(), false);
            positionsForPieCharts.put(renderable.getName(),
                    renderable.getPosition());
        }

        // add all the annotations to the set
        // for (Renderable renderable : globeViz.getAnnotationLayer()
        // .getRenderables()) {
        //
        // if (renderable instanceof CircleAnnotation) {
        //
        // CircleAnnotation oldPieChart = (CircleAnnotation) renderable;
        //
        // visiblePieChartWaypointSet.add(oldPieChart);
        //
        // visited.put(oldPieChart.getName(), false);
        //
        // positionsForPieCharts.put(oldPieChart.getName(),
        // oldPieChart.getPosition());
        //
        // // TODO - decide later if we leave this here or not - it
        // // optimizes computations, but it messes up grouping
        // // if (!positionIsVisible(oldPieChart.getPosition())) {
        // // // don't take hidden pie charts into consideration
        // // invisiblePieCharts.add(oldPieChart);
        // // }
        // }
        // }

        // remove all piecharts that aren't visible - we still keep them in the
        // invisiblePieCharts, so that we can render them again in the end
        // visiblePieChartWaypointSet.removeAll(invisiblePieCharts);

        // create a graph from the clusters in the pie chart;
        createAdjacencyList(positionsForPieCharts, adjacencyList, visited);

        for (PieChartAnnotation waypoint : visiblePieChartWaypointSet) {
            connectedComponent.clear(); // reuse the same structure
            positionsInConnectedComponent.clear();

            // compute the connected component for each unvisited node
            if (!visited.get(waypoint.getName())) {
                DFS(waypoint.getName(), adjacencyList, visited,
                        connectedComponent);

                // the component does not consist of a single cluster
                if (connectedComponent.size() > 1) {
                    newPieChart = null;

                    for (PieChartAnnotation oldPie : visiblePieChartWaypointSet) {
                        if (oldPie.containsSameClustersAs(connectedComponent)) {
                            // we can reuse the existing pie chart
                            newPieChart = oldPie;
                            break;
                        }
                    }

                    // that pie chart doesn't exist yet, we need to create it
                    if (newPieChart == null) {

                        for (String location : connectedComponent) {
                            positionsInConnectedComponent
                                    .add(retrievePositionFromPieList(location));
                        }
                        newPieChart = new PieChartAnnotation(
                                connectedComponent,
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
        for (PieChartAnnotation annotation : pieChartWaypointSet) {
            if (annotation.getName().equals(name)) {
                return annotation.getPosition();
            }
        }

        if (positionList.containsKey(name)) {
            return positionList.get(name);
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
        for (String location : positionList.keySet()) {

            for (String secondLocation : positionList.keySet()) {
                if (!secondLocation.equals(location)) {

                    Position p1 = positionList.get(location);
                    Position p2 = positionList.get(secondLocation);

                    distance = Utils.computeDistance(p1, p2, GlobeVisualization
                            .getVisualization().getModel().getGlobe(),
                            GlobeVisualization.getVisualization().getView());

                    minDistance = 2 * UIConstants.LOCATION_CIRCLE_SIZE;

                    // if the two clusters overlap
                    if (distance > 0 && distance <= minDistance) {
                        adjacencyList.get(location).add(secondLocation);
                        adjacencyList.get(secondLocation).add(location);
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
        PieChartAnnotation startPie, stopPie;
        double value1 = 0, value2 = 0;

        for (Link link : links) {

            // only create arcs between locations
            if ((link.getSource() instanceof LocationImpl)
                    && (link.getDestination() instanceof LocationImpl)) {

                startLocation = Utils.extractLocationName(((LocationImpl) link
                        .getSource()).getName());
                stopLocation = Utils.extractLocationName(((LocationImpl) link
                        .getDestination()).getName());

                startPie = stopPie = null;

                for (PieChartAnnotation pie : pieChartWaypointSet) {
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

                    pos1 = startPie.getPosition();
                    pos2 = stopPie.getPosition();

                    if (pos1 != null && pos2 != null) {
                        GlobeEdge reverseEdge = new GlobeEdge(
                                pos2,
                                pos1,
                                stopPie.getName() + " -> " + startPie.getName(),
                                false);
                        boolean isSecondEdge = tempEdges
                                .containsKey(reverseEdge);

                        GlobeEdge edge = new GlobeEdge(
                                pos1,
                                pos2,
                                startPie.getName() + " -> " + stopPie.getName(),
                                isSecondEdge);
                        // if the edge is already in the map, this just updates
                        // the value
                        if (tempEdges.get(edge) != null) {
                            tempEdges.put(edge, tempEdges.get(edge) + value1
                                    + value2);
                            numberOfSubedgesPerEdge.put(edge,
                                    numberOfSubedgesPerEdge.get(edge) + 1);
                        } else {
                            tempEdges.put(edge, value1 + value2);
                            numberOfSubedgesPerEdge.put(edge, 1);
                        }
                    }
                }
            }
        }
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
                globeViz.getMarkerLayer().setEnabled(true);
            }
        } else { // stop the particle timers
            if (particleMovementTimer.isRunning()) {
                particleMovementTimer.stop();
                globeViz.getMarkerLayer().setEnabled(false);
            }
        }
        updateData(root, true, true);
    }

    // TODO - remove this, it's used only for low level testing
    public void generateFixedLocations(RenderableLayer layer,
            GlobeVisualization globe) {

        Position pos1, pos2, pos3;
        PieChartAnnotation annotation;

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

        annotation = new PieChartAnnotation(pos1, dotAttributes,
                "Location1@Location1@ASD");
        annotation.getAttributes().setTextColor(Color.blue);
        layer.addRenderable(annotation);

        pieChartWaypointSet.add(annotation);

        annotation = new PieChartAnnotation(pos2, dotAttributes,
                "Location2@Location2@ASD");
        annotation.getAttributes().setTextColor(Color.blue);
        layer.addRenderable(annotation);

        pieChartWaypointSet.add(annotation);

        annotation = new PieChartAnnotation(pos3, dotAttributes,
                "Location3@Location3@ASD");
        annotation.getAttributes().setTextColor(Color.blue);
        layer.addRenderable(annotation);

        positionList.put("Location1@Location1@ASD", pos1);
        positionList.put("Location2@Location2@ASD", pos2);
        positionList.put("Location3@Location3@ASD", pos3);

        pieChartWaypointSet.add(annotation);
    }
}

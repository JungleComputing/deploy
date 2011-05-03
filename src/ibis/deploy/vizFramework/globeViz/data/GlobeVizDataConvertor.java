package ibis.deploy.vizFramework.globeViz.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import com.sun.corba.se.impl.ior.OldPOAObjectKeyTemplate;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.Renderable;
import ibis.deploy.gui.deployViz.helpers.VizUtils;
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

public class GlobeVizDataConvertor implements IDataConvertor {

    private AnnotationAttributes dotAttributes;
    private GlobeVisualization globeViz;
    private HashMap<String, Position> positionList;

    private double maximum = Double.MIN_VALUE;
    private HashMap<GlobeEdge, Double> globeEdges;
    private final Location root;
    private Set<CircleAnnotation> pieChartWaypointSet;

    public GlobeVizDataConvertor(GlobeVisualization globeViz, Location rootV) {

        this.globeViz = globeViz;
        globeViz.setDataConvertor(this);
        this.root = rootV;

        // initialize attributes
        dotAttributes = new AnnotationAttributes();
        dotAttributes.setDrawOffset(new Point(0, -16));
        dotAttributes.setSize(new Dimension(15, 15));
        dotAttributes.setBorderWidth(0);
        dotAttributes.setCornerRadius(0);
        dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
        dotAttributes.setImageSource(null);

        positionList = new HashMap<String, Position>();
        globeEdges = new HashMap<GlobeEdge, Double>();
        pieChartWaypointSet = new HashSet<CircleAnnotation>();

        globeViz.getVisualization().getInputHandler()
                .addMouseWheelListener(new MouseWheelListener() {

                    @Override
                    public void mouseWheelMoved(MouseWheelEvent event) {
                        int amount = event.getWheelRotation();
                        // wheen mover up - zoom in
                        if (amount < 0) {
                            splitPieCharts();
                        } else {
                            // wheel moved down - zoom out
                            groupPieCharts();
                        }

                        // connectClusters(root, UIConstants.LEVELS, false);
                    }
                });

        generateFixedLocations(globeViz.getAnnotationLayer(), globeViz);
    }

    public void updateData(Location root, boolean structureChanged) {
        // createAnnotation(root);

        // TODO - de refacut incat sa fie refolosit locationlist, de hotarat
        // daca resetez maximul de fiecare data sau nu
        // TODO - piechartwaypointset - trebuie updatat
        if (structureChanged) {
            positionList.clear();
            globeViz.clearAnnotationLayer();
            globeEdges.clear();

            // displays clusters
            generateLocationsAndConnections(root, " ", UIConstants.LEVELS,
                    structureChanged);
            redrawEdges();

            try {
                groupPieCharts();
            } catch (Exception e) {
                System.err.print(e.getMessage());
                // a nullpointerexception is thrown on the first update because
                // the
                // view isn't completely initialized
            }
        } else {

            // displays clusters
            generateLocationsAndConnections(root, " ", UIConstants.LEVELS,
                    structureChanged);
            redrawEdges();
        }
    }

    private void redrawEdges() {
        Color newColor;

        maximum = Double.MIN_VALUE;

        for (Double value : globeEdges.values()) {
            updateMax(value);
        }

        for (GlobeEdge edge : globeEdges.keySet()) {
            double ratio = globeEdges.get(edge) / maximum;
            newColor = VizUtils.blend(Color.red, Color.green, ratio, 0.7f);
            edge.updateAssociatedPolyline(globeViz, newColor);
        }

        globeViz.redraw();
    }

    private void generateLocationsAndConnections(Location root, String spacer,
            int level, boolean structureChanged) {
        ArrayList<Location> dataChildren = root.getChildren();
        if (dataChildren == null || dataChildren.size() == 0) {
            // ArrayList<Ibis> ibises = root.getAllIbises();
            return;
        }
        for (Location loc : dataChildren) {
            if (level == 0) {
                if (structureChanged) {
                    createAnnotation(loc, null);
                }
                generateConnections(loc);
            }

            generateLocationsAndConnections(loc, spacer.concat("  "),
                    level - 1, structureChanged);
        }
    }

    private void createAnnotation(Location loc, Color color) {
        CircleAnnotation annotation;
        LocationImpl finalLocation = (LocationImpl) loc;
        Position pos = new Position(LatLon.fromDegrees(
                finalLocation.getLatitude(), finalLocation.getLongitude()), 0);

        annotation = new CircleAnnotation(pos, dotAttributes,
                finalLocation.getName());
        if (color == null) {
            annotation.getAttributes().setTextColor(
                    Colors.fromLocation(loc.getName()));
        } else {
            annotation.getAttributes().setTextColor(color);
        }

        globeViz.getAnnotationLayer().addRenderable(annotation);

        positionList.put(loc.getName(), pos);
        pieChartWaypointSet.add(annotation);
    }

    private void generateConnections(Location loc) {
        Link[] links = loc.getLinks();
        String startLocation, stopLocation;
        Position pos1, pos2;
        double value = 0;

        for (Link link : links) {

            // only create arcs between locations
            if ((link.getSource() instanceof LocationImpl)
                    && (link.getDestination() instanceof LocationImpl)) {
                startLocation = ((LocationImpl) link.getSource()).getName();
                stopLocation = ((LocationImpl) link.getDestination()).getName();

                pos1 = positionList.get(startLocation);
                pos2 = positionList.get(stopLocation);

                for (Metric metric : link.getMetrics(LinkDirection.SRC_TO_DST)) {
                    try {
                        value = (Float) metric.getValue(MetricModifier.NORM,
                                MetricOutput.PERCENT);

                    } catch (OutputUnavailableException e) {
                        e.printStackTrace();
                    }
                }

                for (Metric metric : link.getMetrics(LinkDirection.DST_TO_SRC)) {
                    try {
                        value = (Float) metric.getValue(MetricModifier.NORM,
                                MetricOutput.PERCENT);
                    } catch (OutputUnavailableException e) {
                        e.printStackTrace();
                    }
                }

                if (pos1 != null && pos2 != null) {
                    GlobeEdge edge = new GlobeEdge(pos1, pos2);
                    // if the edge is already in the map, this just updates the
                    // value
                    globeEdges.put(edge, value);
                }
            }
        }
    }

    private void splitPieCharts() {
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
                                positions, new AnnotationAttributes());

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
    }

    private void groupPieCharts() {
        HashMap<String, HashSet<String>> adjacencyList = new HashMap<String, HashSet<String>>();
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
        HashMap<String, Position> positionsInPieChart = new HashMap<String, Position>();
        ArrayList<String> connectedComponent = new ArrayList<String>();
        HashSet<CircleAnnotation> invisiblePieCharts = new HashSet<CircleAnnotation>();
        HashSet<CircleAnnotation> pieChartsToAdd = new HashSet<CircleAnnotation>();
        CircleAnnotation newPieChart;
        boolean pieChartsChanged = true; // TODO - remove or add verification

        Set<CircleAnnotation> visiblePieChartWaypointSet = new HashSet<CircleAnnotation>();

        // add all the annotations to the set
        for (Renderable renderable : globeViz.getAnnotationLayer()
                .getRenderables()) {

            if (renderable instanceof CircleAnnotation) {

                CircleAnnotation oldPieChart = (CircleAnnotation) renderable;

                visiblePieChartWaypointSet.add(oldPieChart);

                visited.put(oldPieChart.getLocationName(), false);
                positionsInPieChart.put(oldPieChart.getLocationName(),
                        oldPieChart.getPosition());

                Vec4 vecPos = globeViz.getVisualization().getModel().getGlobe()
                        .computePointFromPosition(oldPieChart.getPosition());

                if (globeViz.getVisualization().getView()
                        .getFrustumInModelCoordinates().contains(vecPos)) {
                    System.out.println(oldPieChart.getLocationName()
                            + " is visible");
                } else {
                    System.out.println(oldPieChart.getLocationName()
                            + " is hidden --->");

                    // don't take hidden pie charts into consideration
                    invisiblePieCharts.add(oldPieChart);
                }
            }
        }
        //
        // for (CircleAnnotation oldPieChart : visiblePieChartWaypointSet) {
        // visited.put(oldPieChart.getLocationName(), false);
        // positionsInPieChart.put(oldPieChart.getLocationName(),
        // oldPieChart.getPosition());
        //
        // Vec4 vecPos = globeViz.getVisualization().getModel().getGlobe()
        // .computePointFromPosition(oldPieChart.getPosition());
        //
        // // globeViz.getVisualization().getModel().getGlobe().compute
        // // annotation.getPosition();
        // if (globeViz.getVisualization().getView()
        // .getFrustumInModelCoordinates().contains(vecPos)) {
        // System.out.println(oldPieChart.getLocationName()
        // + " is visible");
        // } else {
        // System.out.println(oldPieChart.getLocationName()
        // + " is hidden --->");
        //
        // // don't take hidden pie charts into consideration
        // invisiblePieCharts.add(oldPieChart);
        // }
        // }

        // remove all piecharts that aren't visible - we still keep them in the
        // invisiblePieCharts, so that we can add them again in the end
        visiblePieChartWaypointSet.removeAll(invisiblePieCharts);

        // create a graph from the clusters in the pie chart;
        createAdjacencyList(positionsInPieChart, adjacencyList, visited);

        for (CircleAnnotation waypoint : visiblePieChartWaypointSet) {
            connectedComponent.clear(); // reuse the same structure

            // compute the connected component for each unvisited node
            if (!visited.get(waypoint.getLocationName())) {
                DFS(waypoint.getLocationName(), adjacencyList, visited,
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

                        ArrayList<Position> positions = new ArrayList<Position>();
                        for (String location : connectedComponent) {
                            positions
                                    .add(retrievePositionFromPieList(location));
                        }
                        newPieChart = new CircleAnnotation(connectedComponent,
                                positions, new AnnotationAttributes());
                    }

                    pieChartsToAdd.add(newPieChart);
                    // pieChartsToErase.add(waypoint);
                } else {
                    pieChartsToAdd.add(waypoint);

                    // pieChartWaypointSet.add(new CircleAnnotation(positionList
                    // .get(waypoint), new AnnotationAttributes(),
                    // waypoint));
                }
            }
        }

        pieChartWaypointSet.clear();
        pieChartWaypointSet.addAll(pieChartsToAdd);
        // re-add all the invisible pie charts
        pieChartWaypointSet.addAll(invisiblePieCharts);

        if (pieChartsChanged) {
            globeViz.clearAnnotationLayer();
            globeViz.getAnnotationLayer().addRenderables(pieChartWaypointSet);
        }
    }

    private Position retrievePositionFromPieList(String name) {
        for (CircleAnnotation annotation : pieChartWaypointSet) {
            if (annotation.getLocationName().equals(name)) {
                return annotation.getPosition();
            }
        }

        return null;
    }

    // private void regroupPieCharts() {
    // HashMap<String, HashSet<String>> adjacencyList = new HashMap<String,
    // HashSet<String>>();
    //
    // HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    // pieChartWaypointSet.clear();
    //
    // // Set<CircleAnnotation> visibleWaypoints = new
    // // HashSet<CircleAnnotation>();
    //
    // ArrayList<String> connectedComponent = new ArrayList<String>();
    //
    // CircleAnnotation pieChart, oldPieChartWp;
    //
    // Iterable<Renderable> renderables = globeViz.getAnnotationLayer()
    // .getRenderables();
    //
    // Set<CircleAnnotation> oldPieCharts = new HashSet<CircleAnnotation>();
    //
    // // create the graph
    // createAdjacencyList(positionList, adjacencyList, visited);
    //
    // for (Renderable renderable : renderables) {
    // if (renderable instanceof CircleAnnotation) {
    // oldPieCharts.add((CircleAnnotation) renderable);
    // }
    // }
    //
    // for (CircleAnnotation waypoint : oldPieCharts) {
    // connectedComponent.clear(); // reuse the same structure
    //
    // // compute the connected component for each unvisited node
    // if (!visited.get(waypoint)) {
    // DFS(waypoint, adjacencyList, visited, connectedComponent);
    //
    // // the component does not consist of a single cluster
    // if (connectedComponent.size() > 1) {
    // pieChart = null;
    //
    // for (CircleAnnotation oldPie : oldPieCharts) {
    // oldPieChartWp = oldPie;
    // if (oldPieChartWp
    // .containsSameClustersAs(connectedComponent)) {
    // // we can reuse the existing pie chart
    // pieChart = oldPieChartWp;
    // break;
    // }
    // }
    //
    // // String name =
    // // printConnectedComponent(connectedComponent);
    //
    // // that pie chart doesn't exist yet, we need to create it
    // // TODO - attributes might be wrong
    // if (pieChart == null) {
    //
    // ArrayList<Position> positions = new ArrayList<Position>();
    // for (String location : connectedComponent) {
    // positions.add(positionList.get(location));
    // }
    // pieChart = new CircleAnnotation(connectedComponent,
    // positions, new AnnotationAttributes());
    // }
    // pieChartWaypointSet.add(pieChart);
    //
    // // iter = connectedComponent.iterator();
    //
    // // // don't display the waypoints for the clusters in the
    // // pie
    // // while (iter.hasNext()) {
    // // iter.next().show = false;
    // // }
    // } else {
    // pieChartWaypointSet.add(new CircleAnnotation(positionList
    // .get(waypoint), new AnnotationAttributes(),
    // waypoint));
    // }
    // }
    // }
    //
    // System.out.println("No of circles: " + pieChartWaypointSet.size());
    // globeViz.clearAnnotationLayer();
    // globeViz.getAnnotationLayer().addRenderables(pieChartWaypointSet);
    // }

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

                    distance = Utils.computeDistance(p1, p2, globeViz
                            .getVisualization().getModel().getGlobe(), globeViz
                            .getVisualization().getView());

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
            // ArrayList<Ibis> ibises = root.getAllIbises();
            return;
        }
        for (Location loc : dataChildren) {
            if (level == 0) {
                generatePieChartConnections(loc);
            }

            connectClusters(loc, level - 1, structureChanged);
        }
    }

    private void generatePieChartConnections(Location loc) {
        Link[] links = loc.getLinks();
        String startLocation, stopLocation;
        Position pos1, pos2;
        CircleAnnotation startPie, stopPie;
        double value = 0;

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
                    pos1 = startPie.getPosition();
                    pos2 = stopPie.getPosition();

                    value = 0;

                    for (Metric metric : link
                            .getMetrics(LinkDirection.SRC_TO_DST)) {
                        try {
                            value = (Float) metric.getValue(
                                    MetricModifier.NORM, MetricOutput.PERCENT);

                        } catch (OutputUnavailableException e) {
                            e.printStackTrace();
                        }
                    }

                    for (Metric metric : link
                            .getMetrics(LinkDirection.DST_TO_SRC)) {
                        try {
                            value = (Float) metric.getValue(
                                    MetricModifier.NORM, MetricOutput.PERCENT);
                        } catch (OutputUnavailableException e) {
                            e.printStackTrace();
                        }
                    }

                    if (pos1 != null && pos2 != null) {
                        GlobeEdge edge = new GlobeEdge(pos1, pos2);
                        // if the edge is already in the map, this just updates
                        // the
                        // value
                        globeEdges.put(edge, value);
                    }
                }
            }
        }
    }

    //
    // private void regroupClusters() {
    // HashMap<CircleAnnotation, HashSet<CircleAnnotation>> adjacencyList = new
    // HashMap<CircleAnnotation, HashSet<CircleAnnotation>>();
    //
    // HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    // Set<CircleAnnotation> visibleWaypoints = new HashSet<CircleAnnotation>();
    //
    // double distance, minDistance;
    //
    // Iterable<Renderable> renderables = globeViz.getAnnotationLayer()
    // .getRenderables();
    // for (Renderable renderable : renderables) {
    // if (renderable instanceof CircleAnnotation) {
    // visibleWaypoints.add((CircleAnnotation) renderable);
    // }
    // }
    //
    // System.out.println("No of circles: " + visibleWaypoints.size());
    //
    // for (CircleAnnotation waypoint : visibleWaypoints) {
    // adjacencyList.put(waypoint, new HashSet<CircleAnnotation>());
    // visited.put(waypoint, false);
    // }
    //
    // // create adjacency lists for all visible clusters (build the graph)
    // for (CircleAnnotation waypoint : visibleWaypoints) {
    // // waypoint.show = true;
    //
    // for (CircleAnnotation secondWaypoint : visibleWaypoints) {
    // if (secondWaypoint != waypoint) {
    // distance = waypoint.computeDistance(secondWaypoint,
    // globeViz.getVisualization().getModel().getGlobe(),
    // globeViz.getVisualization().getView());
    //
    // minDistance = 2 * UIConstants.LOCATION_CIRCLE_SIZE;
    //
    // // if the two clusters overlap
    // if (distance > 0 && distance <= minDistance) {
    // adjacencyList.get(waypoint).add(secondWaypoint);
    // adjacencyList.get(secondWaypoint).add(waypoint);
    // // System.out.println(waypoint.getLocationName() + "-->"
    // // + secondWaypoint.getLocationName() + " " + distance);
    // }
    // }
    // }
    // }
    //
    // ArrayList<CircleAnnotation> connectedComponent = new
    // ArrayList<CircleAnnotation>();
    // Set<CircleAnnotation> pieChartWaypointSet = new
    // HashSet<CircleAnnotation>();
    //
    // CircleAnnotation pieChart, oldPieChartWp;
    // Iterator<CircleAnnotation> iter;
    // // WaypointPainter<JXMapViewer> pieChartClusterPainter =
    // // getPieChartPainter();
    //
    // Set<CircleAnnotation> oldPieCharts = visibleWaypoints;
    //
    // for (CircleAnnotation waypoint : visibleWaypoints) {
    // connectedComponent.clear(); // reuse the same structure
    //
    // // compute the connected component for each unvisited node
    // if (!visited.get(waypoint)) {
    // DFS(waypoint, adjacencyList, visited, connectedComponent);
    //
    // // the component does not consist of a single cluster
    // if (connectedComponent.size() > 1) {
    // pieChart = null;
    // for (CircleAnnotation wp : oldPieCharts) {
    // oldPieChartWp = wp;
    // if (oldPieChartWp
    // .containsSameClustersAs(connectedComponent)) {
    // // we can reuse the existing pie chart
    // pieChart = oldPieChartWp;
    // break;
    // }
    // }
    //
    // String name = printConnectedComponent(connectedComponent);
    //
    // // that pie chart doesn't exist yet, we need to create it
    // // TODO - attributes might be wrong
    // if (pieChart == null) {
    // pieChart = new CircleAnnotation(connectedComponent,
    // new AnnotationAttributes(), name);
    // }
    // pieChartWaypointSet.add(pieChart);
    //
    // // iter = connectedComponent.iterator();
    //
    // // // don't display the waypoints for the clusters in the
    // // pie
    // // while (iter.hasNext()) {
    // // iter.next().show = false;
    // // }
    // } else {
    // pieChartWaypointSet.add(waypoint);
    // }
    // }
    // }
    //
    // globeViz.clearAnnotationLayer();
    // globeViz.getAnnotationLayer().addRenderables(pieChartWaypointSet);
    // }

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

    //
    // private String extractShortName(String name) {
    // StringTokenizer st = new StringTokenizer(name, "@");
    // if (st.hasMoreTokens()) {
    // st.nextElement();
    // if (st.hasMoreElements()) {
    // return st.nextToken();
    // }
    // }
    //
    // return "";
    // }

    // private String printConnectedComponent(ArrayList<CircleAnnotation> list)
    // {
    // System.out.print("[ ");
    // String name = "";
    // for (CircleAnnotation circle : list) {
    // System.out.print(extractShortName(circle.getLocationName()) + ", ");
    // name = name + " " + extractShortName(circle.getLocationName());
    // }
    // System.out.println(" ]");
    //
    // return name;
    // }

    public void forceUpdate() {
        updateData(root, true);
    }

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
        pos2 = new Position(LatLon.fromDegrees(0, 5), 0);
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

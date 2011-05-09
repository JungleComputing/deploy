package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Renderable;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;
import ibis.deploy.vizFramework.globeViz.data.GlobeVizDataConvertor;
import ibis.deploy.vizFramework.globeViz.viz.utils.RandomDataGenerator;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class GlobeVisualization extends JPanel {
    private static final long serialVersionUID = 1L;

    private GlobeAnnotation tooltipAnnotation;
    private CircleAnnotation lastSelectedDot;
    private WorldWindowGLCanvas worldWindCanvas;
    private RenderableLayer annotationLayer;
    private RenderableLayer polylineLayer;
    private boolean followTerrain = false;
    private GlobeVizDataConvertor convertor;
    private GUI gui;

    public GlobeVisualization(GUI gui) {

        this.gui = gui;
        
        // create a WorldWind main object
        worldWindCanvas = new WorldWindowGLCanvas();
        worldWindCanvas.setModel(new BasicModel());

        this.setLayout(new BorderLayout());
        this.add(worldWindCanvas, BorderLayout.CENTER);

        // initialize the annotation layer
        annotationLayer = new RenderableLayer();
        annotationLayer.setName("Locations");
        worldWindCanvas.getModel().getLayers().add(annotationLayer);
        
        // initialize the polylineLayer layer
        polylineLayer = new RenderableLayer();
        polylineLayer.setName("Locations");
        worldWindCanvas.getModel().getLayers().add(polylineLayer);

        // only used when we want fake-fake data :P
        // RandomDataGenerator.generateRandomDotsAndConnections(this);

        //RandomDataGenerator.generateFixedLocations(annotationLayer, this);

        createTooltip();

        // create a listener for displaying the tooltip
        worldWindCanvas.addSelectListener(new SelectListener() {

            public void selected(SelectEvent event) {
                if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                    highlight(event.getTopObject());
            }
        });
        
        

        // //temporarily disable some layers for debugging - TODO - remove
        // for (Layer layer : worldWindCanvas.getModel().getLayers()) {
        // if (layer.getName()
        // .equals(
        // "NASA Blue Marble Image")
        // || layer.getName().equals(
        // "Blue Marble (WMS) 2004")) {
        // layer.setEnabled(false);
        // }
        // }

    }

    public RenderableLayer getAnnotationLayer() {
        return annotationLayer;
    }

    public WorldWindowGLCanvas getVisualization() {
        return worldWindCanvas;
    }

    // tooltip initialization
    private void createTooltip() {

        // Initialize tooltip annotation
        tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0,
                0));

        // Create attributes for the tooltip --> Make sure to always set
        // imagesource to null, otherwise everything blows up :)
        AnnotationAttributes tooltipAttributes = new AnnotationAttributes();
        tooltipAttributes.setCornerRadius(10);
        tooltipAttributes.setInsets(new Insets(8, 8, 8, 8));
        tooltipAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .85f));
        tooltipAttributes.setDrawOffset(new Point(25, 25));
        tooltipAttributes.setDistanceMinScale(.5);
        tooltipAttributes.setDistanceMaxScale(2);
        tooltipAttributes.setDistanceMinOpacity(.5);
        tooltipAttributes.setLeaderGapWidth(14);
        tooltipAttributes.setDrawOffset(new Point(20, 40));
        tooltipAttributes.setImageSource(null);
        tooltipAttributes.setFont(Font.decode("Arial-BOLD-12"));
        tooltipAttributes.setTextColor(Color.WHITE);

        tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(10,
                100, 0), tooltipAttributes);
        tooltipAnnotation.getAttributes().setSize(new Dimension(150, 0));
        tooltipAnnotation.getAttributes().setVisible(false);
        tooltipAnnotation.setAlwaysOnTop(true);

        annotationLayer.addRenderable(tooltipAnnotation);
    }

    // displays the name of a location on mouse over
    private void highlight(Object o) {
        if (lastSelectedDot == o)
            return; // same thing selected

        if (lastSelectedDot != null) {
            lastSelectedDot.getAttributes().setHighlighted(false);
            lastSelectedDot = null;
            tooltipAnnotation.getAttributes().setVisible(false);
        }

        if (o != null && o instanceof CircleAnnotation) {
            lastSelectedDot = (CircleAnnotation) o;
            lastSelectedDot.getAttributes().setHighlighted(true);
            tooltipAnnotation.setText("<p>" + lastSelectedDot.getName()
                    + "</p>");
            tooltipAnnotation.setPosition(lastSelectedDot.getPosition());
            tooltipAnnotation.getAttributes().setVisible(true);
            worldWindCanvas.repaint();
        }
    }

    // computes a polyline between the two locations
    public UnclippablePolyline createArcBetween(Position pos1, Position pos2,
            Color color) {
        UnclippablePolyline polyline;
        ArrayList<Position> polylineList = new ArrayList<Position>();

        // add the control points to the list
        polylineList.addAll(doTheSplits(pos1, pos2,
                UIConstants.NUMBER_OF_CONTROL_POINTS, true));

        // add the points of the BSpline created using the control points.
        polylineList = BSpline.computePolyline(worldWindCanvas.getModel()
                .getGlobe(), polylineList);

        // The BSpline doesn't pass through the control points, so to force the
        // polyline to pass through the two locations we have to add them
        // separately to the list
        polylineList.add(0, pos1);
        polylineList.add(pos2);

        polyline = new UnclippablePolyline(polylineList);
        polyline.setColor(color);
        polyline.setLineWidth(3.0);
        polyline.setFollowTerrain(followTerrain);

        return polyline;
    }

    // calculates the interpolation point for pos1 and pos2
    private Position getMidPoint(Position pos1, Position pos2,
            boolean adjustHeight) {

        Position pos3 = Position.interpolateGreatCircle(0.5, pos1, pos2);
        if (adjustHeight) {
            double newHeight = LatLon.greatCircleDistance(pos1, pos2).degrees
                    * UIConstants.ARC_HEIGHT;
            pos3 = new Position(pos3.latitude, pos3.longitude, newHeight);
        }

        return pos3;
    }

    private ArrayList<Position> doTheSplits(Position pos1, Position pos2,
            int depth, boolean adjustHeight) {

        ArrayList<Position> l1, l2, list = new ArrayList<Position>();

        if (depth == 0) {
            list.add(pos1);
            list.add(pos2);
            return list;
        }

        Position pos3 = getMidPoint(pos1, pos2, adjustHeight);

        l1 = doTheSplits(pos1, pos3, depth - 1, false);
        l2 = doTheSplits(pos3, pos2, depth - 1, false);

        l1.remove(l1.size() - 1); // remove the last element, otherwise we'll
        // have the midpoint two times in the final
        // list
        list.addAll(l1);
        list.addAll(l2);

        return list;
    }

    public void clearAnnotationLayer() {
        // clear everything
        annotationLayer.removeAllRenderables();

        // add the tooltip back
        annotationLayer.addRenderable(tooltipAnnotation);
    }

    public void drawArc(Position pos1, Position pos2, Color color) {
        polylineLayer.addRenderable(createArcBetween(pos1, pos2, color));
    }

    public void drawArc(UnclippablePolyline line, Color color) {
        line.setColor(color);
        polylineLayer.addRenderable(line);
    }
    
    public void clearPolylineLayer(){
        polylineLayer.removeAllRenderables();
    }

    public void redraw() {
        worldWindCanvas.redraw();
    }

    public void setFollowTerrain(boolean value) {
        if (convertor != null) {
            followTerrain = value;
            convertor.forceUpdate();
            repaint();
        }
    }

    public void setDataConvertor(GlobeVizDataConvertor convertor) {
        this.convertor = convertor;
        convertor.setGUI(gui);
    }

    // /**
    // * Creates piecharts from the clusters that overlap. It first creates an
    // * undirected graph - every pair of clusters that overlap represents two
    // * nodes connected by an edge. After this, the connected components of
    // this
    // * graph are computed using DFS. Each connected component is either a
    // single
    // * cluster or a group of clusters represented by means of a pie chart.
    // */
    // private void regroupClusters() {
    // HashMap<CircleAnnotation, HashSet<CircleAnnotation>> adjacencyList = new
    // HashMap<CircleAnnotation, HashSet<CircleAnnotation>>();
    // // will be used durinf DFS
    // HashMap<CircleAnnotation, Boolean> visited = new
    // HashMap<CircleAnnotation, Boolean>();
    // Set<CircleAnnotation> visibleWaypoints = new HashSet<CircleAnnotation>();
    //
    // double distance, minDistance;
    //
    // Iterable<Renderable> renderables = annotationLayer.getRenderables();
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
    // worldWindCanvas);
    //
    // minDistance = 2 * UIConstants.LOCATION_CIRCLE_SIZE;
    //
    // if (distance <= minDistance) { // the two clusters overlap
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
    // }
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
    // // that pie chart doesn't exist yet, we need to create it
    // // TODO - attributes might be wrong
    // if (pieChart == null) {
    // pieChart = new CircleAnnotation(connectedComponent,
    // new AnnotationAttributes());
    // }
    // pieChartWaypointSet.add(pieChart);
    //
    // // iter = connectedComponent.iterator();
    //
    // // // don't display the waypoints for the clusters in the pie
    // // while (iter.hasNext()) {
    // // iter.next().show = false;
    // // }
    // } else {
    // pieChartWaypointSet.add(waypoint);
    // }
    //
    // }
    //
    // annotationLayer.removeAllRenderables();
    // createTooltip();
    // annotationLayer.addRenderables(pieChartWaypointSet);
    // }
    //
    // // recursive depth-first search
    // private void DFS(CircleAnnotation node,
    // HashMap<CircleAnnotation, HashSet<CircleAnnotation>> graph,
    // HashMap<CircleAnnotation, Boolean> visited,
    // ArrayList<CircleAnnotation> component) {
    // if (visited.get(node)) {
    // return;
    // }
    //
    // component.add(node); // add the node to the current connected component
    // visited.put(node, true);
    //
    // for (CircleAnnotation neighbour : graph.get(node)) {
    // if (!visited.get(neighbour)) {
    // DFS(neighbour, graph, visited, component);
    // }
    // }
    // }
}

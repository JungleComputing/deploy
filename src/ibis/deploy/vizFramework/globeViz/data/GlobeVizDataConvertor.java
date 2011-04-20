package ibis.deploy.vizFramework.globeViz.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
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

public class GlobeVizDataConvertor implements IDataConvertor {

    private AnnotationAttributes dotAttributes;
    private GlobeVisualization globe;
    private HashMap<String, Position> locationList;

    private double maximum = Double.MIN_VALUE;
    private HashMap<GlobeEdge, Double> globeEdges;
    private Location root;

    public GlobeVizDataConvertor(GlobeVisualization globeVis, Location root) {

        globe = globeVis;
        globe.setDataConvertor(this);
        this.root = root;

        // initialize attributes
        dotAttributes = new AnnotationAttributes();
        dotAttributes.setDrawOffset(new Point(0, -16));
        dotAttributes.setSize(new Dimension(15, 15));
        dotAttributes.setBorderWidth(0);
        dotAttributes.setCornerRadius(0);
        dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
        dotAttributes.setImageSource(null);

        locationList = new HashMap<String, Position>();
        globeEdges = new HashMap<GlobeEdge, Double>();
    }

    public void updateData(Location root, boolean structureChanged) {
        // createAnnotation(root);

        // TODO - de refacut incat sa fie refolosit locationlist, de hotarat
        // daca resetez maximul de fiecare data sau nu
        if (structureChanged) {
            locationList.clear();
            globe.clearAnnotationLayer();
            globeEdges.clear();
        }

        // displays clusters
        generateLocationsAndConnections(root, " ", 3, structureChanged);
        redrawEdges();
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
            edge.updateAssociatedPolyline(globe, newColor);
        }

        globe.redraw();
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
                    createAnnotation(loc);
                }
                generateConnections(loc);
            }

            generateLocationsAndConnections(loc, spacer.concat("  "),
                    level - 1, structureChanged);
        }
    }

    private void createAnnotation(Location loc) {
        CircleAnnotation annotation;
        LocationImpl finalLocation = (LocationImpl) loc;
        Position pos = new Position(LatLon.fromDegrees(
                finalLocation.getLatitude(), finalLocation.getLongitude()), 0);

        int idx = ((int) (Math.random() * 1000))
                % UIConstants.LOCATION_COLOR_LIST.length;

        annotation = new CircleAnnotation(pos, dotAttributes,
                finalLocation.getName());
        annotation.getAttributes().setTextColor(
                Colors.fromLocation(loc.getName()));

        globe.getAnnotationLayer().addRenderable(annotation);

        locationList.put(loc.getName(), pos);
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

                pos1 = locationList.get(startLocation);
                pos2 = locationList.get(stopLocation);

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

    private void updateMax(double value) {
        if (value > maximum) {
            maximum = value;
        }
    }
    
    public void forceUpdate(){
        updateData(root, true);
    }
}

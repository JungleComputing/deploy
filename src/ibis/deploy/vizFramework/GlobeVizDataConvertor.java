package ibis.deploy.vizFramework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import ibis.deploy.gui.deployViz.edgeBundles.BSplineEdgeItem;
import ibis.deploy.gui.deployViz.helpers.VizUtils;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Link.LinkDirection;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.collection.impl.LocationImpl;
import ibis.deploy.vizFramework.globeViz.viz.CircleAnnotation;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

public class GlobeVizDataConvertor implements IDataConvertor {

    private AnnotationAttributes dotAttributes;
    private GlobeVisualization globe;
    private HashMap<String, Position> locationList;

    @SuppressWarnings("deprecation")
    public GlobeVizDataConvertor(GlobeVisualization globeVis) {

        globe = globeVis;

        // initialize attributes
        dotAttributes = new AnnotationAttributes();
        dotAttributes.setLeader(FrameFactory.LEADER_NONE);
        dotAttributes.setDrawOffset(new Point(0, -16));
        dotAttributes.setSize(new Dimension(15, 15));
        dotAttributes.setBorderWidth(0);
        dotAttributes.setCornerRadius(0);
        dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
        dotAttributes.setImageSource(null);

        locationList = new HashMap<String, Position>();
    }

    public void updateData(Location root) {
        locationList.clear();
        globe.clearAnnotationLayer();
        // createAnnotation(root);

        // displays clusters
        generateLocationDots(root, " ", 3);
    }

    private void generateLocationDots(Location root, String spacer, int level) {
        ArrayList<Location> dataChildren = root.getChildren();
        //System.out.println(spacer + root.getName());
        if (dataChildren == null || dataChildren.size() == 0) {
            ArrayList<Ibis> ibises = root.getAllIbises();
            // createAnnotation(root);
            // generateConnections(root);
            // for (Ibis ibis : ibises) {
            // for (ibis.deploy.monitoring.collection.Metric metric : ibis
            // .getMetrics()) {
            //
            // if (metric.getDescription().getName().equals("CPU")) {
            // try {
            // float value = (Float) metric.getValue(
            // MetricModifier.NORM, MetricOutput.PERCENT);
            // System.out.println("CPU ---> "+ value);
            // } catch (OutputUnavailableException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
            // }
            // }
            return;
        }
        for (Location loc : dataChildren) {
            // if (level == 0) {
            createAnnotation(loc);
            generateConnections(loc);
            // }

            LocationImpl finalLocation = (LocationImpl) loc;

            generateLocationDots(loc, spacer.concat("  "), level - 1);
        }
    }

    private void generateConnections(Location loc) {
        Link[] links = loc.getLinks();
        String startLocation, stopLocation;
        Position pos1, pos2;

        for (Link link : links) {

            // only create arcs between locations
            if ((link.getSource() instanceof LocationImpl)
                    && (link.getDestination() instanceof LocationImpl)) {
                startLocation = ((LocationImpl) link.getSource()).getName();
                stopLocation = ((LocationImpl) link.getDestination()).getName();

                pos1 = locationList.get(startLocation);
                pos2 = locationList.get(stopLocation);

                // try {
                // Metric m = (Metric)link.getMetric(new
                // BytesReceivedPerSecond());
                // try {
                // System.out.println("-------->" +
                // m.getValue(MetricModifier.NORM,
                // MetricOutput.PERCENT));
                // } catch (OutputUnavailableException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                //
                // } catch (MetricNotAvailableException e1) {
                // // TODO Auto-generated catch block
                // e1.printStackTrace();
                // }

                for (Metric metric : link.getMetrics(LinkDirection.SRC_TO_DST)) {

                    try {
                        float value = (Float) metric.getValue(
                                MetricModifier.NORM, MetricOutput.PERCENT);
                        if (value != 0) {
                            System.out.println(value);
                        }
                    } catch (OutputUnavailableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    // if
                    // (metric.getDescription().getName().equals("Bytes_Sent_Per_Sec"))
                    // {
                    // try {
                    // float value = (Float) metric.getValue(
                    // MetricModifier.NORM, MetricOutput.PERCENT);
                    // System.out.println(value);
                    // } catch (OutputUnavailableException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    // }
                    // float currentValue;
                    // try {
                    //
                    // currentValue = (Float)
                    // metric.getValue(MetricModifier.NORM,
                    // MetricOutput.PERCENT);
                    // System.out.println("-------->" + currentValue);
                    // } catch (OutputUnavailableException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    //
                }

                for (Metric metric : link.getMetrics(LinkDirection.DST_TO_SRC)) {

                    try {
                        float value = (Float) metric.getValue(
                                MetricModifier.NORM, MetricOutput.PERCENT);
                        if (value != 0) {
                            System.out.println(value);
                        }
                    } catch (OutputUnavailableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // ratio = item.getLong(VizUtils.WEIGHT) * 1.0f /
                // VizUtils.MAX_EDGE_WEIGHT;
                // color = VizUtils.blend(startColor, stopColor, ratio,
                // ((BSplineEdgeItem) item).getAlpha());

                if (pos1 != null && pos2 != null) {
                    globe.drawArc(pos1, pos2);
                }
            }
        }

    }

    // public RenderableLayer updateLocationLayer(RenderableLayer layer) {
    // Position pos;
    // CircleAnnotation annotation;
    // int i;
    //
    // ArrayList<Position> positionList = RandomDataGenerator
    // .generatePositionList(UIConstants.NPOSITIONS);
    //
    // //generateRandomConnections(positionList, layer);
    //
    // for (i = 0; i < UIConstants.NPOSITIONS; i++) {
    // pos = positionList.get(i);
    //
    // layer.addRenderable(annotation);
    // }
    //
    // return layer;
    // }

    private void createAnnotation(Location loc) {
        CircleAnnotation annotation;
        LocationImpl finalLocation = (LocationImpl) loc;
        Position pos = new Position(LatLon.fromDegrees(
                finalLocation.getLatitude(), finalLocation.getLongitude()), 0);

        annotation = new CircleAnnotation(pos, dotAttributes,
                finalLocation.getName());
        annotation.getAttributes().setImageSource(
                UIConstants.LOCATIONS_SHAPE_LIST[3]);
        annotation.getAttributes().setTextColor(
                UIConstants.LOCATION_COLOR_LIST[3]);

        globe.getAnnotationLayer().addRenderable(annotation);

        locationList.put(loc.getName(), pos);
    }
}

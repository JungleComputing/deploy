package ibis.deploy.vizFramework.globeViz.viz.utils;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;

import ibis.deploy.vizFramework.globeViz.viz.CircleAnnotation;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

public class RandomDataGenerator {

    // generates a list with nPoints random locations
    public static ArrayList<Position> generatePositionList(int nPoints) {
        ArrayList<Position> positionList = new ArrayList<Position>();
        for (int i = 0; i < nPoints; i++) {
            positionList.add(generateRandomPosition());
        }

        return positionList;
    }

    // generates one random geographic location
    public static Position generateRandomPosition() {
        double lat, longit;
        lat = generateRandomLatitude();
        longit = generateRandomLongitude();
        LatLon pos1 = LatLon.fromDegrees(lat, longit);

        return new Position(pos1, 0);
    }

    public static double generateRandomLatitude() {
        return (Math.random() * 1000) % 180 - 90;
    }

    public static double generateRandomLongitude() {
        return (Math.random() * 1000) % 360 - 180;
    }

    public static void generateRandomDotsAndConnections(GlobeVisualization globe) {
        Position pos;
        CircleAnnotation annotation;
        int i;
        RenderableLayer layer = globe.getAnnotationLayer();

        AnnotationAttributes dotAttributes = new AnnotationAttributes();
        dotAttributes.setDrawOffset(new Point(0, -16));
        dotAttributes.setSize(new Dimension(15, 15));
        dotAttributes.setBorderWidth(0);
        dotAttributes.setCornerRadius(0);
        dotAttributes.setImageSource(null);
        dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));

        ArrayList<Position> positionList = RandomDataGenerator
                .generatePositionList(UIConstants.NPOSITIONS);

        generateRandomConnections(positionList, layer, globe);

        for (i = 0; i < UIConstants.NPOSITIONS; i++) {
            pos = positionList.get(i);

            annotation = new CircleAnnotation(pos, dotAttributes, "Location "
                    + i);
            annotation.getAttributes().setImageSource(
                    UIConstants.LOCATIONS_SHAPE_LIST[i % 7]);
            annotation.getAttributes().setTextColor(
                    UIConstants.LOCATION_COLOR_LIST[i % 7]);

            layer.addRenderable(annotation);
        }
    }

    // generates a random list of connections between the locations in the
    // positionList
    private static void generateRandomConnections(
            ArrayList<Position> positionList, RenderableLayer layer,
            GlobeVisualization globe) {

        int i, j;

        Position pos1, pos2;

        for (i = 0; i < positionList.size(); i++) {
            for (j = i + 1; j < positionList.size(); j++) {
                if (Math.random() > 0.5) {
                    pos1 = positionList.get(i);
                    pos2 = positionList.get(j);
                    layer.addRenderable(globe.createArcBetween(pos1, pos2,
                            new Color(0, 255, 0, 150)));
                }
            }
        }
    }

    public static void generateFixedLocations(RenderableLayer layer,
            GlobeVisualization globe) {

        Position pos1, pos2;
        CircleAnnotation annotation;
        
        AnnotationAttributes dotAttributes = new AnnotationAttributes();
        dotAttributes.setDrawOffset(new Point(0, -16));
        dotAttributes.setSize(new Dimension(15, 15));
        dotAttributes.setBorderWidth(0);
        dotAttributes.setCornerRadius(0);
        dotAttributes.setImageSource(null);
        dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
        
        pos1 = Position.ZERO;
        pos2 = new Position(LatLon.fromDegrees(0, 10), 0);
        
        annotation = new CircleAnnotation(pos1, dotAttributes, "Location1@Location1@ASD");
        annotation.getAttributes().setTextColor(
                Color.blue);
        layer.addRenderable(annotation);
        
        annotation = new CircleAnnotation(pos2, dotAttributes, "Location2@Location2@ASD");
        annotation.getAttributes().setTextColor(
                Color.blue);
        layer.addRenderable(annotation);
        
    }
}

package ibis.deploy.vizFramework.globeViz.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.globeViz.viz.UnclippablePolyline;
import ibis.deploy.vizFramework.globeViz.viz.markers.MarkerPool;
import ibis.deploy.vizFramework.globeViz.viz.markers.MovingMarker;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;

public class GlobeEdge {

    private final Position pos1, pos2;
    private UnclippablePolyline polyline;
    private final String name;
    private Color edgeColor = Color.green;
    //private ArrayList<MovingMarker> markers;
    private BasicMarkerAttributes attributes;
    private int currentIndex;
    private MarkerPool markerPool;

    // pos1 and pos2 need to be non-null
    public GlobeEdge(Position pos1, Position pos2, String name) {
        if (pos1 != null && pos2 != null) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        } else {
            this.pos1 = Position.ZERO;
            this.pos2 = Position.ZERO;
        }
        this.name = name;
        //markers = new ArrayList<MovingMarker>();
        attributes = new BasicMarkerAttributes(new Material(Color.GREEN),
                BasicMarkerShape.SPHERE, 0.5);
        // attributes.setMinMarkerSize(1);
        attributes.setMarkerPixels(8);
        currentIndex = 0;

        markerPool = new MarkerPool();
    }

    // the polyline is calculated only once, otherwise its color is changed
    public void updateAssociatedPolyline(GlobeVisualization globe, Color color,
            boolean forceEdgeRedraw) {
        edgeColor = color;
        if (polyline == null) {
            polyline = globe.createArcBetween(pos1, pos2, color);
            globe.drawArc(polyline, color);
        } else {
            if (forceEdgeRedraw) {
                globe.drawArc(polyline, color);
            } else {
                polyline.setColor(color);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof GlobeEdge) {
            GlobeEdge secondEdge = (GlobeEdge) other;
            if ((GlobeEdge.positionsEqual(pos1, secondEdge.getFirstPosition()) && GlobeEdge
                    .positionsEqual(pos2, secondEdge.getSecondPosition()))
                    || (GlobeEdge.positionsEqual(pos2,
                            secondEdge.getFirstPosition()) && GlobeEdge
                            .positionsEqual(pos1,
                                    secondEdge.getSecondPosition()))) {
                return true;
            }

        }
        return false;
    }

    // TODO - be careful, it's possible that this hash function will create
    // problems in the future when adding GlobeEdges to a hashmap
    @Override
    public int hashCode() {
        // int hash = 1;
        // hash = hash * 31 + pos1.hashCode();
        // hash = hash * 31 + (pos2 == null ? 0 : pos2.hashCode());
        // return hash;

        // We pulled this trick so that we can obtain the same hash code, no
        // matter what the edge direction is
        int hash1 = (41 * (41 + pos1.hashCode()) + pos2.hashCode());
        int hash2 = (41 * (41 + pos2.hashCode()) + pos1.hashCode());
        return Math.max(hash1, hash2);
    }

    public Position getFirstPosition() {
        return pos1;
    }

    public Position getSecondPosition() {
        return pos2;
    }

    // Returns true if the coordinates of the two Position objects are the same
    public static boolean positionsEqual(Position pos1, Position pos2) {
        if (pos1.getAltitude() == pos2.getAltitude()
                && pos1.getLatitude().equals(pos2.getLatitude())
                && pos1.getLongitude().equals(pos2.getLongitude())) {
            return true;
        }
        return false;
    }

    public String getName() {
        // return name;
        return pos1.toString() + " -> " + pos2.toString();
    }

    public UnclippablePolyline getPolyline() {
        return polyline;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public Marker createMarker(Position position) {

        MovingMarker marker = null;
        marker = markerPool.getMarker(position, attributes);
        //markers.add(marker);
        return marker;
    }

    public Vector<MovingMarker> getMarkers() {
        return markerPool.getActiveMarkers();
    }

    public BasicMarkerAttributes getAttributes() {
        return attributes;
    }

    public void moveMarkers(Position pos, Vector<Marker> markerVector) {

        MovingMarker marker;
        int idx;

        if (edgeColor != null) {
            attributes.setMaterial(new Material(edgeColor));
        }

        for (int i = 0; i < markerPool.getActiveMarkers().size(); i++) {
            marker = markerPool.getActiveMarkers().get(i);
            idx = marker.move();
            if (idx >= ((ArrayList<Position>) polyline.getPositions()).size()) {
                marker.resetIndex();
                // remove marker from layer
                markerVector.remove(marker);
                // return the marker to the pool
                markerPool.returnMarkerToPool(marker);

            } else {
                marker.setPosition(((ArrayList<Position>) polyline
                        .getPositions()).get(idx));
            }
        }

    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}

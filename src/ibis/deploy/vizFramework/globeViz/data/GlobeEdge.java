package ibis.deploy.vizFramework.globeViz.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import com.sun.org.apache.bcel.internal.generic.NEW;

import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.globeViz.viz.BSplinePolyline;
import ibis.deploy.vizFramework.globeViz.viz.markers.ParticlePool;
import ibis.deploy.vizFramework.globeViz.viz.markers.MovingParticle;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;

public class GlobeEdge {

    private final Position pos1, pos2;
    private BSplinePolyline polyline;
    private final String name;
    private Color edgeColor = Color.green;
    private Color particleColor = Color.green;
    private ParticlePool markerPool;
    private boolean isSecondEdge;
    private boolean lastStateFollowGrid = false;
    ArrayList<Position> positions; //TODO - maybe remove this

    // pos1 and pos2 need to be non-null
    public GlobeEdge(Position pos1, Position pos2, String name,
            boolean isSecondEdge) {
        if (pos1 != null && pos2 != null) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        } else {
            this.pos1 = Position.ZERO;
            this.pos2 = Position.ZERO;
        }
        this.name = name;
        this.isSecondEdge = isSecondEdge;
        markerPool = new ParticlePool();
    }

    public void removeAssociatedPolylineFromDisplay(GlobeVisualization viz){
        viz.removePolyline(polyline);
        for(MovingParticle particle: markerPool.getActiveMarkers()){
            viz.getMarkerLayer().removeMarker(particle);
        }
        
        markerPool.returnAllMarkersToPool();
    }
    
    // the polyline is calculated only once. If the polyline is already
    // calculated, only the color is changed.
    public void updateAssociatedPolyline(GlobeVisualization globe, Color color,
            boolean forceEdgeRedraw, boolean showParticles, boolean followGrid) {
        int size;
        particleColor = color;
        if (!showParticles) {
            edgeColor = color;
            size = UIConstants.EDGE_WITHOUT_PARTICLE_SIZE; 
        } else {
            edgeColor = UIConstants.EDGE_WITH_PARTICLES_COLOR;
            size = UIConstants.EDGE_WITH_PARTICLE_SIZE;
        }

        if (polyline == null || forceEdgeRedraw || lastStateFollowGrid) {
            if (!showParticles && followGrid) {
                polyline = globe.createArcBetween(positions, edgeColor, size);
                lastStateFollowGrid = true;
            } else {
                polyline = globe.createArcBetween(pos1, pos2, edgeColor, size,
                        isSecondEdge);
                globe.drawArc(polyline, edgeColor);
                lastStateFollowGrid = false;
            }
        } else {
            polyline.setColor(edgeColor);
            polyline.setLineWidth(size);
        }
    }

    public void updatePolylineFollowGrid(ArrayList<Position> positions) {
        this.positions = positions;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof GlobeEdge) {
            GlobeEdge secondEdge = (GlobeEdge) other;
            if ((GlobeEdge.positionsEqual(pos1, secondEdge.getFirstPosition()) && GlobeEdge
                    .positionsEqual(pos2, secondEdge.getSecondPosition()))) {
                // || (GlobeEdge.positionsEqual(pos2,
                // secondEdge.getFirstPosition()) && GlobeEdge
                // .positionsEqual(pos1,
                // secondEdge.getSecondPosition()))) {
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
        // matter what the edge direction is. TODO - if at some point edge
        // direction matters, change this
        int hash1 = (41 * (41 + pos1.hashCode()) + pos2.hashCode());
         //int hash2 = (41 * (41 + pos2.hashCode()) + pos1.hashCode());
        return hash1; 
        //Math.max(hash1, hash2);
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
        // return pos1.toString() + " -> " + pos2.toString();
        return name;
    }

    public BSplinePolyline getPolyline() {
        return polyline;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public Marker addMarkerToEdge() {
        MovingParticle marker = null;
        if (((ArrayList<Position>) polyline.getPositions()).size() > 0) {
            marker = markerPool.getMarker(
                    ((ArrayList<Position>) polyline.getPositions()).get(0),
                    particleColor);
        }
        return marker;
    }

    public Marker addMarkerToEdge(Position position) {

        MovingParticle marker = null;
        marker = markerPool.getMarker(position, particleColor);
        return marker;
    }

    public Vector<MovingParticle> getMarkers() {
        return markerPool.getActiveMarkers();
    }

    public void moveMarkers(GlobeVisualization globeViz) {

        MovingParticle marker;
        int idx;

        // if (edgeColor != null) {
        // attributes.setMaterial(new Material(edgeColor));
        // }

        // for (int i = 0; i < markerPool.getActiveMarkers().size(); i++) {
        // marker = markerPool.getActiveMarkers().get(i);
        // idx = marker.move();
        // if (idx >= ((ArrayList<Position>) polyline.getPositions()).size()) {
        // marker.resetIndex();
        // // remove marker from layer
        // globeViz.getMarkerLayer().removeMarker(marker);
        // // return the marker to the pool
        // markerPool.returnMarkerToPool(marker);
        //
        // } else {
        // marker.setPosition(((ArrayList<Position>) polyline
        // .getPositions()).get(idx));
        // }
        // }

        for (int i = 0; i < markerPool.getActiveMarkers().size(); i++) {
            marker = markerPool.getActiveMarkers().get(i);
            idx = marker.move();
            if (idx >= ((ArrayList<Position>) polyline.getPositions()).size()) {
                marker.resetIndex();
                // remove marker from layer
                globeViz.getMarkerLayer().removeMarker(marker);
                // return the marker to the pool
                markerPool.returnMarkerToPool(marker);

            } else {
                marker.setPosition(((ArrayList<Position>) polyline
                        .getPositions()).get(idx));
            }
        }

    }
}

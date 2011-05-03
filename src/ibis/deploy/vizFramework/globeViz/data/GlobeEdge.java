package ibis.deploy.vizFramework.globeViz.data;

import java.awt.Color;

import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.globeViz.viz.UnclippablePolyline;
import gov.nasa.worldwind.geom.Position;

public class GlobeEdge {

    private final Position pos1, pos2;
    private UnclippablePolyline polyline;

    // pos1 and pos2 need to be non-null
    public GlobeEdge(Position pos1, Position pos2) {
        if (pos1 != null && pos2 != null) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        } else {
            this.pos1 = Position.ZERO;
            this.pos2 = Position.ZERO;
        }
    }

    // the polyline is calculated only once, otherwise its color is changed
    public void updateAssociatedPolyline(GlobeVisualization globe, Color color) {
        if (polyline == null) {
            polyline = globe.createArcBetween(pos1, pos2, color);
            globe.drawArc(polyline, color);
        } else {
            polyline.setColor(color);
            //System.out.println("redraw");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GlobeEdge) {
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

        return (41 * (41 + pos1.hashCode()) + pos2.hashCode());
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
}

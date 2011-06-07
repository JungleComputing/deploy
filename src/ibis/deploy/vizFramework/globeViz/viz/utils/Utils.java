package ibis.deploy.vizFramework.globeViz.viz.utils;

import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

public class Utils {
    
    public static double computeDistance(Position p1, Position p2, Globe globe,
            View view) {

        if (p1 != null && p2 != null) {
            Vec4 point1 = globe.computePointFromPosition(p1);
            Vec4 point2 = globe.computePointFromPosition(p2);

            if (point1 != null && point2 != null) {

                if (view.getFrustumInModelCoordinates().contains(point1)
                        && view.getFrustumInModelCoordinates().contains(point2)) {
                    point1 = view.project(point1);
                    point2 = view.project(point2);

                    if (point1 != null && point2 != null) {
                        return point1.distanceTo3(point2);
                    }
                }
            }
        }

        return -1;
    }

    public static Vec4 fromPositionToScreen(Position pos, Globe globe, View view) {
        Vec4 vecPos;
        vecPos = globe.computePointFromPosition(pos);
        return view.project(vecPos);
    }
    
    public static Position fromScreenToPosition(double x, double y, double z, Globe globe, View view){
        Vec4 vecpos = view.unProject(new Vec4(x, y, z));
        Position pos = globe.computePositionFromPoint(vecpos);
        return new Position(pos.getLatitude(), pos.getLongitude(), 0);
        //return globe.computePositionFromPoint(temp);
    }
}

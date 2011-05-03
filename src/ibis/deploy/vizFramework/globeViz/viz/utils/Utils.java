package ibis.deploy.vizFramework.globeViz.viz.utils;

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
}

package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.util.ArrayList;

public class BSpline {
    public BSpline() {
    }

    public static ArrayList<Position> computePolyline(Globe globe,
            ArrayList<Position> controlPoints) {

        int nSteps = 10, index, ncurves, i, j;
        double xA, yA, zA, xB, yB, zB, xC, yC, zC, xD, yD, zD;
        double a0, a1, a2, a3, b0, b1, b2, b3, c0, c1, c2, c3;
        double x = 0, y = 0, z = 0, t;

        int[] xPoints;
        int[] yPoints;
        int[] zPoints;

        // the number of segments depends on the length of the arc
        if (controlPoints.size() >= 2) {
            double dist = LatLon.greatCircleDistance(controlPoints.get(0),
                    controlPoints.get(controlPoints.size() - 1)).degrees;
            nSteps = (int) dist / 10;
        }

        ArrayList<Position> polylineKnots;

        ncurves = controlPoints.size() - 3;

        xPoints = new int[ncurves * (nSteps + 1)];
        yPoints = new int[ncurves * (nSteps + 1)];
        zPoints = new int[ncurves * (nSteps + 1)];

        index = 0;

        for (i = 1; i < controlPoints.size() - 2; i++) {

            Vec4 point = globe.computePointFromPosition(controlPoints
                    .get(i - 1));

            xA = point.x;
            yA = point.y;
            zA = point.z;

            point = globe.computePointFromPosition(controlPoints.get(i));

            xB = point.x;
            yB = point.y;
            zB = point.z;

            point = globe.computePointFromPosition(controlPoints.get(i + 1));

            xC = point.x;
            yC = point.y;
            zC = point.z;

            point = globe.computePointFromPosition(controlPoints.get(i + 2));

            xD = point.x;
            yD = point.y;
            zD = point.z;

            /*
             * Apply this matrix to the three points and obtain line-matrix: |-1
             * 3 -3 1| | 3 -6 0 3| | -3 0 3 0| | 1 4 1 0| * 1/6
             */

            a3 = (-xA + 3 * (xB - xC) + xD) / 6;
            b3 = (-yA + 3 * (yB - yC) + yD) / 6;
            c3 = (-zA + 3 * (zB - zC) + zD) / 6;

            a2 = (xA - 2 * xB + xC) / 2;
            b2 = (yA - 2 * yB + yC) / 2;
            c2 = (zA - 2 * zB + zC) / 2;

            a1 = (-xA + xC) / 2;
            b1 = (-yA + yC) / 2;
            c1 = (-zA + zC) / 2;

            a0 = (xA + 4 * xB + xC) / 6;
            b0 = (yA + 4 * yB + yC) / 6;
            c0 = (zA + 4 * zB + zC) / 6;

            xPoints[index] = (int) a0;
            yPoints[index] = (int) b0;
            zPoints[index++] = (int) c0;

            t = 0;

            for (j = 1; j <= nSteps; j++) {

                t = (double) j / (double) nSteps;

                x = (a3 * t * t + a2 * t + a1) * t + a0;
                y = (b3 * t * t + b2 * t + b1) * t + b0;
                z = (c3 * t * t + c2 * t + c1) * t + c0;

                xPoints[index] = (int) x;
                yPoints[index] = (int) y;
                zPoints[index++] = (int) z;
            }

        }

        polylineKnots = new ArrayList<Position>();

        // for (i = 0; i < controlPoints.size(); i++) {
        // Vec4 point = globe.computePointFromPosition(controlPoints.get(i));
        // polylineKnots.add(globe.computePositionFromPoint(point));
        // }

        // we'll add the first and last control point to the list to make sure
        // that the curve passes through them
        // polylineKnots.addAll(controlPoints);
        for (i = 0; i < xPoints.length; i++) {
            polylineKnots.add(globe.computePositionFromPoint(new Vec4(
                    xPoints[i], yPoints[i], zPoints[i])));

            // System.out.println(polylineKnots.get(i).elevation);
        }
        // polylineKnots.add(controlPoints.get(controlPoints.size() - 1));

        
        // The BSpline doesn't pass through the control points, so to force the
        // polyline to pass through the two locations we have to add them
        // separately to the list  
        polylineKnots.add(0, controlPoints.get(0));
        polylineKnots.add(controlPoints.get(controlPoints.size() - 1));

        return polylineKnots;
    }
}

package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.helpers.ResourceWaypoint;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class PieChartWaypoint extends Waypoint {
    private int resourceRadius;

    public ArrayList<String> resourceNames = new ArrayList<String>();
    public ArrayList<ResourceWaypoint> resources = new ArrayList<ResourceWaypoint>();

    public static final int fixedGap = 2;
    private int variableGap = 0;

    public PieChartWaypoint(ArrayList<ResourceWaypoint> resourceList) {
        super();

        double radius = 0;
        double latitude = 0;
        double longitude = 0;
        // double totalNodes = 0;

        // compute the maximum radius in the group, position, number of nodes
        for (ResourceWaypoint waypoint : resourceList) {
            if (waypoint.getResource().isVisibleOnMap()) {
                if (waypoint.getRadius() > radius) {
                    radius = waypoint.getRadius();
                }

                latitude += waypoint.getPosition().getLatitude();
                longitude += waypoint.getPosition().getLongitude();
                // totalNodes += waypoint.getResource().getNodes();

                resourceNames.add(waypoint.getName());
                this.resources.add(waypoint);
            }
        }

        resourceRadius = (int) radius;
        latitude /= this.resources.size();
        longitude /= this.resources.size();
        setPosition(new GeoPosition(latitude, longitude));

        variableGap = (int) (0.3 * this.resources.size());
    }

    /**
     * Checks if the current piechart consists of the same resources that are
     * part of the resourceList. It assumes the lists don't contain duplicates.
     */
    public boolean containsSameResourcesAs(ArrayList<ResourceWaypoint> resourceList) {
        Iterator<ResourceWaypoint> it = resourceList.iterator();
        ResourceWaypoint cwp;

        if (resourceList.size() != resources.size()) {
            return false;
        }

        while (it.hasNext()) {
            cwp = it.next();
            if (!resources.contains(cwp)) {
                return false;
            }
        }

        return true;
    }

    public int getRadius() {
        return resourceRadius;
    }

    public int getPieChartGap() {
        return fixedGap + variableGap;
    }

    /**
     * @param mousePoint
     *            - the mouse point
     * @param referencePoint
     *            -the center of the pie chart
     * @return the resource which is represented by the slice over which the
     *         mouse is at the moment
     */
    public ResourceWaypoint getSelectedResource(Point2D mousePoint,
            Point2D referencePoint) {
        double angle = getPieAngle(mousePoint, referencePoint);
        double stepAngle = 2 * Math.PI / resources.size();

        int index = (int) Math.floor(angle / stepAngle);

        if (index >= resources.size()) {
            index = 0;
        }

        // slices are drawn in reverse order, fix index
        if (index != 0) {
            index = resources.size() - index;
        }

        return resources.get(index);
    }

    /**
     * @param initialPoint
     *            - the mouse point
     * @param referencePoint
     *            - the center of the pie chart
     * @return the location for the pie slice label
     */
    public Point getLabelLocation(Point2D initialPoint, Point2D referencePoint) {
        double angle = getPieAngle(initialPoint, referencePoint);
        double stepAngle = 2 * Math.PI / resources.size();

        int index = (int) Math.floor(angle / stepAngle);
        angle = index * stepAngle; // the middle of the slice

        Point2D point;

        ResourceWaypoint resource = getSelectedResource(initialPoint,
                referencePoint);

        point = new Point2D.Double(getRadius() + getPieChartGap(), 0);

        AffineTransform affineTransform = AffineTransform
                .getRotateInstance(angle);
        affineTransform.transform(point, point);

        if (angle >= Math.PI / 2 && angle <= 3 * Math.PI / 2) {
            return new Point(
                    (int) (point.getX() + referencePoint.getX() - 7 * resource
                            .getName().length()),
                    (int) (-point.getY() + referencePoint.getY()));
        } else {
            return new Point((int) (point.getX() + referencePoint.getX()),
                    (int) (-point.getY() + referencePoint.getY()));
        }
    }

    private double getPieAngle(Point2D initialPoint, Point2D referencePoint) {
        // y is converted from screen coord to Cartesian
        double y = -initialPoint.getY() + referencePoint.getY();
        double x = initialPoint.getX() - referencePoint.getX();
        double angle = Math.atan2(y, x);

        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        double stepAngle = 2 * Math.PI / resources.size();
        angle += stepAngle / 2; // resources are shifted with stepAngle/2
        // clockwise

        return angle;
    }

}

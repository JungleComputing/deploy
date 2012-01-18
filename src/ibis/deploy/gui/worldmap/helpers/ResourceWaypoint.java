package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.Resource;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class ResourceWaypoint extends Waypoint {

    private Resource resource;

    private int resourceCount;

    private boolean selected;

    private Dimension offset = new Dimension(0, 0);

    public boolean show = true;

    public ResourceWaypoint(Resource resource, boolean selected) {
        super(resource.getLatitude(), resource.getLongitude());
        this.selected = selected;
        this.resource = resource;
        this.resourceCount = 1;
    }
    
    int getResourceNodes() {
        return 100;
    }

    /**
     * Radius of a resource based on the number of nodes. Number of nodes
     * represents the AREA of the resource, so we convert to the radius.
     * 
     * @return the radius
     */
    public int getRadius() {


        return (int) Math.sqrt(50 + (getResourceNodes() * 13) / Math.PI);
    }

    public void resetOffset() {
        offset = new Dimension(0, 0);

    }

    public Dimension getOffset() {
        return offset;
    }

    public void addOffset(int x, int y) {
        offset.height += y;
        offset.width += x;
    }

    public void decreaseResourceCount() {
        resourceCount = Math.max(1, resourceCount - 1);
    }

    public void increaseResourceCount() {
        resourceCount = Math.min(resourceCount + 1, getResourceNodes());
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public String getName() {
        return resource.getName();
    }

    public boolean isSelected() {
        return selected;
    }

    public Resource getResource() {
        return resource;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Computes the distance to a second resource, in pixels
     */
    public double computeDistance(JXMapViewer map, ResourceWaypoint cwp) {
        Point2D p1 = map.convertGeoPositionToPoint(getPosition());
        p1.setLocation(p1.getX() + getOffset().width, p1.getY()
                + getOffset().height);
        Point2D p2 = map.convertGeoPositionToPoint(cwp.getPosition());
        p2.setLocation(p2.getX() + cwp.getOffset().width, p2.getY()
                + cwp.getOffset().height);

        return p1.distance(p2);
    }

}

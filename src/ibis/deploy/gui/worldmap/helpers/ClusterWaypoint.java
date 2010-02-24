package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.Cluster;
import ibis.deploy.gui.misc.Utils;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class ClusterWaypoint extends Waypoint {

    private Cluster cluster;

    private int resourceCount;

    private boolean selected;

    private Dimension offset = new Dimension(0, 0);
    
    public boolean show = true;

    public ClusterWaypoint(Cluster cluster, boolean selected) {
        super(
                cluster.getLatitude() == 0 && cluster.getLongitude() == 0 ? Utils.localClusterLatitude
                        : cluster.getLatitude(), cluster.getLatitude() == 0
                        && cluster.getLongitude() == 0 ? Utils.localClusterLongitude : cluster
                        .getLongitude());
        this.selected = selected;
        this.cluster = cluster;
        this.resourceCount = 1;
    }

    /**
     * Radius of a cluster based on the number of nodes. Number of nodes
     * represents the AREA of the cluster, so we convert to the radius.
     * 
     * @return
     */
    public int getRadius() {
        int nodes = cluster.getNodes();

        return (int) Math.sqrt(50 + (nodes * 13) / Math.PI);
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
        resourceCount = Math.min(resourceCount + 1, cluster.getNodes());
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public String getName() {
        return cluster.getName();
    }

    public boolean isSelected() {
        return selected;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * Computes the distance to a second cluster, in pixels
     */
    public double computeDistance(JXMapViewer map, ClusterWaypoint cwp)
    {
    	Point2D p1 = map.convertGeoPositionToPoint(getPosition());
        p1.setLocation(p1.getX() + getOffset().width, p1.getY()
                + getOffset().height);
        Point2D p2 = map.convertGeoPositionToPoint(cwp.getPosition());
        p2.setLocation(p2.getX() + cwp.getOffset().width, p2.getY()
                + cwp.getOffset().height);

        return p1.distance(p2);
    }

}

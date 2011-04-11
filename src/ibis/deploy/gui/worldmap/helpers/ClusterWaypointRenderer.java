package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.MapUtilities;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;
import ibis.deploy.util.Colors;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class ClusterWaypointRenderer implements WaypointRenderer {
    private boolean booleanSelect;

    public ClusterWaypointRenderer(boolean booleanSelect) {
        super();
        this.booleanSelect = booleanSelect;
    }

    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
        ClusterWaypoint cwp = (ClusterWaypoint) wp;

        if (cwp.show && cwp.getCluster().isVisibleOnMap()) {
            final int x = cwp.getOffset().width;
            final int y = cwp.getOffset().height;

            Color clusterBorderColor = cwp.getCluster()
                    .getColor();
            if (clusterBorderColor == null)
                clusterBorderColor = MapUtilities.defaultClusterBorderColor;

            Color clusterFillColor = Colors.getLightColor(cwp.getCluster()
                    .getColor());
            if (clusterFillColor == null)
                clusterFillColor = MapUtilities.defaultClusterFillColor;

            Color selectedBorderColor = MapUtilities.selectedClusterBorderColor;
            Color selectedFillColor = MapUtilities.selectedClusterFillColor;

            // draw circle
            final int radius = cwp.getRadius();
            final int diameter = 2 * radius;

            if (cwp.isSelected() && booleanSelect) {
                g.setPaint(selectedFillColor);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                g.setPaint(selectedBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            } else if (cwp.isSelected()) {
                g.setPaint(cwp.getCluster().getColor());
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, -(cwp
                        .getResourceCount() * 360)
                        / Math.max(1, cwp.getCluster().getNodes()));
                g
                        .setPaint(Colors.getLightColor(cwp.getCluster()
                                .getColor()));
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, 360
                        - (cwp.getResourceCount() * 360)
                        / Math.max(1, cwp.getCluster().getNodes()));
                g.setPaint(clusterBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            } else {
                g.setPaint(clusterFillColor);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                g.setPaint(clusterBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            }
        }
        return false;
    }

}

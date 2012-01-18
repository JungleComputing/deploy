package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.MapUtilities;
import ibis.deploy.util.Colors;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class ResourceWaypointRenderer implements WaypointRenderer {
    private boolean booleanSelect;

    public ResourceWaypointRenderer(boolean booleanSelect) {
        super();
        this.booleanSelect = booleanSelect;
    }

    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
        ResourceWaypoint cwp = (ResourceWaypoint) wp;

        if (cwp.show && cwp.getResource().isVisibleOnMap()) {
            final int x = cwp.getOffset().width;
            final int y = cwp.getOffset().height;

            Color resourceBorderColor = cwp.getResource()
                    .getColor();
            if (resourceBorderColor == null)
                resourceBorderColor = MapUtilities.defaultResourceBorderColor;

            Color resourceFillColor = Colors.getLightColor(cwp.getResource()
                    .getColor());
            if (resourceFillColor == null)
                resourceFillColor = MapUtilities.defaultResourceFillColor;

            Color selectedBorderColor = MapUtilities.selectedResourceBorderColor;
            Color selectedFillColor = MapUtilities.selectedResourceFillColor;

            // draw circle
            final int radius = cwp.getRadius();
            final int diameter = 2 * radius;

            if (cwp.isSelected() && booleanSelect) {
                g.setPaint(selectedFillColor);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                g.setPaint(selectedBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            } else if (cwp.isSelected()) {
                g.setPaint(cwp.getResource().getColor());
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, -(cwp
                        .getResourceCount() * 360)
                        / Math.max(1, cwp.getResourceNodes()));
                g
                        .setPaint(Colors.getLightColor(cwp.getResource()
                                .getColor()));
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, 360
                        - (cwp.getResourceCount() * 360)
                        / Math.max(1, cwp.getResourceNodes()));
                g.setPaint(resourceBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            } else {
                g.setPaint(resourceFillColor);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                g.setPaint(resourceBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            }
        }
        return false;
    }

}

package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.MapUtilities;
import ibis.deploy.gui.worldmap.helpers.ResourceWaypoint;
import ibis.deploy.gui.worldmap.helpers.PieChartWaypoint;
import ibis.deploy.util.Colors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class PieChartWaypointRenderer implements WaypointRenderer {
	public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
		PieChartWaypoint pieChartCwp = (PieChartWaypoint) wp;

		final int x = 0, y = 0;

		final int radius = pieChartCwp.getRadius();
		final int diameter = 2 * radius;

		int i;
		int angle = 0;
		int stepAngle = 360 / pieChartCwp.resources.size();

		Color resourceColor, resourceBorderColor;
		ResourceWaypoint resourcewp;

		for (i = 0; i < pieChartCwp.resources.size(); i++) {
			resourcewp = pieChartCwp.resources.get(i);
			resourceColor = null;

			// get the color of the slice and of its border
			if (resourcewp.isSelected()) {
				resourceColor = MapUtilities.selectedResourceFillColor;
				resourceBorderColor = MapUtilities.selectedResourceBorderColor;
			} else {
				resourceColor = Colors.getLightColor(resourcewp.getResource()
						.getColor());
				resourceBorderColor = resourcewp.getResource().getColor();
			}
			if (resourceColor == null) {
				resourceColor = MapUtilities.defaultResourceFillColor;
			}
			if (resourceBorderColor == null) {
				resourceBorderColor = MapUtilities.defaultResourceBorderColor;
			}

			g.rotate(Math.toRadians(angle));
			g.translate(pieChartCwp.getPieChartGap(), 0);

			// draw the pie slice
			g.setColor(resourceColor);
			g.fillArc(x - radius, y - radius, diameter, diameter,
					-stepAngle / 2, stepAngle);

			// draw the border of the pie slice
			Arc2D arc = new Arc2D.Double(x - radius, y - radius, diameter,
					diameter, -stepAngle / 2, stepAngle, Arc2D.PIE);
			g.setColor(resourceBorderColor);
			g.draw(arc);

			g.translate(-pieChartCwp.getPieChartGap(), 0);
			g.rotate(-Math.toRadians(angle));

			angle += stepAngle;
		}

		return false;
	}

}

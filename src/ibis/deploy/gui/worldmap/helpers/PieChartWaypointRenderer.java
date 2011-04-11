package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.MapUtilities;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;
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
		int stepAngle = 360 / pieChartCwp.clusters.size();

		Color clusterColor, clusterBorderColor;
		ClusterWaypoint clusterwp;

		for (i = 0; i < pieChartCwp.clusters.size(); i++) {
			clusterwp = pieChartCwp.clusters.get(i);
			clusterColor = null;

			// get the color of the slice and of its border
			if (clusterwp.isSelected()) {
				clusterColor = MapUtilities.selectedClusterFillColor;
				clusterBorderColor = MapUtilities.selectedClusterBorderColor;
			} else {
				clusterColor = Colors.getLightColor(clusterwp.getCluster()
						.getColor());
				clusterBorderColor = clusterwp.getCluster().getColor();
			}
			if (clusterColor == null) {
				clusterColor = MapUtilities.defaultClusterFillColor;
			}
			if (clusterBorderColor == null) {
				clusterBorderColor = MapUtilities.defaultClusterBorderColor;
			}

			g.rotate(Math.toRadians(angle));
			g.translate(pieChartCwp.getPieChartGap(), 0);

			// draw the pie slice
			g.setColor(clusterColor);
			g.fillArc(x - radius, y - radius, diameter, diameter,
					-stepAngle / 2, stepAngle);

			// draw the border of the pie slice
			Arc2D arc = new Arc2D.Double(x - radius, y - radius, diameter,
					diameter, -stepAngle / 2, stepAngle, Arc2D.PIE);
			g.setColor(clusterBorderColor);
			g.draw(arc);

			g.translate(-pieChartCwp.getPieChartGap(), 0);
			g.rotate(-Math.toRadians(angle));

			angle += stepAngle;
		}

		return false;
	}

}

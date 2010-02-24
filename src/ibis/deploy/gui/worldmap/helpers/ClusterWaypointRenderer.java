package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class ClusterWaypointRenderer implements WaypointRenderer 
{
    private boolean booleanSelect;
    
	public ClusterWaypointRenderer(boolean booleanSelect)
    {
    	super();
		this.booleanSelect = booleanSelect;
    }
	
	public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) 
	{
        ClusterWaypoint cwp = (ClusterWaypoint) wp;
        
        if(cwp.show && cwp.getCluster().isVisibleOnMap())
        {
        	final int x = cwp.getOffset().width;
            final int y = cwp.getOffset().height;

            Color clusterBorderColor = Utils.getColor(cwp.getCluster().getColorCode());
            if (clusterBorderColor == null) 
                clusterBorderColor = Utils.defaultClusterBorderColor;

            Color clusterFillColor = Utils.getLightColor(cwp.getCluster().getColorCode());
            if (clusterFillColor == null) 
                clusterFillColor = Utils.defaultClusterFillColor;

            // Color clusterBorderColor = new Color(100, 100, 255, 255);
            // Color clusterFillColor = new Color(100, 100, 255, 150);
            //Color clusterTextColor = new Color(255, 255, 255, 255);

            Color selectedBorderColor = Utils.selectedClusterBorderColor;
            // Color selectedArcColor = new Color(255, 100, 100, 200);
            Color selectedFillColor = Utils.selectedClusterFillColor;
            //Color selectedTextColor = new Color(255, 100, 100, 100);


            // String numberNodesString = ""
            // + ((cwp.getCluster().getNodes() > 0) ? cwp.getCluster()
            // .getNodes() : "n.a.");

            // draw circle
            final int radius = cwp.getRadius();
            final int diameter = 2 * radius;

            if (cwp.isSelected() && booleanSelect) 
            {
                g.setPaint(selectedFillColor);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                g.setPaint(selectedBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            } 
            else if (cwp.isSelected()) 
            {
                g.setPaint(Utils.getColor(cwp.getCluster().getColorCode()));
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, -(cwp
                        .getResourceCount() * 360)
                        / Math.max(1, cwp.getCluster().getNodes()));
                g
                        .setPaint(Utils.getLightColor(cwp.getCluster()
                                .getColorCode()));
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
            
            //int width, height;
            Font original = g.getFont();
            
//            //if(cwp.showLabel)
//	        {
//	            // draw cluster name
//	            String clusterName = cwp.getCluster().getName();
//	            
//	            g.setFont(original.deriveFont(Font.BOLD));
//	            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//	                    RenderingHints.VALUE_ANTIALIAS_ON);
//	
//	            g.setRenderingHint(RenderingHints.KEY_RENDERING,
//	                    RenderingHints.VALUE_RENDER_QUALITY);
//	
//	            width = (int) g.getFontMetrics()
//	                    .getStringBounds(clusterName, g).getWidth();
//	            height = (int) g.getFontMetrics().getStringBounds(clusterName,
//	                    g).getHeight();
//	
//	            g.setPaint(Color.BLACK);
//	            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
//	                    - 1 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 + 1, y + height / 2 + 8
//	                    - 1 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 + 1, y + height / 2 + 8
//	                    + 1 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
//	                    + 1 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
//	                    - 0 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 + 1, y + height / 2 + 8
//	                    - 0 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 + 0, y + height / 2 + 8
//	                    + 1 + radius); // shadow
//	            g.drawString(clusterName, x + -width / 2 - 0, y + height / 2 + 8
//	                    - 1 + radius); // shadow
//	
//	            g.setPaint(Color.WHITE);
//	
//	            g.drawString(clusterName, x + -width / 2, y + height / 2 + 8
//	                    + radius); // text
//	
//	            if (cwp.isSelected()) {
//	                g.setPaint(selectedTextColor);
//	            } else {
//	                g.setPaint(clusterTextColor);
//	
//	            }
//	            g.drawString(clusterName, x + -width / 2, y + height / 2 + 8
//	                    + radius); // text
//	            
//	        }
//
//            // draw a line to the original position of the cluster
//            // Point2D point = map.convertGeoPositionToPoint(cwp.getPosition());
//            // g.setPaint(Color.BLACK);
//            // g.drawLine(0, 0, x, y);
//
//            // draw usage
//            
//            if (cwp.isSelected() && !booleanSelect) {
//
//                String usageString = cwp.getResourceCount()
//                        + "/"
//                        + ((cwp.getCluster().getNodes() > 0) ? cwp.getCluster()
//                                .getNodes() : "n.a.");
//                width = (int) g.getFontMetrics()
//                        .getStringBounds(usageString, g).getWidth();
//                height = (int) g.getFontMetrics().getStringBounds(usageString,
//                        g).getHeight();
//
//                // draw text w/ shadow
//                g.setPaint(Color.BLACK);
//                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
//                        - 3 - 1); // shadow
//                g.drawString(usageString, x + -width / 2 + 1, y + height / 2
//                        - 3 - 1); // shadow
//                g.drawString(usageString, x + -width / 2 + 1, y + height / 2
//                        - 3 + 1); // shadow
//                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
//                        - 3 + 1); // shadow
//                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
//                        - 3 - 0); // shadow
//                g.drawString(usageString, x + -width / 2 + 1, y + height / 2
//                        - 3 - 0); // shadow
//                g.drawString(usageString, x + -width / 2 + 0, y + height / 2
//                        - 3 - 1); // shadow
//                g.drawString(usageString, x + -width / 2 + 0, y + height / 2
//                        - 3 + 1); // shadow
//                g.setPaint(Color.WHITE);
//                g.drawString(usageString, x + -width / 2, y + height / 2 - 3); // text
//                g.setPaint(clusterTextColor);
//                g.drawString(usageString, x + -width / 2, y + height / 2 - 3); // text
//
//            }

            g.setFont(original);
        }
        return false;
    }

}

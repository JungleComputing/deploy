package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;
import ibis.deploy.gui.worldmap.helpers.PieChartWaypoint;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class PieChartWaypointRenderer implements WaypointRenderer 
{
    private final Color clusterSelectedColor = new Color(255, 100, 100, 200);
    private final Color clusterDefaultColor = new Color(100, 100, 255, 255);
	
	public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) 
    {
        PieChartWaypoint cwp = (PieChartWaypoint) wp;
        
        final int x = 0;
        final int y = 0;
        
        final int radius = cwp.getRadius();
        final int diameter = 2 * radius;
        
        int i;
        int angle = 0;
        int newAngle;
        
        Color clusterColor; 
        ClusterWaypoint clusterwp;
        
        for(i = 0; i < cwp.percentages.size(); i++)
        {
        	clusterwp = cwp.clusters.get(i);
        	clusterColor = null;
        	
        	//set the color of the slice
        	if(clusterwp.isSelected())
    			clusterColor = clusterSelectedColor;
    		else
    			clusterColor = Utils.getLightColor(clusterwp.getCluster().getColorCode()); 
    		if (clusterColor == null) 
            {
            	clusterColor = clusterDefaultColor; 
            }
        	
    		newAngle = (int)(360 * cwp.percentages.get(i));
        	g.setPaint(clusterColor);
        	//draw the pie slice
        	g.fillArc(x - radius, y - radius, diameter, diameter, angle, newAngle);
        	
//        	g.setPaint(Color.black);
//        	g.fillArc(x - radius, y - radius, diameter, diameter, angle, 2);
        	
        	angle += newAngle;
        }
        
        g.setPaint(Color.black);
        g.drawOval(x - radius, y - radius, diameter, diameter);
        return false;
    }

}

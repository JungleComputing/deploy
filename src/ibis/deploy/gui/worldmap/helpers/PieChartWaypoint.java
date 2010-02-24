package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class PieChartWaypoint extends Waypoint 
{
    private int clusterRadius;
    
    public ArrayList<String> clusterNames = new ArrayList<String>();
    public ArrayList<ClusterWaypoint> clusters = new ArrayList<ClusterWaypoint>();
    
    public static final int fixedGap = 2;
    private int variableGap = 0;
	
    public PieChartWaypoint(ArrayList<ClusterWaypoint> clusterList) 
    {
    	super();
    	
    	double radius = 0;
    	double latitude = 0;
    	double longitude = 0;
    	double totalNodes = 0;
    	
    	// compute the maximum radius in the group, position, number of nodes
    	for(ClusterWaypoint waypoint:clusterList)
    	{
    		if(waypoint.getCluster().isVisibleOnMap())
    		{
	    		if(waypoint.getRadius() > radius) 
	    			radius = waypoint.getRadius();
	    		
	    		
	    		latitude += waypoint.getPosition().getLatitude();
	    		longitude += waypoint.getPosition().getLongitude();
	            totalNodes += waypoint.getCluster().getNodes();
	
	            clusterNames.add(waypoint.getName());
	            this.clusters.add(waypoint);
    		}
    	}
    	
    	clusterRadius = (int) radius;
    	latitude /= this.clusters.size();
    	longitude /= this.clusters.size();
    	setPosition(new GeoPosition(latitude, longitude)); 
    	
    	variableGap = (int)(0.3 * this.clusters.size());
    }
    /**
     * Checks if the current piechart consists of the same clusters
     * that are part of the clusterList. It assumes the lists don't contain duplicates.
     */
    public boolean containsSameClustersAs(ArrayList<ClusterWaypoint> clusterList)
    {
    	Iterator<ClusterWaypoint> it = clusterList.iterator();
    	ClusterWaypoint cwp;
    	
    	if(clusterList.size() != clusters.size())
    		return false;
    	
    	while(it.hasNext())
    	{
    		cwp = it.next();
    		if(!clusters.contains(cwp))
    			return false;
    	}
    	
    	return true;
    }
    
    public int getRadius() 
    {
       return clusterRadius; 
    }
    
    public int getPieChartGap()
    {
    	return fixedGap + variableGap;
    }
    
   /** 
    * @param mousePoint - the mouse point
    * @param referencePoint -the center of the pie chart
    * @return the cluster which is represented by the slice over which the mouse is at the moment 
    */
    public ClusterWaypoint getSelectedCluster(Point2D mousePoint, Point2D referencePoint)
    {
    	double angle = getPieAngle(mousePoint, referencePoint);
    	double stepAngle = 2*Math.PI / clusters.size();
    	
    	int index = (int) Math.floor(angle / stepAngle);
    	
    	if(index >= clusters.size())
    		index = 0;
    	
    	//slices are drawn in reverse order, fix index
    	if(index != 0)
    		index = clusters.size()-index;
    	
    	return clusters.get(index);
    }
    
    /**
     * @param initialPoint - the mouse point
     * @param referencePoint - the center of the pie chart
     * @return the location for the pie slice label
     */
    public Point getLabelLocation(Point2D initialPoint, Point2D referencePoint)
    {
    	double angle = getPieAngle(initialPoint, referencePoint);
    	double stepAngle = 2*Math.PI / clusters.size();
    	
    	int index = (int) Math.floor(angle / stepAngle);    	
    	angle = index*stepAngle; // the middle of the slice
    	
    	Point2D point;
    	
    	ClusterWaypoint cluster = getSelectedCluster(initialPoint, referencePoint); 

		point = new Point2D.Double(getRadius() + getPieChartGap(), 0);
		
    	AffineTransform affineTransform = AffineTransform.getRotateInstance(angle);
    	affineTransform.transform(point, point);
    	
    	if(angle >= Math.PI/2 && angle <= 3*Math.PI/2)
	    	return new Point((int) (point.getX() + referencePoint.getX() - 7*cluster.getName().length()), 
	    			(int)( - point.getY() + referencePoint.getY())); //y needs to be converted again Cartesian -> screen
    	else
    		return new Point((int) (point.getX() + referencePoint.getX()), 
	    			(int)( - point.getY() + referencePoint.getY())); //y needs to be converted again Cartesian -> screen
    }

    private double getPieAngle(Point2D initialPoint, Point2D referencePoint)
    {
    	double y = - initialPoint.getY() + referencePoint.getY(); //y is converted from screen coord to Cartesian
    	double x = initialPoint.getX() - referencePoint.getX();
    	double angle = Math.atan2(y ,x); 
    	
    	if(angle < 0)
    		angle += 2*Math.PI; 
    	
    	double stepAngle = 2*Math.PI / clusters.size();
    	angle += stepAngle / 2; //clusters are shifted with stepAngle/2 clockwise
    	
    	return angle;
    }

}

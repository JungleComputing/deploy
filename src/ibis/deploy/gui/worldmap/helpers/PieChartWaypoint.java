package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;

import java.util.ArrayList;
import java.util.Iterator;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class PieChartWaypoint extends Waypoint 
{

    private int clusterRadius;
    
    public ArrayList<Double> percentages = new ArrayList<Double>();
    public ArrayList<String> clusterNames = new ArrayList<String>();
    public ArrayList<ClusterWaypoint> clusters = new ArrayList<ClusterWaypoint>();
	
    public PieChartWaypoint(ArrayList<ClusterWaypoint> clusters) 
    {
    	super();
    	
    	double radius = 0;
    	double latitude = 0;
    	double longitude = 0;
    	double totalNodes = 0;
    	double percentage;
    	
    	// compute the maximum radius in the group, position, number of nodes
    	for(ClusterWaypoint waypoint:clusters)
    	{
    		if(waypoint.getRadius() > radius) 
    			radius = waypoint.getRadius();
    		
    		
    		latitude += waypoint.getPosition().getLatitude();
    		longitude += waypoint.getPosition().getLongitude();
            totalNodes += waypoint.getCluster().getNodes();
            
            percentages.add(waypoint.getCluster().getNodes()+0.0);
            clusterNames.add(waypoint.getName());
            this.clusters.add(waypoint);
    	}
    	
    	//for each cluster compute the percentage it represents from the total number of nodes
    	for(int i = 0; i < percentages.size(); i++)
    	{
    		percentage = percentages.get(i);
    		percentage /= totalNodes;
    		percentages.set(i, percentage);
    	}
    	
    	clusterRadius = (int) radius;
    	latitude /= clusters.size();
    	longitude /= clusters.size();
    	setPosition(new GeoPosition(latitude, longitude));          
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

}

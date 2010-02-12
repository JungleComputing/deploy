package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.MapUtilities;
import ibis.deploy.gui.worldmap.WorldMapPanel;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;
import ibis.deploy.gui.worldmap.helpers.PieChartWaypoint;
import ibis.deploy.gui.worldmap.helpers.ToolTipPainter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

public final class WorldMap extends JXMapKit 
{
	/**
     * 
     */
    private static final long serialVersionUID = -6194956781979564591L;
    private boolean initialized = false;
//    private static final int MAX_DEPTH = 5;
//    private static final double MIN_DISTANCE = 3.5;

    private static final int WIDTH = 256;
    private static final int HEIGHT = 256;
    
    private Set<Waypoint> waypoints;

    public WorldMap(WorldMapPanel parentPanel, int zoom) 
    {
        // create loading image in color of background
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++)
                image.setRGB(x, y, Color.decode("#99b3cc").getRGB());
        }

        getMainMap().setLoadingImage(image);

        // debug: show tiles borders and coordinates
        //getMainMap().setDrawTileBorders(true);
        
        //initialization
        MapUtilities.register(this);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setTileFactory(MapUtilities.getDefaultTileFactory());
        setMiniMapVisible(false);
        setAddressLocationShown(false);
        getMainMap().setZoom(zoom);
        getMainMap().setCenterPosition(MapUtilities.INITIAL_MAP_CENTER);
        getMainMap().setHorizontalWrapped(false);
        
        //keep a reference to the list of waypoints
        this.waypoints = parentPanel.getWaypoints();
        
        //create overlay painters for the map
        CompoundPainter<JXMapViewer> cp = new CompoundPainter<JXMapViewer>();
        
        //normal waypoint painter
        WaypointPainter<JXMapViewer> painter = new WaypointPainter<JXMapViewer>();
        painter.setRenderer(new ClusterWaypointRenderer(parentPanel.getBooleanSelect()));
        painter.setWaypoints(waypoints);
        
        //pie chart waypoint painter which contains the other three painters
        WaypointPainter<JXMapViewer> pieChartClusterPainter = new WaypointPainter<JXMapViewer>();
        pieChartClusterPainter.setRenderer(new PieChartWaypointRenderer());
        
        //tooltip painter for displaying the names of the clusters
        ToolTipPainter<JXMapViewer> tooltipPainter = new ToolTipPainter<JXMapViewer>(parentPanel);
        
        //add cluster painter on 1st position, piechart painter on 2nd and tooltip painter on 3rd
        cp.setPainters(painter, pieChartClusterPainter, tooltipPainter);
        cp.setCacheable(false); //so that the overlay is repainted when the user pans    
        
        //add the overlays to the  map
        getMainMap().setOverlayPainter(cp);
    }

    private void doFit() {
        for (Waypoint waypoint : waypoints) {
            ((ClusterWaypoint) waypoint).resetOffset();
        }
//        for (Waypoint currentWaypoint : waypoints) {
//            for (Waypoint otherWaypoint : waypoints) {
//                if (currentWaypoint != otherWaypoint) {
//                    adjustPosition((ClusterWaypoint) currentWaypoint,
//                           (ClusterWaypoint) otherWaypoint, 0);
//                }
//            }
//        } 
    }

    @Override
    public void setZoom(int zoom) {
        if(waypoints != null) //change the zoom only after initialization
        {
	    	doFit();
	        super.setZoom(zoom);
	        regroupClusters();
	        repaint();
        }
    }
    
    /**
     * Based on the current zoom level, it sets the maximum size for the map
     * @param zoom
     */
    public void adjustMapSize()
    {
    	int zoom = getMainMap().getZoom();
    	//calculate actual map size
    	Dimension mapSize = getMainMap().getTileFactory().getMapSize(zoom);
    	int mapWidth = (int)mapSize.getWidth() * getMainMap().getTileFactory().getTileSize(zoom);
    	int mapHeight = (int) mapSize.getHeight() * getMainMap().getTileFactory().getTileSize(zoom);
    	
    	Dimension newSize = new Dimension(mapWidth, mapHeight);
    	
    	setPreferredSize(newSize); 
    	setMaximumSize(newSize);
    	
    	revalidate(); // revalidate to force the layout manager to recompute sizes
    	
    	getMainMap().setCenter(getMainMap().getCenter()); //the map doesn't automatically center itself       	
    }
    
    /**
     * Sets the zoom level to the minimum value which allows all the clusters to be displayed
     */
    public void setZoomRelativeToClusters()
    {
    	Set<GeoPosition> positions = new HashSet<GeoPosition>();
    	Iterator<Waypoint> iterator = waypoints.iterator();
    	
    	while(iterator.hasNext())
    		positions.add(iterator.next().getPosition());
    	
    	//this method only increases the zoom until all clusters are visible
    	//if the zoom level is high enough to make everything visible, it does nothing
    	getMainMap().calculateZoomFrom(positions);
    	
    	//if the zoom level is too large - decrease it in order to only show the 
    	//part of the map which contains clusters
    	calculateZoomDecreaseFrom(positions);
    }
    
    /**
     * Decreases the zoom level until minimum zoom for all clusters to be visible is reached.
     * Based on the calculateZoomFrom method from the JXMapViewer.
     * @param positions - set of positions for the clusters
     */
    private void calculateZoomDecreaseFrom(Set<GeoPosition> positions) 
    {
        //if there's a single node, just set zoom level to 1 to make that area visible
        if(positions.size() < 2) 
        {
        	setZoom(1);
            return;
        }
        
        int zoom = getMainMap().getZoom();
        Rectangle2D bounds = generateBoundingRect(positions);
        
        int count = 0;
        
        //zoom in as long as all nodes are still contained in the viewport
        while(getMainMap().getViewportBounds().contains(bounds)) 
        {
            //calculate the position of the center of the new bounding rectangle
            Point2D center = new Point2D.Double(bounds.getX() + bounds.getWidth()/2,
                    							bounds.getY() + bounds.getHeight()/2);
            
            //transform it to geographical coordinates
            GeoPosition centerpx = getMainMap().getTileFactory().pixelToGeo(center,zoom);
            
            setCenterPosition(centerpx);
            count++;
            if(count > 30) break;
            
            //after recenter, zoom level is too low, viewport no longer contains all points
            if(!getMainMap().getViewportBounds().contains(bounds)) 
                break;
            
            zoom = zoom - 1;
            if(zoom < 1) //we've reached the lowest zoom level
                break;
            
            setZoom(zoom);
            bounds = generateBoundingRect(positions);
        }
        //increase zoom with one more level, to make sure all clusters are visible
        setZoom(zoom+1);
    }
    
    
    /**
     * Generate bounding rectangle for a set of points
     */
    private Rectangle2D generateBoundingRect(final Set<GeoPosition> positions) 
    {
        int zoom = getMainMap().getZoom();
    	Point2D initialPoint = getMainMap().getTileFactory().geoToPixel(positions.iterator().next(), zoom);
        Rectangle2D rect = new Rectangle2D.Double(initialPoint.getX(), initialPoint.getY(),0,0);
        
        for(GeoPosition position : positions) 
        {
            Point2D point = getMainMap().getTileFactory().geoToPixel(position, zoom);
            rect.add(point);
        }
        return rect;
    }

    public void paint(Graphics g) 
    {
        if (!initialized) {
            doFit();
            setZoomRelativeToClusters(); 
            initialized = true;
        }
        adjustMapSize();
        super.paint(g);
    }

//    private void adjustPosition(ClusterWaypoint waypoint,
//            ClusterWaypoint otherWaypoint, int depth) {
//        // take already known offset into account
//        Point2D p1 = getMainMap().convertGeoPositionToPoint(
//                waypoint.getPosition());
//        p1.setLocation(p1.getX() + waypoint.getOffset().width, p1.getY()
//                + waypoint.getOffset().height);
//        Point2D p2 = getMainMap().convertGeoPositionToPoint(
//                otherWaypoint.getPosition());
//        p2.setLocation(p2.getX() + otherWaypoint.getOffset().width, p2
//                .getY()
//                + otherWaypoint.getOffset().height);
//
//        double distance = p1.distance(p2);
//        double minDistance = waypoint.getRadius()
//                + otherWaypoint.getRadius() + 20;
//        if (distance < minDistance) {
//            // move both waypoints in half the overlap size in the
//            // proper direction
//            double overlap = 0.5 * (minDistance - distance) + MIN_DISTANCE;
//            double deltaX = p1.getX() - p2.getX();
//            double deltaY = p1.getY() - p2.getY();
//            waypoint.addOffset((int) ((overlap / distance) * deltaX),
//                    (int) ((overlap / distance) * deltaY));
//            otherWaypoint.addOffset((int) ((overlap / distance) * -deltaX),
//                    (int) ((overlap / distance) * -deltaY));
//            for (Waypoint thirdWaypoint : waypoints) {
//                if (thirdWaypoint != waypoint
//                        && thirdWaypoint != otherWaypoint
//                        && depth < MAX_DEPTH) {
//                    adjustPosition(waypoint,
//                            (ClusterWaypoint) thirdWaypoint, depth + 1);
//                    adjustPosition(otherWaypoint,
//                            (ClusterWaypoint) thirdWaypoint, depth + 1);
//                }
//            }
//        }
//    }
    
    /**
     * Updates the list of labels that is shown when the mouse is
     * over a cluster or a pie chart
     */
    public void updateTooltipLabels(Point mousePoint)
    {
    	Point2D clusterPoint = null;
        Point2D closestPoint = null;
        
        double dist;
        
    	ToolTipPainter<JXMapViewer> labelPainter = getTooltipPainter();
    	WaypointPainter<JXMapViewer> pieChartPainter = getPieChartPainter();
					
    	if(labelPainter != null && pieChartPainter != null)
    	{
    		ArrayList<String> labels = labelPainter.labels;
            labels.clear();
            
            Point location;
            ClusterWaypoint cwp;
            
            Rectangle mapBounds = getMainMap().getBounds(); 
            
            //first check is the mouse is over one of the normal cluster waypoints
    		for(Waypoint wp : waypoints)
            {
            	cwp = (ClusterWaypoint) wp;
            	if(cwp.show)//only check if the waypoint is displayed
            	{
                	clusterPoint = getMainMap().convertGeoPositionToPoint(wp.getPosition());
                	
                	//only take the cluster into consideration if it's within the visible bounds
                	if(clusterPoint.getX() > mapBounds.getX() &&
                			clusterPoint.getX() < mapBounds.getX()+mapBounds.getWidth() &&
                			clusterPoint.getY() > mapBounds.getY() &&
                			clusterPoint.getY() < mapBounds.getX()+mapBounds.getHeight())
                	{
	                	dist = clusterPoint.distance(mousePoint);
	                	
	                	if(dist <= cwp.getRadius()) 
	                	{
	        				closestPoint = clusterPoint;
	        				labels.add(cwp.getName());
	        				break;
	                	}
                	}
            	}
            }
            
            if(closestPoint != null) // the mouse was over one of the clusters
            {
            	location = new Point((int)closestPoint.getX(), (int)closestPoint.getY());
            	labelPainter.setLocation(location);
            }
            else //also check the pie chart waypoints
        	{
            	PieChartWaypoint piechartwp;
            	
            	for(Waypoint pwp : pieChartPainter.getWaypoints())
                {
                	piechartwp = (PieChartWaypoint) pwp;
                	clusterPoint = getMainMap().convertGeoPositionToPoint(pwp.getPosition());
                	
                	//only continue checking if the center of the piechart is in the visible area 
                	if(clusterPoint.getX() > mapBounds.getX() &&
                			clusterPoint.getX() < mapBounds.getX()+mapBounds.getWidth() &&
                			clusterPoint.getY() > mapBounds.getY() &&
                			clusterPoint.getY() < mapBounds.getX()+mapBounds.getHeight())
                	{
	                	dist = clusterPoint.distance(mousePoint);
	                	
	                	if(dist <= piechartwp.getRadius())
	                	{
	        				closestPoint = clusterPoint;
	        				labels.addAll(piechartwp.clusterNames);
	        				break;
	                	}
                	}
                }
            	
            	if(closestPoint != null)//the mouse was over one of the piecharts
            	{
                	location = new Point((int)closestPoint.getX(), (int)closestPoint.getY());
                	labelPainter.setLocation(location);
                }
            	else //the mouse wasn't over anything        	
            		labelPainter.setLocation(null);
        	}
		}
    	repaint();
    }
    
    /**
     * Creates piecharts from the clusters that overlap.
     * It first creates an undirected graph - every pair of clusters that overlap represents
     * two nodes connected by an edge. After this, the connected components of this graph 
     * are computed using DFS. Each connected component is either a single cluster
     * or a group of clusters represented by means of a pie chart.
     */
	private void regroupClusters()
	{
		HashMap<ClusterWaypoint, HashSet<ClusterWaypoint>> set = new HashMap<ClusterWaypoint, HashSet<ClusterWaypoint>>();
		HashMap<Waypoint, Boolean> visited = new HashMap<Waypoint, Boolean>(); //will be used during DFS
		
		double distance, minDistance;
		ClusterWaypoint cwptmp;
		
		for(Waypoint waypoint : waypoints)
		{
			set.put((ClusterWaypoint)waypoint, new HashSet<ClusterWaypoint>());
			visited.put(waypoint, false);
		}
		
		//create adjacency lists for all clusters (build the graph)
		for(Waypoint waypoint : waypoints)
		{
			ClusterWaypoint cwp1 = (ClusterWaypoint) waypoint;
			cwp1.show = true;
			
			for(Waypoint secondWaypoint: waypoints)
			{
				if(secondWaypoint != waypoint)
				{	
    				ClusterWaypoint cwp2 = (ClusterWaypoint) secondWaypoint;
    				distance = cwp1.computeDistanceTo(getMainMap(), cwp2);
    	            minDistance = cwp1.getRadius()+ cwp2.getRadius();
    	            
    	            if(distance <= minDistance)//the two clusters overlap
    	            {
    	            	set.get(cwp1).add(cwp2);
    	            	set.get(cwp2).add(cwp1);
    	            }
				}
			}
		}

		
		ArrayList<ClusterWaypoint> connectedComponent = new ArrayList<ClusterWaypoint>();
		Set<Waypoint> pieChartWaypointSet = new HashSet<Waypoint>();
		PieChartWaypoint pieChart, tempWp;
		Iterator<ClusterWaypoint> iter;
		WaypointPainter<JXMapViewer> pieChartClusterPainter = getPieChartPainter(); 
		
		if(pieChartClusterPainter != null) //it's been initialized
		{
			Set<Waypoint> oldPieCharts = pieChartClusterPainter.getWaypoints();
			
			for(Waypoint waypoint : waypoints)
			{
				cwptmp = (ClusterWaypoint) waypoint; 
				
				connectedComponent.clear(); // reuse the same structure
				
				//compute the connected component for each unvisited node
				if(! visited.get(cwptmp))
					DFS(cwptmp, set, visited, connectedComponent);
				
				//the component does  not consist of a single cluster
				if(connectedComponent.size() > 1)
				{
	    			pieChart = null;
					for(Waypoint wp:oldPieCharts)
	    			{
	    				tempWp = (PieChartWaypoint) wp;
	    				if(tempWp.containsSameClustersAs(connectedComponent))
	    				{
	    					pieChart = tempWp; // we can reuse the existing piechart
	    					break;
	    				}
	    			}
					
					if(pieChart == null) //that piechart doesn't exist yet
						pieChart = new PieChartWaypoint(connectedComponent);
	    			pieChartWaypointSet.add(pieChart);
	    			
	    			iter = connectedComponent.iterator();
	    			
	    			while(iter.hasNext()) // don't display the waypoints for the clusters in the pie
	    			 iter.next().show = false;
				}
			}
			//add the piechart list to the painter
			pieChartClusterPainter.setWaypoints(pieChartWaypointSet);
		}
	}
	
	//recursive depth-first search
	private void DFS(ClusterWaypoint node, HashMap<ClusterWaypoint, HashSet<ClusterWaypoint>> graph,
			HashMap<Waypoint, Boolean> visited, ArrayList<ClusterWaypoint> component)
	{
		if(visited.get(node))
    		return;
		
		component.add(node); // add the node to the current connected component
		visited.put(node, true);
		
		for(ClusterWaypoint neighbour : graph.get(node))
			if(!visited.get(neighbour))
				DFS(neighbour, graph, visited, component);		
	}
	
	@SuppressWarnings("unchecked")
	public WaypointPainter<JXMapViewer> getClusterPainter()
    {
    	CompoundPainter<JXMapViewer> cpainter = (CompoundPainter<JXMapViewer>) getMainMap().getOverlayPainter();
    	Painter<JXMapViewer>[] painters= cpainter.getPainters();
    	
    	if(painters.length > 0 && (painters[0] instanceof WaypointPainter<?>))
    	{
    		return (WaypointPainter<JXMapViewer>) painters[0];
    	}
    	
    	return null;
    }
    
    @SuppressWarnings("unchecked")
	public WaypointPainter<JXMapViewer> getPieChartPainter()
    {
    	CompoundPainter<JXMapViewer> cpainter = (CompoundPainter<JXMapViewer>) getMainMap().getOverlayPainter();
    	Painter<JXMapViewer>[] painters= cpainter.getPainters();
    	
    	if(painters.length > 1 && (painters[1] instanceof WaypointPainter<?>))
    	{
    		return (WaypointPainter<JXMapViewer>) painters[1];
    	}
    	
    	return null;
    }
    
    @SuppressWarnings("unchecked")
	public ToolTipPainter<JXMapViewer> getTooltipPainter()
    {
    	CompoundPainter<JXMapViewer> cpainter = (CompoundPainter<JXMapViewer>) getMainMap().getOverlayPainter();
    	Painter<JXMapViewer>[] painters= cpainter.getPainters();
    	
    	if(painters.length > 2 && (painters[2] instanceof ToolTipPainter<?>))
    	{
    		return (ToolTipPainter<JXMapViewer>) painters[2];
    	}
    	
    	return null;
    }
}


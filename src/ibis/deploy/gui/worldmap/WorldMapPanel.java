package ibis.deploy.gui.worldmap;

import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.experiment.composer.ClusterSelectionPanel;
import ibis.deploy.gui.misc.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class WorldMapPanel extends JPanel {
	/**
	 * 
	 */
    private static final long serialVersionUID = -846163477030295465L;

    // private static final int INITIAL_MAP_ZOOM = 15;

    private ClusterSelectionPanel clusterSelectionPanel;

    private Cluster selectedCluster;

    private ClusterWaypoint selectedWaypoint = null;

    private Set<Waypoint> waypoints = new HashSet<Waypoint>();

    // if true, only yes/no selection is supported for each cluster, instead of
    // the "count" normally available
    private final boolean booleanSelect;

    public WorldMapPanel(final GUI gui, final int zoom, boolean booleanSelect) {
        this.booleanSelect = booleanSelect;
        JMenuBar menuBar = gui.getMenuBar();
        JMenu menu = null;
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            if (menuBar.getMenu(i).getText().equals("View")) {
                menu = menuBar.getMenu(i);
            }
        }
        if (menu == null) {
            menu = new JMenu("View");
            menu.add(MapUtilities.getMapMenu());
            menuBar.add(menu, Math.max(0, menuBar.getMenuCount() - 1));
        } else {
            boolean found = false;
            for (int i = 0; i < menu.getComponentCount(); i++) {
                if (menu.getComponent(i) == MapUtilities.getMapMenu()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                menu.add(MapUtilities.getMapMenu());
            }
        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        final JXMapKit worldMap = new WorldMap();
        MapUtilities.register(worldMap);
        add(worldMap);
        worldMap.setAlignmentX(Component.LEFT_ALIGNMENT);
        worldMap.setTileFactory(MapUtilities.getDefaultTileFactory());
        worldMap.setMiniMapVisible(false);
        worldMap.setAddressLocationShown(false);
        worldMap.getMainMap().setZoom(zoom);
        worldMap.getMainMap().setCenterPosition(MapUtilities.INITIAL_MAP_CENTER);
        worldMap.getMainMap().setHorizontalWrapped(false);

        for (Cluster cluster : gui.getGrid().getClusters()) {
            waypoints.add(new ClusterWaypoint(cluster, false));

        }

        gui.addGridWorkSpaceListener(new WorkSpaceChangedListener() {
            public void workSpaceChanged(GUI gui) {
                waypoints.clear();
                for (Cluster cluster : gui.getGrid().getClusters()) {
                    waypoints.add(new ClusterWaypoint(cluster, false));
                }
                //worldMap.setZoom(zoom);
                ((WorldMap) worldMap).setZoomRelativeToClusters();
                worldMap.getMainMap().repaint();
            }
        });
        WaypointPainter<JXMapViewer> painter = new WaypointPainter<JXMapViewer>();
        painter.setRenderer(new ClusterWaypointRenderer());
        painter.setWaypoints(waypoints);
        worldMap.getMainMap().setOverlayPainter(painter);
        worldMap.getMainMap().repaint();

        worldMap.getMainMap().addMouseListener(new MouseListener() {
            @SuppressWarnings("unchecked")
            public void mouseClicked(MouseEvent e) {
                if (worldMap.getMainMap().getOverlayPainter() instanceof WaypointPainter) {
                    Set<Waypoint> waypoints = ((WaypointPainter) worldMap
                            .getMainMap().getOverlayPainter()).getWaypoints();
                    double closestDistance = Double.MAX_VALUE;
                    ClusterWaypoint tmpWaypoint = null;
                    for (Waypoint wp : waypoints) {
                        ClusterWaypoint cwp = (ClusterWaypoint) wp;
                        Point2D clusterPoint = worldMap.getMainMap()
                                .convertGeoPositionToPoint(cwp.getPosition());
                        clusterPoint.setLocation(clusterPoint.getX()
                                + cwp.getOffset().width, clusterPoint.getY()
                                + cwp.getOffset().height);
                        if (e.getPoint().distance(clusterPoint)
                                - cwp.getRadius() < closestDistance) {
                            closestDistance = e.getPoint().distance(
                                    clusterPoint)
                                    - cwp.getRadius();
                            tmpWaypoint = cwp;
                        }
                        cwp.setSelected(false);
                    }
                    if (selectedWaypoint == tmpWaypoint) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            selectedWaypoint.increaseResourceCount();
                        } else {
                            selectedWaypoint.decreaseResourceCount();
                        }
                        if (clusterSelectionPanel != null) {
                            clusterSelectionPanel
                                    .setResourceCount(selectedWaypoint
                                            .getResourceCount());
                        }
                    } else {
                        selectedWaypoint = tmpWaypoint; 
                        selectedCluster = selectedWaypoint.getCluster();
                        if (clusterSelectionPanel != null) {
                            clusterSelectionPanel.setSelected(selectedCluster);
                            clusterSelectionPanel
                                    .setResourceCount(selectedWaypoint
                                            .getResourceCount());
                        }
                    }
                    selectedWaypoint.setSelected(true);
                    worldMap.repaint();
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

        });
    }

    public void setResourceCount(int resourceCount) {
        if (selectedWaypoint != null) {
            selectedWaypoint.setResourceCount(resourceCount);
        }
        repaint();
    }

    public void setSelected(Cluster selectedCluster) {
        this.selectedCluster = selectedCluster;
        for (Waypoint waypoint : waypoints) {
            if (((ClusterWaypoint) waypoint).getCluster() == selectedCluster) {
                this.selectedWaypoint = (ClusterWaypoint) waypoint;
                ((ClusterWaypoint) waypoint).setSelected(true);
            } else {
                ((ClusterWaypoint) waypoint).setSelected(false);
            }
        }
        repaint();
    }

    public void registerClusterSelectionPanel(
            ClusterSelectionPanel clusterSelectionPanel) {
        this.clusterSelectionPanel = clusterSelectionPanel;
    }

    public Cluster getSelectedCluster() {
        if (selectedWaypoint == null) {
            return null;
        }
        return selectedWaypoint.getCluster();
    }

    // HELPER CLASSES

    private final class WorldMap extends JXMapKit {
    	/**
         * 
         */
        private static final long serialVersionUID = -6194956781979564591L;
        private boolean initialized = false;
        private static final int MAX_DEPTH = 5;
        private static final double MIN_DISTANCE = 3.5;

        private static final int WIDTH = 256;
        private static final int HEIGHT = 256;

        WorldMap() {

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
           
        }

        private void doFit() {
            for (Waypoint waypoint : waypoints) {
                ((ClusterWaypoint) waypoint).resetOffset();
            }
            for (Waypoint currentWaypoint : waypoints) {
                for (Waypoint otherWaypoint : waypoints) {
                    if (currentWaypoint != otherWaypoint) {
                        adjustPosition((ClusterWaypoint) currentWaypoint,
                               (ClusterWaypoint) otherWaypoint, 0);
                    }
                }
            } 
        }

        @Override
        public void setZoom(int zoom) {
            doFit();
            super.setZoom(zoom);
            repaint();
        }
        
        /**
         * Based on the current zoom level, it sets the maximum size for the map
         * @param zoom
         */
        public void adjustMapSize(int zoom)
        {
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
         * Decreases the zoom level until minimun zoom for all clusters to be visible is reached.
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
            Rectangle2D bounds = generateBoundingRect(positions, zoom);
            
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
                
                //after recenter, zoom level is too low, vieport no longer contains all points
                if(!getMainMap().getViewportBounds().contains(bounds)) 
                    break;
                
                zoom = zoom - 1;
                if(zoom < 1) //we've reached the lowest zoom level
                    break;
                
                setZoom(zoom);
                bounds = generateBoundingRect(positions, zoom);
            }
            //increase zoom with one more level, to make sure everyting fits in the viewport
            setZoom(zoom+1);
        }
        
        
        /**
         * Generate bounding rectangle for a set of points
         */
        private Rectangle2D generateBoundingRect(final Set<GeoPosition> positions, int zoom) 
        {
            Point2D initialPoint = getMainMap().getTileFactory().geoToPixel(positions.iterator().next(), zoom);
            Rectangle2D rect = new Rectangle2D.Double(initialPoint.getX(), initialPoint.getY(),0,0);
            
            for(GeoPosition position : positions) 
            {
                Point2D point = getMainMap().getTileFactory().geoToPixel(position, zoom);
                rect.add(point);
            }
            return rect;
        }

        public void paint(Graphics g) {
            if (!initialized) {
                doFit();
                setZoomRelativeToClusters(); 
                initialized = true;
            }
            adjustMapSize(getMainMap().getZoom());
            super.paint(g);
        }

        private void adjustPosition(ClusterWaypoint waypoint,
                ClusterWaypoint otherWaypoint, int depth) {
            // take already known offset into account
            Point2D p1 = getMainMap().convertGeoPositionToPoint(
                    waypoint.getPosition());
            p1.setLocation(p1.getX() + waypoint.getOffset().width, p1.getY()
                    + waypoint.getOffset().height);
            Point2D p2 = getMainMap().convertGeoPositionToPoint(
                    otherWaypoint.getPosition());
            p2.setLocation(p2.getX() + otherWaypoint.getOffset().width, p2
                    .getY()
                    + otherWaypoint.getOffset().height);

            double distance = p1.distance(p2);
            double minDistance = waypoint.getRadius()
                    + otherWaypoint.getRadius() + 20;
            if (distance < minDistance) {
                // move both waypoints in half the overlap size in the
                // proper direction
                double overlap = 0.5 * (minDistance - distance) + MIN_DISTANCE;
                double deltaX = p1.getX() - p2.getX();
                double deltaY = p1.getY() - p2.getY();
                waypoint.addOffset((int) ((overlap / distance) * deltaX),
                        (int) ((overlap / distance) * deltaY));
                otherWaypoint.addOffset((int) ((overlap / distance) * -deltaX),
                        (int) ((overlap / distance) * -deltaY));
                for (Waypoint thirdWaypoint : waypoints) {
                    if (thirdWaypoint != waypoint
                            && thirdWaypoint != otherWaypoint
                            && depth < MAX_DEPTH) {
                        adjustPosition(waypoint,
                                (ClusterWaypoint) thirdWaypoint, depth + 1);
                        adjustPosition(otherWaypoint,
                                (ClusterWaypoint) thirdWaypoint, depth + 1);
                    }
                }
            }
        }
    }

    private class ClusterWaypoint extends Waypoint {

        private Cluster cluster;

        private int resourceCount;

        private boolean selected;

        private Dimension offset = new Dimension(0, 0);

        public ClusterWaypoint(Cluster cluster, boolean selected) {
            super(
                    cluster.getLatitude() == 0 && cluster.getLongitude() == 0 ? 52.332933
                            : cluster.getLatitude(), cluster.getLatitude() == 0
                            && cluster.getLongitude() == 0 ? 4.866064 : cluster
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

    }

    private class ClusterWaypointRenderer implements WaypointRenderer {
        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            ClusterWaypoint cwp = (ClusterWaypoint) wp;
            final int x = cwp.getOffset().width;
            final int y = cwp.getOffset().height;

            Color clusterBorderColor = Utils.getColor(cwp.getCluster()
                    .getColorCode());
            if (clusterBorderColor == null) {
                clusterBorderColor = new Color(100, 100, 255, 255);
            }
            Color clusterFillColor = Utils.getLightColor(cwp.getCluster()
                    .getColorCode());
            if (clusterFillColor == null) {
                clusterFillColor = new Color(100, 100, 255, 150);
            }

            // Color clusterBorderColor = new Color(100, 100, 255, 255);
            // Color clusterFillColor = new Color(100, 100, 255, 150);
            Color clusterTextColor = new Color(255, 255, 255, 255);

            Color selectedBorderColor = new Color(255, 100, 100, 255);
            // Color selectedArcColor = new Color(255, 100, 100, 200);
            Color selectedFillColor = new Color(255, 100, 100, 200);
            Color selectedTextColor = new Color(255, 100, 100, 100);

            // draw a line from where the cluster is drawn to where it actually
            // is
            // g.setPaint(Color.BLACK);
            // g.drawLine(0, 0, x, y);

            // String numberNodesString = ""
            // + ((cwp.getCluster().getNodes() > 0) ? cwp.getCluster()
            // .getNodes() : "n.a.");

            // draw circle
            final int radius = cwp.getRadius();
            final int diameter = 2 * radius;

            if (cwp.isSelected() && booleanSelect) {
                g.setPaint(selectedFillColor);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                g.setPaint(selectedBorderColor);
                g.drawOval(x - radius, y - radius, diameter, diameter);
            } else if (cwp.isSelected()) {

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

            // draw cluster name
            String clusterName = cwp.getCluster().getName();
            Font original = g.getFont();
            g.setFont(original.deriveFont(Font.BOLD));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int width = (int) g.getFontMetrics()
                    .getStringBounds(clusterName, g).getWidth();
            int height = (int) g.getFontMetrics().getStringBounds(clusterName,
                    g).getHeight();

            g.setPaint(Color.BLACK);
            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
                    - 1 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 + 1, y + height / 2 + 8
                    - 1 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 + 1, y + height / 2 + 8
                    + 1 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
                    + 1 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
                    - 0 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 + 1, y + height / 2 + 8
                    - 0 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 + 0, y + height / 2 + 8
                    + 1 + radius); // shadow
            g.drawString(clusterName, x + -width / 2 - 0, y + height / 2 + 8
                    - 1 + radius); // shadow

            g.setPaint(Color.WHITE);

            g.drawString(clusterName, x + -width / 2, y + height / 2 + 8
                    + radius); // text

            if (cwp.isSelected()) {
                g.setPaint(selectedTextColor);
            } else {
                g.setPaint(clusterTextColor);

            }
            g.drawString(clusterName, x + -width / 2, y + height / 2 + 8
                    + radius); // text

            // draw a line to the original position of the cluster
            // Point2D point = map.convertGeoPositionToPoint(cwp.getPosition());
            // g.setPaint(Color.BLACK);
            // g.drawLine(0, 0, x, y);

            // draw usage
            
            if (cwp.isSelected() && !booleanSelect) {

                String usageString = cwp.getResourceCount()
                        + "/"
                        + ((cwp.getCluster().getNodes() > 0) ? cwp.getCluster()
                                .getNodes() : "n.a.");
                width = (int) g.getFontMetrics()
                        .getStringBounds(usageString, g).getWidth();
                height = (int) g.getFontMetrics().getStringBounds(usageString,
                        g).getHeight();

                // draw text w/ shadow
                g.setPaint(Color.BLACK);
                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
                        - 3 - 1); // shadow
                g.drawString(usageString, x + -width / 2 + 1, y + height / 2
                        - 3 - 1); // shadow
                g.drawString(usageString, x + -width / 2 + 1, y + height / 2
                        - 3 + 1); // shadow
                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
                        - 3 + 1); // shadow
                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
                        - 3 - 0); // shadow
                g.drawString(usageString, x + -width / 2 + 1, y + height / 2
                        - 3 - 0); // shadow
                g.drawString(usageString, x + -width / 2 + 0, y + height / 2
                        - 3 - 1); // shadow
                g.drawString(usageString, x + -width / 2 + 0, y + height / 2
                        - 3 + 1); // shadow
                g.setPaint(Color.WHITE);
                g.drawString(usageString, x + -width / 2, y + height / 2 - 3); // text
                g.setPaint(clusterTextColor);
                g.drawString(usageString, x + -width / 2, y + height / 2 - 3); // text

            }

            g.setFont(original);
            return false;
        }

    }

    public int getResourceCount() {
        if (selectedWaypoint != null) {
            return selectedWaypoint.getResourceCount();
        } else {
            return 1;
        }
    }

}

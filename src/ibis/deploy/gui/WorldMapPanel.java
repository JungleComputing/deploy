package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

import deployer.gui.GoogleMapStreetTileProvider;
import deployer.gui.GoogleMapTerrainTileProvider;
import deployer.gui.MicrosoftMapTileProvider;
import deployer.gui.OpenStreetMapTileProvider;

public class WorldMapPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -846163477030295465L;

    private static final GeoPosition INITIAL_MAP_CENTER = new GeoPosition(
            52.332042, 4.866736);

    private static final int INITIAL_MAP_ZOOM = 15;

    private ClusterSelectionPanel clusterSelectionPanel;

    private Cluster selectedCluster;

    private ClusterWaypoint selectedWaypoint = null;

    private Set<Waypoint> waypoints = new HashSet<Waypoint>();

    private static List<JXMapKit> mapKits = new ArrayList<JXMapKit>();

    private static TileFactory defaultTileFactory = GoogleMapTerrainTileProvider
            .getDefaultTileFactory();

    private static JMenu mapMenu = new JMenu("Map Tile Provider");

    static {

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem mapMenuItem = new JRadioButtonMenuItem(
                "Google Terrain", true);
        mapMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                defaultTileFactory = GoogleMapTerrainTileProvider
                        .getDefaultTileFactory();
                updateMapTileFactory();
            }
        });
        group.add(mapMenuItem);
        mapMenu.add(mapMenuItem);
        mapMenuItem = new JRadioButtonMenuItem("Google Street");
        mapMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                defaultTileFactory = GoogleMapStreetTileProvider
                        .getDefaultTileFactory();
                updateMapTileFactory();
            }
        });
        group.add(mapMenuItem);
        mapMenu.add(mapMenuItem);
        mapMenuItem = new JRadioButtonMenuItem("Microsoft");
        mapMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                defaultTileFactory = MicrosoftMapTileProvider
                        .getDefaultTileFactory();
                updateMapTileFactory();
            }
        });
        group.add(mapMenuItem);
        mapMenu.add(mapMenuItem);
        mapMenuItem = new JRadioButtonMenuItem("Open Street Maps");
        mapMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                defaultTileFactory = OpenStreetMapTileProvider
                        .getDefaultTileFactory();
                updateMapTileFactory();
            }
        });
        group.add(mapMenuItem);
        mapMenu.add(mapMenuItem);
    }

    public WorldMapPanel(final GUI gui) {
        JMenuBar menuBar = gui.getMenuBar();
        JMenu menu = null;
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            if (menuBar.getMenu(i).getText().equals("View")) {
                menu = menuBar.getMenu(i);
            }
        }
        if (menu == null) {
            menu = new JMenu("View");
            menu.add(mapMenu);
            menuBar.add(menu);
        } else {
            boolean found = false;
            for (int i = 0; i < menu.getComponentCount(); i++) {
                if (menu.getComponent(i) == mapMenu) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                menu.add(mapMenu);
            }
        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        final JXMapKit mapKit = new JXMapKit() {

            private boolean initialized = false;

            private static final int MAX_DEPTH = 3;

            private static final int MIN_DISTANCE = 4;

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
            }

            public void paint(Graphics g) {
                if (!initialized) {
                    doFit();
                    initialized = true;
                }
                super.paint(g);
            }

            private void adjustPosition(ClusterWaypoint waypoint,
                    ClusterWaypoint otherWaypoint, int depth) {
                // take already known offset into account
                Point2D p1 = getMainMap().convertGeoPositionToPoint(
                        waypoint.getPosition());
                p1.setLocation(p1.getX() + waypoint.getOffset().width, p1
                        .getY()
                        + waypoint.getOffset().height);
                Point2D p2 = getMainMap().convertGeoPositionToPoint(
                        otherWaypoint.getPosition());
                p2.setLocation(p2.getX() + otherWaypoint.getOffset().width, p2
                        .getY()
                        + otherWaypoint.getOffset().height);

                double distance = p1.distance(p2);
                double minDistance = waypoint.getRadius()
                        + otherWaypoint.getRadius();
                if (distance < minDistance) {
                    // move both waypoints in half the overlap size in the
                    // proper direction
                    double overlap = 0.5 * (minDistance - distance)
                            + MIN_DISTANCE;
                    double deltaX = p1.getX() - p2.getX();
                    double deltaY = p1.getY() - p2.getY();
                    waypoint.addOffset((int) ((overlap / distance) * deltaX),
                            (int) ((overlap / distance) * deltaY));
                    otherWaypoint.addOffset(
                            (int) ((overlap / distance) * -deltaX),
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
        };
        mapKits.add(mapKit);
        add(mapKit);
        mapKit.setTileFactory(defaultTileFactory);
        mapKit.setMiniMapVisible(false);
        mapKit.setAddressLocationShown(false);
        mapKit.getMainMap().setZoom(INITIAL_MAP_ZOOM);
        mapKit.getMainMap().setCenterPosition(INITIAL_MAP_CENTER);

        for (Cluster cluster : gui.getGrid().getClusters()) {
            waypoints.add(new ClusterWaypoint(cluster, false));

        }
        WaypointPainter<JXMapViewer> painter = new WaypointPainter<JXMapViewer>();
        painter.setRenderer(new ClusterWaypointRenderer());
        painter.setWaypoints(waypoints);
        mapKit.getMainMap().setOverlayPainter(painter);
        mapKit.getMainMap().repaint();

        mapKit.getMainMap().addMouseListener(new MouseListener() {
            @SuppressWarnings("unchecked")
            public void mouseClicked(MouseEvent e) {
                if (mapKit.getMainMap().getOverlayPainter() instanceof WaypointPainter) {
                    Set<Waypoint> waypoints = ((WaypointPainter) mapKit
                            .getMainMap().getOverlayPainter()).getWaypoints();
                    double closestDistance = Double.MAX_VALUE;
                    ClusterWaypoint tmpWaypoint = null;
                    for (Waypoint wp : waypoints) {
                        ClusterWaypoint cwp = (ClusterWaypoint) wp;
                        Point2D clusterPoint = mapKit.getMainMap()
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
                    mapKit.repaint();
                }
            }

            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

        });

    }

    protected static void updateMapTileFactory() {
        for (JXMapKit mapKit : mapKits) {

            GeoPosition position = mapKit.getMainMap().getCenterPosition();
            mapKit.setTileFactory(defaultTileFactory);
            mapKit.getMainMap().setCenterPosition(position);
        }

    }

    protected void setResourceCount(int resourceCount) {
        if (selectedWaypoint != null) {
            selectedWaypoint.setResourceCount(resourceCount);
        }
        repaint();
    }

    protected void setSelected(Cluster selectedCluster) {
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

    protected void registerClusterSelectionPanel(
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

    private class ClusterWaypoint extends Waypoint {

        private Cluster cluster;

        private int resourceCount;

        private boolean selected;

        private Dimension offset = new Dimension(0, 0);

        public ClusterWaypoint(Cluster cluster, boolean selected) {
            super(cluster.getLongitude(), cluster.getLatitude());
            this.selected = selected;
            this.cluster = cluster;
            this.resourceCount = 1;
        }

        public int getRadius() {
            return Math.max(20, 15 + cluster.getNodes()) / 2;
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

            String numberNodesString = "" + cwp.getCluster().getNodes();

            // draw circle
            final int radius = cwp.getRadius();
            final int diameter = 2 * radius;

            if (cwp.isSelected()) {
                g.setPaint(new Color(255, 100, 100, 200));
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, -(cwp
                        .getResourceCount() * 360)
                        / cwp.getCluster().getNodes());
                g.setPaint(new Color(255, 100, 100, 80));
                g.fillArc(x - radius, y - radius, diameter, diameter, 90, 360
                        - (cwp.getResourceCount() * 360)
                        / cwp.getCluster().getNodes());
            } else {
                g.setPaint(new Color(100, 100, 255, 80));
                g.fillOval(x - radius, y - radius, diameter, diameter);
            }
            if (cwp.isSelected()) {
                g.setPaint(new Color(255, 100, 100, 255));
            } else {
                g.setPaint(new Color(100, 100, 255, 255));
            }
            g.drawOval(x - radius, y - radius, diameter, diameter);

            // draw cluster name
            String clusterName = cwp.getCluster().getName();
            int width = (int) g.getFontMetrics()
                    .getStringBounds(clusterName, g).getWidth();
            int height = (int) g.getFontMetrics().getStringBounds(clusterName,
                    g).getHeight();

            if (cwp.isSelected()) {
                g.setPaint(new Color(255, 100, 100, 100));
            } else {
                g.setPaint(new Color(100, 100, 255, 100));
            }
            g.fillRoundRect(x + -width / 2 - 5, y + -height / 2 + 8 + radius,
                    width + 10, height + 6, 10, 10);
            if (cwp.isSelected()) {
                g.setPaint(new Color(255, 100, 100, 255));
            } else {
                g.setPaint(new Color(100, 100, 255, 255));
            }
            g.drawRoundRect(x + -width / 2 - 5, y + -height / 2 + 8 + radius,
                    width + 10, height + 6, 10, 10);

            // draw text w/ shadow
            g.setPaint(Color.BLACK);
            g.drawString(clusterName, x + -width / 2 - 1, y + height / 2 + 8
                    - 1 + radius); // shadow
            g.setPaint(Color.WHITE);
            g.drawString(clusterName, x + -width / 2, y + height / 2 + 8
                    + radius); // text

            // draw usage
            if (cwp.isSelected()) {
                String usageString = cwp.getResourceCount() + "/"
                        + cwp.getCluster().getNodes();
                width = (int) g.getFontMetrics()
                        .getStringBounds(usageString, g).getWidth();
                height = (int) g.getFontMetrics().getStringBounds(usageString,
                        g).getHeight();

                // draw text w/ shadow
                g.setPaint(Color.BLACK);
                g.drawString(usageString, x + -width / 2 - 1, y + height / 2
                        - 3 - 1); // shadow
                g.setPaint(Color.WHITE);
                g.drawString(usageString, x + -width / 2, y + height / 2 - 3); // text
            }
            return false;
        }

    }

    public int getResourceCount() {
        return selectedWaypoint.getResourceCount();
    }

}

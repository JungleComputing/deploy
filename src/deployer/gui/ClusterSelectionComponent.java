package deployer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

import deployer.Cluster;
import deployer.Deployer;
import deployer.Grid;

public class ClusterSelectionComponent implements SelectionComponent {

    private static final GeoPosition INITIAL_MAP_CENTER = new GeoPosition(
            52.332042, 4.866736);

    private static final int INITIAL_MAP_ZOOM = 15;

    private JPanel panel;

    private JComboBox gridComboBox;

    private JComboBox clusterComboBox;

    private JSpinner resourceCountSpinner;

    private Deployer deployer;

    private MapActionListener mapActionListener;

    public ClusterSelectionComponent(Deployer deployer) {
        this.deployer = deployer;

        // construct all the individual components
        gridComboBox = new JComboBox(deployer.getGrids().toArray(
                new Grid[deployer.getGrids().size()]));
        gridComboBox.setPreferredSize(new Dimension(DEFAULT_COMPONENT_WIDTH,
                (int) gridComboBox.getPreferredSize().getHeight()));

        clusterComboBox = new JComboBox();
        clusterComboBox.setPreferredSize(new Dimension(DEFAULT_COMPONENT_WIDTH,
                (int) clusterComboBox.getPreferredSize().getHeight()));

        resourceCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1,
                Integer.MAX_VALUE, 1));
        resourceCountSpinner.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) resourceCountSpinner
                        .getPreferredSize().getHeight()));

        JXMapKit mapKit = new JXMapKit();
        mapKit.setTileFactory(MicrosoftMapTileProvider.getDefaultTileFactory());
        mapKit.setMiniMapVisible(false);
        mapKit.setAddressLocationShown(false);
        mapKit.getMainMap().setZoom(INITIAL_MAP_ZOOM);
        mapKit.getMainMap().setCenterPosition(INITIAL_MAP_CENTER);

        JPanel mapSelectionPanel = new JPanel();
        JRadioButton googleMapsTerrain = new JRadioButton("Google (Terrain)");
        JRadioButton googleMapsStreet = new JRadioButton("Google (Street)");
        JRadioButton microsoftMaps = new JRadioButton("Microsoft (Satellite)",
                true);
        JRadioButton openStreetMaps = new JRadioButton(
                "OpenStreetMaps (Street)");

        googleMapsTerrain.addActionListener(new MapSelectionActionListener(
                mapKit, GoogleMapTerrainTileProvider.getDefaultTileFactory()));
        googleMapsStreet.addActionListener(new MapSelectionActionListener(
                mapKit, GoogleMapStreetTileProvider.getDefaultTileFactory()));
        microsoftMaps.addActionListener(new MapSelectionActionListener(mapKit,
                MicrosoftMapTileProvider.getDefaultTileFactory()));
        openStreetMaps.addActionListener(new MapSelectionActionListener(mapKit,
                OpenStreetMapTileProvider.getDefaultTileFactory()));

        ButtonGroup mapSelectionGroup = new ButtonGroup();
        mapSelectionGroup.add(googleMapsTerrain);
        mapSelectionGroup.add(googleMapsStreet);
        mapSelectionGroup.add(microsoftMaps);
        mapSelectionGroup.add(openStreetMaps);

        mapSelectionPanel.add(googleMapsStreet);
        mapSelectionPanel.add(googleMapsTerrain);
        mapSelectionPanel.add(microsoftMaps);
        mapSelectionPanel.add(openStreetMaps);

        // add the textual selection items to the textSelectionPanel
        JPanel textSelectionPanel = new JPanel(new GridLayout(3, 2, 3, 3));

        textSelectionPanel.add(new JLabel("grid: ", JLabel.RIGHT));
        textSelectionPanel.add(gridComboBox);
        textSelectionPanel.add(new JLabel("cluster: ", JLabel.RIGHT));
        textSelectionPanel.add(clusterComboBox);
        textSelectionPanel.add(new JLabel("resource count: ", JLabel.RIGHT));
        textSelectionPanel.add(resourceCountSpinner);

        // add the textual selection items to a new flow layout panel
        JPanel textSelectionContainer = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, 0));
        textSelectionContainer.add(textSelectionPanel);

        // now add everything to the main panel
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("select cluster"));

        // first the textSelectionPanel
        panel.add(textSelectionContainer);
        // a bit of rigid area
        panel.add(Box.createRigidArea(new Dimension(0, 3)));

        // then the map
        panel.add(mapKit);
        // then the map selection
        panel.add(mapSelectionPanel);

        // finally add the listeners to the components
        mapActionListener = new MapActionListener(gridComboBox,
                clusterComboBox, mapKit);
        gridComboBox.addActionListener(mapActionListener);
        clusterComboBox.addActionListener(mapActionListener);
        gridComboBox.addActionListener(new GridComboBoxActionListener(
                gridComboBox, clusterComboBox));
        mapKit.getMainMap().addMouseListener(
                new MapMouseListener(mapKit, clusterComboBox));

    }

    public void update() {
        for (int i = 0; i < gridComboBox.getItemCount(); i++) {
            boolean exists = false;
            for (Grid grid : deployer.getGrids()) {
                if (grid == gridComboBox.getItemAt(i)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                gridComboBox.removeItemAt(i);
                i--;
            }
        }
        for (Grid grid : deployer.getGrids()) {
            boolean exists = false;
            for (int i = 0; i < gridComboBox.getItemCount(); i++) {
                if (grid == gridComboBox.getItemAt(i)) {
                    exists = true;
                }
            }
            if (!exists) {
                gridComboBox.addItem(grid);
            }
        }
        for (int i = 0; i < clusterComboBox.getItemCount(); i++) {
            boolean exists = false;
            if (((Grid) gridComboBox.getSelectedItem()) != null) {
                for (Cluster cluster : ((Grid) gridComboBox.getSelectedItem())
                        .getClusters()) {
                    if (cluster == clusterComboBox.getItemAt(i)) {
                        exists = true;
                        break;
                    }
                }
            }
            if (!exists) {
                clusterComboBox.removeItemAt(i);
                i--;
                mapActionListener.actionPerformed(new ActionEvent(
                        clusterComboBox, 0, null));
            }
        }
        if (((Grid) gridComboBox.getSelectedItem()) != null) {
            for (Cluster cluster : ((Grid) gridComboBox.getSelectedItem())
                    .getClusters()) {
                boolean exists = false;
                for (int i = 0; i < clusterComboBox.getItemCount(); i++) {
                    if (cluster == clusterComboBox.getItemAt(i)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    clusterComboBox.addItem(cluster);
                    mapActionListener.actionPerformed(new ActionEvent(
                            gridComboBox, 0, null));
                }
            }
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public Object[] getValues() {
        // Cluster, resource count
        return new Object[] {
                clusterComboBox.getSelectedItem(),
                ((SpinnerNumberModel) resourceCountSpinner.getModel())
                        .getNumber().intValue() };
    }

    private class GridComboBoxActionListener implements ActionListener {

        private JComboBox clusterComboBox;

        private JComboBox gridComboBox;

        GridComboBoxActionListener(JComboBox gridComboBox,
                JComboBox clusterComboBox) {
            this.gridComboBox = gridComboBox;
            this.clusterComboBox = clusterComboBox;
            if (gridComboBox.getSelectedItem() != null) {
                for (Cluster cluster : ((Grid) gridComboBox.getSelectedItem())
                        .getClusters()) {
                    clusterComboBox.addItem(cluster);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (gridComboBox.getSelectedItem() == null) {
                return;
            }
            clusterComboBox.removeAllItems();
            for (Cluster cluster : ((Grid) gridComboBox.getSelectedItem())
                    .getClusters()) {
                clusterComboBox.addItem(cluster);
            }
        }
    }

    private class MapMouseListener implements MouseListener {

        private JXMapKit mapKit;

        private JComboBox clusterComboBox;

        public MapMouseListener(JXMapKit mapKit, JComboBox clusterComboBox) {
            this.mapKit = mapKit;
            this.clusterComboBox = clusterComboBox;
        }

        @SuppressWarnings("unchecked")
        public void mouseClicked(MouseEvent e) {
            if (mapKit.getMainMap().getOverlayPainter() instanceof WaypointPainter) {
                Set<Waypoint> waypoints = ((WaypointPainter) mapKit
                        .getMainMap().getOverlayPainter()).getWaypoints();
                Cluster closestCluster = null;
                double closestDistance = Double.MAX_VALUE;

                for (Waypoint wp : waypoints) {
                    if (e.getPoint().distance(
                            mapKit.getMainMap().convertGeoPositionToPoint(
                                    wp.getPosition())) < closestDistance) {
                        closestDistance = e.getPoint().distance(
                                mapKit.getMainMap().convertGeoPositionToPoint(
                                        wp.getPosition()));
                        closestCluster = ((ClusterWaypoint) wp).getCluster();
                    }
                }
                if (closestCluster != null) {
                    clusterComboBox.setSelectedItem(closestCluster);
                }
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

    }

    private class ClusterWaypoint extends Waypoint {

        private Cluster cluster;

        private boolean selected;

        public ClusterWaypoint(Cluster cluster, boolean selected) {
            super(cluster.getGeoPositionX(), cluster.getGeoPositionY());
            this.selected = selected;
            this.cluster = cluster;
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

    }

    private class MapActionListener implements ActionListener {

        private class ClusterWaypointRenderer implements WaypointRenderer {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map,
                    Waypoint wp) {
                ClusterWaypoint cwp = (ClusterWaypoint) wp;

                // draw tab
                if (cwp.isSelected()) {
                    g.setPaint(new Color(255, 100, 100, 200));
                } else {
                    g.setPaint(new Color(100, 100, 255, 200));
                }
                int width = (int) g.getFontMetrics().getStringBounds(
                        cwp.getName(), g).getWidth();
                g.fillRoundRect(-width / 2 - 5, -5, width + 10, 20, 10, 10);

                // draw text w/ shadow
                g.setPaint(Color.BLACK);
                g.drawString(cwp.getName(), -width / 2 - 1, 10 - 1); // shadow
                g.setPaint(Color.WHITE);
                g.drawString(cwp.getName(), -width / 2, 10); // text
                return false;
            }
        }

        private JXMapKit mapKit;

        private JComboBox gridComboBox;

        private JComboBox clusterComboBox;

        @SuppressWarnings("unchecked")
        MapActionListener(JComboBox gridComboBox, JComboBox clusterComboBox,
                JXMapKit mapKit) {
            this.gridComboBox = gridComboBox;
            this.clusterComboBox = clusterComboBox;
            this.mapKit = mapKit;
            if (gridComboBox.getSelectedItem() != null) {
                Set<Waypoint> waypoints = new HashSet<Waypoint>();
                for (Cluster cluster : ((Grid) gridComboBox.getSelectedItem())
                        .getClusters()) {
                    waypoints.add(new ClusterWaypoint(cluster, clusterComboBox
                            .getSelectedItem() == cluster));

                }
                WaypointPainter painter = new WaypointPainter();
                painter.setRenderer(new ClusterWaypointRenderer());
                painter.setWaypoints(waypoints);
                mapKit.getMainMap().setOverlayPainter(painter);
                mapKit.getMainMap().repaint();
            }
        }

        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            if (gridComboBox.getSelectedItem() == null) {
                return;
            }
            WaypointPainter painter = new WaypointPainter();
            painter.setRenderer(new ClusterWaypointRenderer());
            Set<Waypoint> waypoints = new HashSet<Waypoint>();
            Set<GeoPosition> positions = new HashSet<GeoPosition>();
            for (Cluster cluster : ((Grid) gridComboBox.getSelectedItem())
                    .getClusters()) {
                waypoints.add(new ClusterWaypoint(cluster, clusterComboBox
                        .getSelectedItem() == cluster));
                positions.add(new GeoPosition(cluster.getGeoPositionX(),
                        cluster.getGeoPositionY()));

            }
            painter.setWaypoints(waypoints);
            mapKit.getMainMap().setOverlayPainter(painter);
            if (e.getSource() == gridComboBox) {
                mapKit.getMainMap().setZoom(1);
                mapKit.getMainMap().calculateZoomFrom(positions);
                mapKit.getMainMap().setZoom(mapKit.getMainMap().getZoom() + 1);
            }
            mapKit.getMainMap().repaint();
        }
    }

    private class MapSelectionActionListener implements ActionListener {

        private JXMapKit mapKit;

        private TileFactory tileFactory;

        public MapSelectionActionListener(JXMapKit mapKit,
                TileFactory tileFactory) {
            this.mapKit = mapKit;
            this.tileFactory = tileFactory;
        }

        public void actionPerformed(ActionEvent e) {
            GeoPosition position = mapKit.getMainMap().getCenterPosition();
            mapKit.setTileFactory(tileFactory);
            mapKit.getMainMap().setCenterPosition(position);
        }

    }

}

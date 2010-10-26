package ibis.deploy.gui.worldmap;

import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.experiment.composer.ClusterSelectionPanel;
import ibis.deploy.gui.worldmap.helpers.ClusterWaypoint;
import ibis.deploy.gui.worldmap.helpers.WorldMap;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.Waypoint;

public class WorldMapPanel extends JPanel {
    /**
	 * 
	 */
    private static final long serialVersionUID = -846163477030295465L;

    // private static final int INITIAL_MAP_ZOOM = 15;

    private ClusterSelectionPanel clusterSelectionPanel;

    //private Cluster selectedCluster;

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

        // create cluster waypoints
        for (Cluster cluster : gui.getGrid().getClusters()) {
            waypoints.add(new ClusterWaypoint(cluster, false));

        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        final WorldMap worldMap = new WorldMap(this, zoom);
        add(worldMap);

        gui.addGridWorkSpaceListener(new WorkSpaceChangedListener() {
            public void workSpaceChanged(GUI gui) {
                waypoints.clear();
                for (Cluster cluster : gui.getGrid().getClusters()) {
                    waypoints.add(new ClusterWaypoint(cluster, false));
                }

                worldMap.updateWaypoints();

                // worldMap.setZoom(zoom);
                worldMap.setZoomRelativeToClusters();

                selectedWaypoint = null;
                worldMap.getMainMap().repaint();
            }
        });

        worldMap.getMainMap().repaint();

        // add mouse listeners
        worldMap.getMainMap().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                worldMap.updateOnMouseAction(e.getPoint(), true);
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
                worldMap.getTooltipPainter().setLocation(null);
                worldMap.repaint();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

        });

        // update tooltips on mouse move
        worldMap.getMainMap().addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                worldMap.updateOnMouseAction(e.getPoint(), false);
            }

            public void mouseDragged(MouseEvent e) {
                worldMap.updateOnMouseAction(e.getPoint(), false);
            }
        });

        // update tooltips on mouse wheel movement
        worldMap.getMainMap().addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                worldMap.updateOnMouseAction(e.getPoint(), false);
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
        //this.selectedCluster = selectedCluster;
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

    public int getResourceCount() {
        if (selectedWaypoint != null) {
            return selectedWaypoint.getResourceCount();
        } else {
            return 1;
        }
    }

    public boolean getBooleanSelect() {
        return booleanSelect;
    }

    public Set<Waypoint> getWaypoints() {
        return waypoints;
    }

    public ClusterSelectionPanel getClusterSelectionPanel() {
        return clusterSelectionPanel;
    }

}
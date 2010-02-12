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
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;

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
        final WorldMap worldMap = new WorldMap(this, zoom);
        add(worldMap);

        for (Cluster cluster : gui.getGrid().getClusters()) {
            waypoints.add(new ClusterWaypoint(cluster, false));

        }

        gui.addGridWorkSpaceListener(new WorkSpaceChangedListener() 
        {
            public void workSpaceChanged(GUI gui) {
                waypoints.clear();
                for (Cluster cluster : gui.getGrid().getClusters()) {
                    waypoints.add(new ClusterWaypoint(cluster, false));
                }
                //worldMap.setZoom(zoom);
                worldMap.setZoomRelativeToClusters();
                worldMap.getMainMap().repaint();
            }
        });

        worldMap.getMainMap().repaint();
        
        //add mouse listeners
        worldMap.getMainMap().addMouseListener(new MouseListener() {
            //@SuppressWarnings("unchecked")
            public void mouseClicked(MouseEvent e) 
            {
            	
//            	WaypointPainter<JXMapViewer> wpainter = worldMap.getClusterPainter();
//
//            	if(wpainter != null)//we have such a painter
//            	{
//                    Set<Waypoint> waypoints = wpainter.getWaypoints();
//                    double closestDistance = Double.MAX_VALUE;
//                    ClusterWaypoint tmpWaypoint = null;
//                    for (Waypoint wp : waypoints) 
//                    {
//                        ClusterWaypoint cwp = (ClusterWaypoint) wp;
//                        Point2D clusterPoint = worldMap.getMainMap()
//                                .convertGeoPositionToPoint(cwp.getPosition());
//                        clusterPoint.setLocation(clusterPoint.getX()
//                                + cwp.getOffset().width, clusterPoint.getY()
//                                + cwp.getOffset().height);
//                        if (e.getPoint().distance(clusterPoint)
//                                - cwp.getRadius() < closestDistance) {
//                            closestDistance = e.getPoint().distance(
//                                    clusterPoint)
//                                    - cwp.getRadius();
//                            tmpWaypoint = cwp;
//                        }
//                        cwp.setSelected(false);
//                    }
//                    if (selectedWaypoint == tmpWaypoint) {
//                        if (e.getButton() == MouseEvent.BUTTON1) {
//                            selectedWaypoint.increaseResourceCount();
//                        } else {
//                            selectedWaypoint.decreaseResourceCount();
//                        }
//                        if (clusterSelectionPanel != null) {
//                            clusterSelectionPanel
//                                    .setResourceCount(selectedWaypoint
//                                            .getResourceCount());
//                        }
//                    } else {
//                        selectedWaypoint = tmpWaypoint; 
//                        selectedCluster = selectedWaypoint.getCluster();
//                        if (clusterSelectionPanel != null) {
//                            clusterSelectionPanel.setSelected(selectedCluster);
//                            clusterSelectionPanel
//                                    .setResourceCount(selectedWaypoint
//                                            .getResourceCount());
//                        }
//                    }
//                    selectedWaypoint.setSelected(true);
//                    worldMap.repaint();
//                }
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
        
        //update tooltips on mouse move
        worldMap.getMainMap().addMouseMotionListener(new MouseMotionListener() 
        {
        	public void mouseMoved(MouseEvent e) 
			{
        		worldMap.updateTooltipLabels(e.getPoint());
			}
			
			public void mouseDragged(MouseEvent e) {
				worldMap.updateTooltipLabels(e.getPoint());
			}
		});
        
        //update tooltips on mouse wheel movement
        worldMap.getMainMap().addMouseWheelListener(new MouseWheelListener() 
        {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) 
			{
				worldMap.updateTooltipLabels(e.getPoint());
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

    public int getResourceCount() {
        if (selectedWaypoint != null) {
            return selectedWaypoint.getResourceCount();
        } else {
            return 1;
        }
    }
    
    public boolean getBooleanSelect()
    {
    	return booleanSelect;
    }
    
    public Set<Waypoint> getWaypoints()
    {
    	return waypoints;
    }

}
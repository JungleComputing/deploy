package ibis.deploy.gui.worldmap;

import ibis.deploy.Resource;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.experiment.composer.ResourceSelectionPanel;
import ibis.deploy.gui.worldmap.helpers.ResourceWaypoint;
import ibis.deploy.gui.worldmap.helpers.WorldMap;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.Waypoint;

public class WorldMapPanel extends JPanel {
    /**
	 * 
	 */
    private static final long serialVersionUID = -846163477030295465L;

    // private static final int INITIAL_MAP_ZOOM = 15;

    private ResourceSelectionPanel resourceSelectionPanel;

    private ResourceWaypoint selectedWaypoint = null;

    private Set<Waypoint> waypoints = new HashSet<Waypoint>();

    // if true, only yes/no selection is supported for each resource, instead of
    // the "count" normally available
    private final boolean booleanSelect;

    public WorldMapPanel(final GUI gui, final int zoom, boolean booleanSelect) {
        this.booleanSelect = booleanSelect;

        // create resource waypoints
        for (Resource resource : gui.getJungle().getResources()) {
            waypoints.add(new ResourceWaypoint(resource, false));

        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        final WorldMap worldMap = new WorldMap(this, zoom);
        add(worldMap);

        gui.addJungleWorkSpaceListener(new WorkSpaceChangedListener() {
            public void workSpaceChanged(GUI gui) {
                waypoints.clear();
                for (Resource resource : gui.getJungle().getResources()) {
                    waypoints.add(new ResourceWaypoint(resource, false));
                }

                worldMap.updateWaypoints();

                // worldMap.setZoom(zoom);
                worldMap.setZoomRelativeToResources();

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

    public void setSelected(Resource selectedResource) {
        for (Waypoint waypoint : waypoints) {
            if (((ResourceWaypoint) waypoint).getResource() == selectedResource) {
                this.selectedWaypoint = (ResourceWaypoint) waypoint;
                ((ResourceWaypoint) waypoint).setSelected(true);
            } else {
                ((ResourceWaypoint) waypoint).setSelected(false);
            }
        }
        repaint();
    }

    public void registerResourceSelectionPanel(
            ResourceSelectionPanel resourceSelectionPanel) {
        this.resourceSelectionPanel = resourceSelectionPanel;
    }

    public Resource getSelectedResource() {
        if (selectedWaypoint == null) {
            return null;
        }
        return selectedWaypoint.getResource();
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

    public ResourceSelectionPanel getResourceSelectionPanel() {
        return resourceSelectionPanel;
    }

}
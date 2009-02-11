package ibis.deploy.gui.editor;

import ibis.deploy.gui.worldmap.MapUtilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public class MapEditor {

    private final JXMapKit mapKit = new JXMapKit();

    private JLabel coordinatesLabel = new JLabel();

    private static final GeoPosition DEFAULT_GEO_POSITION = new GeoPosition(
            52.332933, 4.866064);

    // map editor doesn't support default values, since it isn't straightforward
    // to implement it. Map editor uses two values which both could be defaults.
    public MapEditor(JPanel form, String text, double latitude, double longitude) {
        JPanel container = new JPanel(new BorderLayout());

        JLabel label = new JLabel(text, JLabel.TRAILING);
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        JPanel labelPanel = new JPanel(new BorderLayout());
        JCheckBox dummy = new JCheckBox();
        dummy.setSelected(true);
        dummy.setEnabled(false);
        labelPanel.add(dummy, BorderLayout.WEST);
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);

        MapUtilities.register(mapKit);
        mapKit.setTileFactory(MapUtilities.getDefaultTileFactory());
        mapKit.setMiniMapVisible(false);
        GeoPosition position;
        if (latitude == 0 && longitude == 0) {
            position = DEFAULT_GEO_POSITION;
        } else {
            position = new GeoPosition(latitude, longitude);
        }
        mapKit.getMainMap().setZoom(MapUtilities.INITIAL_MAP_ZOOM);
        mapKit.getMainMap().setCenterPosition(MapUtilities.INITIAL_MAP_CENTER);
        mapKit.getMainMap().setAddressLocation(position);
        mapKit.getMainMap().addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                GeoPosition position = mapKit.getMainMap().getCenterPosition();
                mapKit.getMainMap().setAddressLocation(
                        mapKit.getMainMap().convertPointToGeoPosition(
                                e.getPoint()));
                coordinatesLabel.setText(""
                        + mapKit.getMainMap().convertPointToGeoPosition(
                                e.getPoint()));
                mapKit.getMainMap().setCenterPosition(position);
                mapKit.repaint();
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
        JPanel mapPanel = new JPanel(new BorderLayout());
        coordinatesLabel.setText("" + position);
        mapPanel.add(mapKit, BorderLayout.CENTER);
        mapPanel.add(coordinatesLabel, BorderLayout.SOUTH);
        container.add(mapPanel, BorderLayout.CENTER);
        container.setMinimumSize(container.getPreferredSize());
        form.add(container);

    }

    public double getLatitude() {
        return mapKit.getMainMap().getAddressLocation().getLatitude();
    }

    public double getLongitude() {
        return mapKit.getMainMap().getAddressLocation().getLongitude();
    }

}

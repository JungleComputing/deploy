package ibis.deploy.gui.worldmap;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;

public class MapUtilities {

    private static List<JXMapKit> mapKits = new ArrayList<JXMapKit>();

    private static TileFactory defaultTileFactory = MicrosoftMapTileProvider
            .getDefaultTileFactory();

    private static JMenu mapMenu = new JMenu("Map Tile Provider");

    public static final GeoPosition INITIAL_MAP_CENTER = new GeoPosition(
            52.332042, 4.866736);

    public static final Color selectedClusterFillColor = new Color(255, 100,
            100, 200);
    public static final Color selectedClusterBorderColor = new Color(255, 100,
            100, 255);

    public static final Color defaultClusterBorderColor = new Color(100, 100,
            255, 255);
    public static final Color defaultClusterFillColor = new Color(100, 100,
            255, 150);

    static {

        ButtonGroup group = new ButtonGroup();
        
        JRadioButtonMenuItem mapMenuItem;
        
        
        //FIXME: fix google map providers
//        JRadioButtonMenuItem mapMenuItem = new JRadioButtonMenuItem(
//                "Google Terrain");
//        mapMenuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent arg0) {
//                defaultTileFactory = GoogleMapTerrainTileProvider
//                        .getDefaultTileFactory();
//                updateMapTileFactory();
//            }
//        });
//        group.add(mapMenuItem);
//        mapMenu.add(mapMenuItem);
//        
//        mapMenuItem = new JRadioButtonMenuItem("Google Street");
//        mapMenuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent arg0) {
//                defaultTileFactory = GoogleMapStreetTileProvider
//                        .getDefaultTileFactory();
//                updateMapTileFactory();
//            }
//        });
//        group.add(mapMenuItem);
//        mapMenu.add(mapMenuItem);
        
        mapMenuItem = new JRadioButtonMenuItem("Microsoft", true);
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

    public static void register(JXMapKit mapKit) {
        mapKits.add(mapKit);
    }

    public static TileFactory getDefaultTileFactory() {
        return defaultTileFactory;
    }

    private static void updateMapTileFactory() {
        for (JXMapKit mapKit : mapKits) {

            GeoPosition position = mapKit.getMainMap().getCenterPosition();
            mapKit.setTileFactory(defaultTileFactory);
            mapKit.getMainMap().setCenterPosition(position);
        }

    }

    public static JMenu getMapMenu() {
        return mapMenu;
    }

}

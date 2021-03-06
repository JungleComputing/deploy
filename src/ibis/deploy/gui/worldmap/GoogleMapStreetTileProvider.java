package ibis.deploy.gui.worldmap;

import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

public class GoogleMapStreetTileProvider {
    private static final String VERSION = "w2.83";

    private static final int minZoom = 1;

    private static final int maxZoom = 16;

    private static final int mapZoom = 17;

    private static final int tileSize = 256;

    private static final boolean xr2l = true;

    private static final boolean yt2b = true;

    // old: http://mt2.google.com/mt/v=w2p.81&hl=nl&x=2106&y=1348&zoom=5
    // new: http://mt3.google.com/mt?v=app.81&hl=nl&x=525&y=335&z=10&s=Galile
    private static final String baseURL = "http://mt0.google.com";

    private static final TileFactoryInfo GOOGLE_MAPS_TILE_INFO = new GoogleMapStreetTileFactoryInfo(
            minZoom, maxZoom, mapZoom, tileSize, xr2l, yt2b, baseURL);

    public static TileFactory getDefaultTileFactory() {
        return (new DefaultTileFactory(GOOGLE_MAPS_TILE_INFO));
    }

    private static class GoogleMapStreetTileFactoryInfo extends TileFactoryInfo {

        public GoogleMapStreetTileFactoryInfo(int minZoom, int maxZoom,
                int mapZoom, int tileSize, boolean xr2l, boolean ytb2,
                String baseURL) {
            super(minZoom, maxZoom, mapZoom, tileSize, xr2l, ytb2, baseURL,
                    null, null, null);
        }

        public String getTileUrl(int x, int y, int zoom) {
            return this.baseURL + "/mt?v=" + VERSION + "&hl=nl&x=" + x + "&y="
                    + y + "&z=" + ((mapZoom - zoom));
        }

    }
}

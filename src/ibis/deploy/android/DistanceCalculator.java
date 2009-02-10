package ibis.deploy.android;

import com.google.android.maps.GeoPoint;

public class DistanceCalculator {

    private static final int EARTH_RADIUS = 6371;

    public static double distance(GeoPoint p1, GeoPoint p2) {
        final double lat1 = p1.getLatitudeE6() / 1E6;
        final double lat2 = p2.getLatitudeE6() / 1E6;
        final double lon1 = p1.getLongitudeE6() / 1E6;
        final double lon2 = p2.getLongitudeE6() / 1E6;

        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}

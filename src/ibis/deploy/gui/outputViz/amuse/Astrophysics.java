package ibis.deploy.gui.outputViz.amuse;

import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.math.Vec4;

public class Astrophysics {
    public final static double SIGMA = 5.67037321E-8;
    public final static double WIEN = 2.8977685E-3;

    public final static double SOLAR_LUMINOSITY = 3.839E26;
    public final static double SOLAR_RADIUS = 6.955E8;
    public final static double EARTH_RADIUS = 6371;

    public final static double PARSEC = 3.08568025E16;
    public final static double DISTANCE_FACTOR = 25.0;
    public final static double STAR_RADIUS_FACTOR_SMALL = 0.5;
    public final static double STAR_RADIUS_AT_1000_SOLAR_RADII = 15.0;

    public final static double STAR_FORMULAE_INTERSECTION = find_intersection();

    public static Vec3 locationToScreenCoord(double x, double y, double z) {
        float fx = (float) (DISTANCE_FACTOR * (x / PARSEC));
        float fy = (float) (DISTANCE_FACTOR * (y / PARSEC));
        float fz = (float) (DISTANCE_FACTOR * (z / PARSEC));

        return new Vec3(fx, fy, fz);
    }

    public static float starToScreenRadius(double size) {
        double radius_in_solar = size / SOLAR_RADIUS;
        double radius_factor = 1000.0 / Math.pow(
                STAR_RADIUS_AT_1000_SOLAR_RADII, 2);

        float fs;
        if (radius_in_solar < STAR_FORMULAE_INTERSECTION) {
            fs = (float) (radius_in_solar * STAR_RADIUS_FACTOR_SMALL);
        } else {
            fs = (float) (Math.sqrt(radius_in_solar / radius_factor));
        }

        return fs;
    }

    private static double find_intersection() {
        double radius_factor = 1000.0 / Math.pow(
                STAR_RADIUS_AT_1000_SOLAR_RADII, 2);

        for (double i = 0.1; i < 10000.0; i += 0.01) {
            double diff = (i * STAR_RADIUS_FACTOR_SMALL)
                    - (Math.sqrt(i / radius_factor));
            if (diff > 0.0) {
                return i;
            }
        }
        return 0;
    }

    public static int indexOfStarRadius(double size) {
        double radius_in_solar = size / SOLAR_RADIUS;

        return (int) Math.round(radius_in_solar * 10);
    }

    public static float toScreenCoord(double parsecs) {
        float fx = (float) (DISTANCE_FACTOR * parsecs);

        return fx;
    }

    public static double starTemperature(
            double luminosity_in_solar_luminosities, double radius) {
        return Math.pow((luminosity_in_solar_luminosities / (4 * Math.PI
                * (radius * radius) * SIGMA)), 0.25);
    }

    private static double colorIntensity(double max, double min, double current) {
        return (current - min) / (max - min);
    }

    public static Vec4 starColor(double luminosity, double radius) {
        luminosity *= SOLAR_LUMINOSITY;

        double temperature = starTemperature(luminosity, radius);

        // Vec4 color;
        // float intensity = 0f;
        //
        // HashMap<Integer, Double> temperatureBands = new HashMap<Integer,
        // Double>();
        // temperatureBands.put(0, 2000.0);
        // temperatureBands.put(1, 3500.0);
        // temperatureBands.put(2, 5000.0);
        // temperatureBands.put(3, 6000.0);
        // temperatureBands.put(4, 7500.0);
        // temperatureBands.put(5, 10000.0);
        // temperatureBands.put(6, 30000.0);
        // temperatureBands.put(7, 60000.0);
        //
        // for (int i = 1; i < temperatureBands.size(); i++) {
        // if (temperature <= temperatureBands.get(i)) {
        // intensity = (float) (colorIntensity(temperatureBands.get(i),
        // temperatureBands.get(i - 1), temperature));
        // }
        // }
        //
        // intensity *= 0.25f;

        float r = 0f, g = 0f, b = 0f;

        if /* temperature > 2000 */(temperature <= 3500) { // M
            r = 255f / 255f;
            g = 204f / 255f;
            b = 111f / 255f;
        } else if (temperature > 3500 && temperature <= 5000) { // K
            r = 255f / 255f;
            g = 210f / 255f;
            b = 161f / 255f;
        } else if (temperature > 5000 && temperature <= 6000) { // G
            r = 255f / 255f;
            g = 244f / 255f;
            b = 234f / 255f;
        } else if (temperature > 6000 && temperature <= 7500) { // F
            r = 248f / 255f;
            g = 247f / 255f;
            b = 255f / 255f;
        } else if (temperature > 7500 && temperature <= 10000) {// A
            r = 202f / 255f;
            g = 215f / 255f;
            b = 255f / 255f;
        } else if (temperature > 10000 && temperature <= 30000) {// B
            r = 170f / 255f;
            g = 191f / 255f;
            b = 255f / 255f;
        } else { /* temperature > 30000 && temperature <= 60000 */// O
            r = 155f / 255f;
            g = 176f / 255f;
            b = 255f / 255f;
        }

        return new Vec4(r, g, b, 1f); // M;
    }
}

package ibis.deploy.vizFramework.globeViz.viz.utils;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;

public class RandomDataGenerator {


    // generates a list with nPoints random locations
    public static ArrayList<Position> generatePositionList(int nPoints) {
            ArrayList<Position> positionList = new ArrayList<Position>();
            for (int i = 0; i < nPoints; i++) {
                    positionList.add(generateRandomPosition());
            }

            return positionList;
    }

    // generates one random geographic location
    public static Position generateRandomPosition() {
            double lat, longit;
            lat = generateRandomLatitude();
            longit = generateRandomLongitude();
            LatLon pos1 = LatLon.fromDegrees(lat, longit);

            return new Position(pos1, 0);
    }
    
    public static double generateRandomLatitude(){
        return (Math.random() * 1000) % 180 - 90;
    }
    
    public static double generateRandomLongitude(){
        return (Math.random() * 1000) % 360 - 180;
    }
}

package ibis.deploy.vizFramework.globeViz.viz.markers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

import java.util.Vector;

public class MarkerPool {

    private Vector<MovingMarker> passiveMarkers;
    private Vector<MovingMarker> activeMarkers;
    
    public MarkerPool(){
        passiveMarkers = new Vector<MovingMarker>();
        activeMarkers = new Vector<MovingMarker>();
    }
    
    public MovingMarker getMarker(Position pos, BasicMarkerAttributes attrs){
        MovingMarker marker;
        if(passiveMarkers.size() > 0){
            marker = passiveMarkers.remove(0);
            marker.setAttributes(attrs);
            marker.setPosition(pos);
        } else {
            marker = new MovingMarker(pos, attrs);
        }
        
        activeMarkers.add(marker);
        return marker;
    }
    
    public void returnMarkerToPool(MovingMarker marker){
        activeMarkers.remove(marker);
        passiveMarkers.add(marker);
    }
    
    public Vector<MovingMarker> getActiveMarkers(){
        return activeMarkers;
    }
}

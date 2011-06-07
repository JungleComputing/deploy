package ibis.deploy.vizFramework.globeViz.viz.markers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;

import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

import java.awt.Color;
import java.util.Vector;

public class MarkerPool {

    private Vector<MovingMarker> passiveMarkers;
    private Vector<MovingMarker> activeMarkers;
    
    public MarkerPool(){
        passiveMarkers = new Vector<MovingMarker>();
        activeMarkers = new Vector<MovingMarker>();
    }
    
    public MovingMarker getMarker(Position pos, Color color){
        MovingMarker marker;
        if(passiveMarkers.size() > 0){
            marker = passiveMarkers.remove(0);
            marker.getAttributes().setMaterial(new Material(color));
            marker.setPosition(pos);
        } else {
            BasicMarkerAttributes attrs = new BasicMarkerAttributes(new Material(Color.GREEN),
                  BasicMarkerShape.SPHERE, 0.5);
            attrs.setMarkerPixels(UIConstants.MARKER_SIZE);
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

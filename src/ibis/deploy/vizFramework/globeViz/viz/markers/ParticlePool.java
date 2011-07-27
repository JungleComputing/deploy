package ibis.deploy.vizFramework.globeViz.viz.markers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;

import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

import java.awt.Color;
import java.util.Vector;

public class ParticlePool {

    private Vector<MovingParticle> passiveMarkers;
    private Vector<MovingParticle> activeMarkers;
    
    public ParticlePool(){
        passiveMarkers = new Vector<MovingParticle>();
        activeMarkers = new Vector<MovingParticle>();
    }
    
    public MovingParticle getMarker(Position pos, Color color){
        MovingParticle marker;
        if(passiveMarkers.size() > 0){
            marker = passiveMarkers.remove(0);
            marker.getAttributes().setMaterial(new Material(color));
            marker.setPosition(pos);
        } else {
            BasicMarkerAttributes attrs = new BasicMarkerAttributes(new Material(Color.GREEN),
                  BasicMarkerShape.SPHERE, 0.5);
            attrs.setMarkerPixels(UIConstants.MARKER_SIZE);
            marker = new MovingParticle(pos, attrs);
        }
        
        activeMarkers.add(marker);
        return marker;
    }
    
    public void returnAllMarkersToPool(){
        passiveMarkers.addAll(activeMarkers);
        activeMarkers.clear();
    }
    
    public void returnMarkerToPool(MovingParticle marker){
        activeMarkers.remove(marker);
        passiveMarkers.add(marker);
    }
    
    public Vector<MovingParticle> getActiveMarkers(){
        return activeMarkers;
    }
}

package ibis.deploy.vizFramework.globeViz.viz.markers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

public class MovingParticle extends BasicMarker {
    
    private int currentIndex;
    //private int maxIndex;
    
    public MovingParticle(Position pos, BasicMarkerAttributes attrs){
        super(pos, attrs);
        currentIndex = 0;
        //this.maxIndex = maxIndex;
    }
    
    public int move(){
        currentIndex++;
//        if(currentIndex >= maxIndex){
//            currentIndex = 0;
//        }
        return currentIndex;
    }
    
    public void resetIndex(){
        currentIndex = 0;
    }
}

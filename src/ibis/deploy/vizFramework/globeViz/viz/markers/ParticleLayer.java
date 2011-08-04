package ibis.deploy.vizFramework.globeViz.viz.markers;

import java.util.Vector;

import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * @author Ana Vinatoru
 *
 */

public class ParticleLayer extends MarkerLayer {

    public ParticleLayer() {
        super();
    }

    public ParticleLayer(Iterable<Marker> markers) {
        super(markers);
    }

    protected synchronized void doRender(DrawContext dc) {
        draw(dc, null);
    }
    
    protected synchronized void doPick(DrawContext dc, java.awt.Point pickPoint) {
        draw(dc, pickPoint);
    }
    
    protected synchronized void draw(DrawContext dc, java.awt.Point pickPoint){
        super.draw(dc, pickPoint);
    }

    public synchronized Vector<Marker> getMarkers() {
        Vector<Marker> markers = (Vector<Marker>) super.getMarkers();

        if (markers == null) {
            markers = new Vector<Marker>();
        }
        setMarkers(markers);
        return markers;
    }

    public synchronized void addMarker(Marker m) {
        getMarkers().add(m);
    }

    public synchronized boolean removeMarker(Marker m) {
        return getMarkers().remove(m);
    }
    
    public synchronized void clearMarkers(){
        getMarkers().clear();
    }
    
    public synchronized void addAllMarkers(Vector<MovingParticle> markers){
        getMarkers().addAll(markers);
    }
}

package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import ibis.deploy.gui.GUI;
import ibis.deploy.vizFramework.IVisualization;
import ibis.deploy.vizFramework.globeViz.data.GlobeDataConvertor;
import ibis.deploy.vizFramework.globeViz.viz.markers.SynchronizedMarkerLayer;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JPanel;

public class GlobeVisualization extends JPanel implements IVisualization {
    private static final long serialVersionUID = 1L;
    private static WorldWindowGLCanvas worldWindCanvas;

    private GlobeAnnotation tooltipAnnotation;
    private CircleAnnotation lastSelectedDot;
    private RenderableLayer annotationLayer;
    private RenderableLayer polylineLayer;
    private SynchronizedMarkerLayer markerLayer;
    private boolean followTerrain = false;
    private GlobeDataConvertor convertor;
    private GUI gui;

    public GlobeVisualization(GUI gui) {

        this.gui = gui;

        // create a WorldWind main object
        worldWindCanvas = new WorldWindowGLCanvas();
        worldWindCanvas.setModel(new BasicModel());

        this.setLayout(new BorderLayout());
        this.add(worldWindCanvas, BorderLayout.CENTER);

        // initialize the annotation layer
        annotationLayer = new RenderableLayer();
        annotationLayer.setName("Locations");
        worldWindCanvas.getModel().getLayers().add(annotationLayer);

        // initialize the polylineLayer layer
        polylineLayer = new RenderableLayer();
        polylineLayer.setName("Edges");
        worldWindCanvas.getModel().getLayers().add(polylineLayer);

        markerLayer = new SynchronizedMarkerLayer();
        markerLayer.setName("Markers");
        markerLayer.setKeepSeparated(false);
        worldWindCanvas.getModel().getLayers().add(markerLayer);

        // only used when we want fake-fake data :P
        // RandomDataGenerator.generateRandomDotsAndConnections(this);

        // RandomDataGenerator.generateFixedLocations(annotationLayer, this);

        createTooltip();

        // create a listener for displaying the tooltip
        worldWindCanvas.addSelectListener(new SelectListener() {

            public void selected(SelectEvent event) {
                if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                    highlight(event.getTopObject());
            }
        });

        // //temporarily disable some layers for debugging - TODO - remove
//        for (Layer layer : worldWindCanvas.getModel().getLayers()) {
//            if (layer.getName().equals("NASA Blue Marble Image")
//                    || layer.getName().equals("Blue Marble (WMS) 2004")) {
//                layer.setEnabled(false);
//            }
//        }

    }

    public RenderableLayer getAnnotationLayer() {
        return annotationLayer;
    }

    public static WorldWindowGLCanvas getVisualization() {
        return worldWindCanvas;
    }

    // tooltip initialization
    private void createTooltip() {

        // Initialize tooltip annotation
        tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0,
                0));

        // Create attributes for the tooltip --> Make sure to always set
        // imagesource to null, otherwise everything blows up :)
        AnnotationAttributes tooltipAttributes = new AnnotationAttributes();
        tooltipAttributes.setCornerRadius(10);
        tooltipAttributes.setInsets(new Insets(8, 8, 8, 8));
        tooltipAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .85f));
        tooltipAttributes.setDrawOffset(new Point(25, 25));
        tooltipAttributes.setDistanceMinScale(.5);
        tooltipAttributes.setDistanceMaxScale(2);
        tooltipAttributes.setDistanceMinOpacity(.5);
        tooltipAttributes.setLeaderGapWidth(14);
        tooltipAttributes.setDrawOffset(new Point(20, 40));
        tooltipAttributes.setImageSource(null);
        tooltipAttributes.setFont(Font.decode("Arial-BOLD-12"));
        tooltipAttributes.setTextColor(Color.WHITE);

        tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(10,
                100, 0), tooltipAttributes);
        tooltipAnnotation.getAttributes().setSize(new Dimension(150, 0));
        tooltipAnnotation.getAttributes().setVisible(false);
        tooltipAnnotation.setAlwaysOnTop(true);

        annotationLayer.addRenderable(tooltipAnnotation);
    }

    // displays the name of a location on mouse over
    private void highlight(Object o) {
        if (lastSelectedDot == o)
            return; // same thing selected

        if (lastSelectedDot != null) {
            lastSelectedDot.getAttributes().setHighlighted(false);
            lastSelectedDot = null;
            tooltipAnnotation.getAttributes().setVisible(false);
        }

        if (o != null && o instanceof CircleAnnotation) {
            lastSelectedDot = (CircleAnnotation) o;
            lastSelectedDot.getAttributes().setHighlighted(true);
            tooltipAnnotation.setText("<p>" + lastSelectedDot.getName()
                    + "</p>");
            tooltipAnnotation.setPosition(lastSelectedDot.getPosition());
            tooltipAnnotation.getAttributes().setVisible(true);
            worldWindCanvas.repaint();
        }
    }

    // computes a polyline between the two locations
    public UnclippablePolyline createArcBetween(Position pos1, Position pos2,
            Color color, int lineWidth, boolean applyOffset) {
        UnclippablePolyline polyline;
        ArrayList<Position> polylineList = new ArrayList<Position>();

        // add the control points to the list
        polylineList.addAll(doTheSplits(pos1, pos2,
                UIConstants.NUMBER_OF_CONTROL_POINTS, !followTerrain, applyOffset));

        // create the points of the BSpline using the control points.
        polylineList = BSpline3D.computePolyline(worldWindCanvas.getModel()
                .getGlobe(), polylineList);

        // //this is to add more knots in the straight part of the edge
        Position tempPos = polylineList.remove(0);
        polylineList
                .addAll(0,
                        doTheSplits(tempPos, polylineList.get(0), 2, true,
                                applyOffset));

        tempPos = polylineList.remove(polylineList.size() - 1);
        polylineList.addAll(doTheSplits(
                polylineList.get(polylineList.size() - 1), tempPos, 2, true,
                applyOffset));

        polyline = new UnclippablePolyline(polylineList);
        polyline.setColor(color);
        polyline.setLineWidth(lineWidth);
        polyline.setFollowTerrain(followTerrain);

        return polyline;
    }

    // calculates the interpolation point for pos1 and pos2
    private Position getMidPoint(Position pos1, Position pos2,
            boolean adjustHeight, boolean applyOffset) {

        Position pos3 = Position.interpolateGreatCircle(0.5, pos1, pos2);
        if (adjustHeight) {
            double newHeight;
            if (!applyOffset) {
                newHeight = LatLon.greatCircleDistance(pos1, pos2).degrees
                        * UIConstants.ARC_HEIGHT;
            } else {
                newHeight = LatLon.greatCircleDistance(pos1, pos2).degrees
                        * UIConstants.ARC_SECOND_HEIGHT;
            }
            pos3 = new Position(pos3.latitude, pos3.longitude, newHeight);
        }

        return pos3;
    }

    private ArrayList<Position> doTheSplits(Position pos1, Position pos2,
            int depth, boolean adjustHeight, boolean applyOffset) {

        ArrayList<Position> l1, l2, list = new ArrayList<Position>();

//        if (applyOffset) {
//            Globe globe = worldWindCanvas.getModel().getGlobe();
//            // Vec4 pos = Utils.fromPositionToScreen(pos1, globe,
//            // worldWindCanvas.getView());
//            // pos = new Vec4(pos.x + 1000, pos.y + 1000, pos.z);
//            // pos1 = Utils.fromScreenToPosition(pos, globe,
//            // worldWindCanvas.getView());
//
//            Vec4 pos = Utils.fromPositionTo3DCoords(pos2, globe);
//            // System.out.println(worldWindCanvas.getView().getEyePosition().getElevation());
//            pos = new Vec4(pos.x + 1000, pos.y + 1000, pos.z);
//            pos2 = Utils.from3DCoordsToPosition(pos, globe);
//        }

        if (depth == 0) {
            list.add(pos1);
            list.add(pos2);
            return list;
        }

        Position pos3 = getMidPoint(pos1, pos2, adjustHeight, applyOffset);

        l1 = doTheSplits(pos1, pos3, depth - 1, false, false);
        l2 = doTheSplits(pos3, pos2, depth - 1, false, false);

        l1.remove(l1.size() - 1); // remove the last element, otherwise we'll
        // have the midpoint two times in the final list

        list.addAll(l1);
        list.addAll(l2);

        return list;
    }

    public void clearAnnotationLayer() {
        // clear everything
        annotationLayer.removeAllRenderables();

        // add the tooltip back
        annotationLayer.addRenderable(tooltipAnnotation);
    }

    // TODO - maybe remove this one if it's unused
    public void drawArc(Position pos1, Position pos2, Color color, int lineWidth) {
        polylineLayer.addRenderable(createArcBetween(pos1, pos2, color,
                lineWidth, false));
    }

    public void drawArc(UnclippablePolyline line, Color color) {
        line.setColor(color);
        polylineLayer.addRenderable(line);
    }

    public void clearPolylineLayer() {
        polylineLayer.removeAllRenderables();
    }

    public void redraw() {
        worldWindCanvas.redraw();
    }

    public void setFollowTerrain(boolean value) {
        if (convertor != null) {
            followTerrain = value;
            convertor.forceUpdate();
            worldWindCanvas.repaint();
        }
    }

    public void setShowParticles(boolean value) {
        if (convertor != null) {
            convertor.setShowParticles(value);
            worldWindCanvas.repaint();
        }
    }

    public void setDataConvertor(GlobeDataConvertor convertor) {
        this.convertor = convertor;
        convertor.setGUI(gui);
    }

    public SynchronizedMarkerLayer getMarkerLayer() {
        return markerLayer;
    }

    public void setPolylinesEnabled(boolean enabled) {
        polylineLayer.setEnabled(enabled);
    }
}

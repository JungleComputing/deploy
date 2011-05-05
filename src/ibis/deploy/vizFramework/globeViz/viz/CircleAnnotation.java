package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import ibis.deploy.util.Colors;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

//TODO - maybe adjust circle size according to location size
public class CircleAnnotation extends GlobeAnnotation {
    private String locationName;

    private Set<String> clusters;

    private ArrayList<Color> clusterColors;

    private HashMap<CircleAnnotation, Double> connections;

    public CircleAnnotation(Position position, AnnotationAttributes defaults,
            String name) {
        super(name, position, defaults);
        locationName = name;
        clusters = new HashSet<String>();
        clusters.add(name);

        clusterColors = new ArrayList<Color>();
        clusterColors.add(Colors.fromLocation(name));
        connections = new HashMap<CircleAnnotation, Double>();

    }

    // public CircleAnnotation(ArrayList<CircleAnnotation> clusterAnnotations,
    // AnnotationAttributes defaults, String name) {
    // super(name, Position.ZERO, defaults);
    // locationName = name;
    // clusters = new HashSet<String>();
    //
    // if (clusterAnnotations != null && clusterAnnotations.size() > 0) {
    //
    // for (CircleAnnotation annotation : clusterAnnotations) {
    // clusters.addAll(annotation.getClusters());
    // }
    //
    // if (clusterAnnotations.size() == 1) {
    // setPosition(clusterAnnotations.get(0).getPosition());
    // } else {
    // Position intermediate = Position.interpolate(0.5,
    // clusterAnnotations.get(0).getPosition(),
    // clusterAnnotations.get(1).getPosition());
    //
    // for (int i = 1; i < clusterAnnotations.size(); i++) {
    // intermediate = Position.interpolate(0.2, intermediate,
    // clusterAnnotations.get(i).getPosition());
    // }
    //
    // setPosition(intermediate);
    // }
    // }
    // }

    public CircleAnnotation(ArrayList<String> clusterNames,
            ArrayList<Position> positions, AnnotationAttributes defaults) {
        super(clusterNames.toString(), Position.ZERO, defaults);

        // there might be compound names, so split them before using them
        HashSet<String> finalClusterNames = new HashSet<String>();
        for (String name : clusterNames) {
            StringTokenizer st = new StringTokenizer(name, "[] ,");
            while (st.hasMoreElements()) {
                finalClusterNames.add(st.nextToken());
            }
        }

        locationName = finalClusterNames.toString();
        clusters = new HashSet<String>();
        clusterColors = new ArrayList<Color>();
        connections = new HashMap<CircleAnnotation, Double>();

        if (finalClusterNames != null && finalClusterNames.size() > 0) {

            clusters.addAll(finalClusterNames);

            if (positions.size() == 1) {
                setPosition(positions.get(0));
            } else {
                Position intermediate = Position.interpolate(0.5,
                        positions.get(0), positions.get(1));

                for (int i = 1; i < positions.size(); i++) {
                    intermediate = Position.interpolate(0.5, intermediate,
                            positions.get(i));
                }

                setPosition(intermediate);
            }

            for (String cluster : finalClusterNames) {
                clusterColors.add(Colors.fromLocation(cluster));
            }
        }
    }

    public boolean containsCluster(String clusterName) {
        return clusters.contains(clusterName);
    }

    public void addConnection(CircleAnnotation destination, double value) {
        if (connections.containsKey(destination)) {
            connections.put(destination, connections.get(destination) + value);
        } else {
            connections.put(destination, value);
        }
    }

    public void setClusters(Set<String> newClusters) {
        clusters.clear();
        clusters.addAll(newClusters);
    }

    public Set<String> getClusters() {
        return clusters;
    }

    // // TODO - real comparison
    // public boolean containsSameClustersAs(ArrayList<CircleAnnotation> others)
    // {
    // Set<String> otherClusterNames = new HashSet<String>();
    // for (CircleAnnotation other : others) {
    // otherClusterNames.addAll(other.getClusters());
    // }
    //
    // if (clusters.size() != otherClusterNames.size()) {
    // return false;
    // }
    //
    // for (String cluster : clusters) {
    // if (!otherClusterNames.contains(cluster)) {
    // return false;
    // }
    // }
    //
    // return true;
    // }

    // TODO - there must never be a [], or space in the cluster name
    public boolean containsSameClustersAs(ArrayList<String> others) {

        ArrayList<String> names = new ArrayList<String>();

        for (String name : others) {
            StringTokenizer st = new StringTokenizer(name, "[] ,");
            while (st.hasMoreElements()) {
                names.add(st.nextToken());
            }
        }

        if (clusters.size() != names.size()) {
            return false;
        }

        for (String cluster : clusters) {
            if (!names.contains(cluster)) {
                return false;
            }
        }

        return true;
    }

    public void setLocationName(String name) {
        locationName = name;
    }

    public String getName() {
        return locationName;
    }

    protected void applyScreenTransform(DrawContext dc, int x, int y,
            int width, int height, double scale) {
        double finalScale = scale * this.computeScale(dc);

        GL2 gl = dc.getGL().getGL2();
        gl.glTranslated(x, y, 0);
        gl.glScaled(finalScale, finalScale, 1);
    }

    // Override annotation drawing for a simple circle
    private DoubleBuffer shapeBuffer;
    private FloatBuffer colorBuffer;

    protected void doDraw(DrawContext dc, int width, int height,
            double opacity, Position pickPosition) {
        // Draw colored circle around screen point - use annotation's text color
        if (dc.isPickingMode()) {
            this.bindPickableObject(dc, pickPosition);
        }

        this.applyColor(dc, this.getAttributes().getTextColor(), 0.7 * opacity,
                true);

        int size = UIConstants.LOCATION_CIRCLE_SIZE;
        if (shapeBuffer == null) {
            shapeBuffer = CustomFrameFactory.createShapeBuffer(
                    FrameFactory.SHAPE_ELLIPSE, size, size, 0, null);

            // FrameFactory.createShapeBuffer(
            // FrameFactory.SHAPE_ELLIPSE, size, size, 0, null);
        }

        if (colorBuffer == null) {
            colorBuffer = CustomFrameFactory.createColorBuffer(colorBuffer);
        }

        dc.getGL().getGL2().glTranslated(-size / 2, -size / 2, 0);
        CustomFrameFactory.drawCustomBuffer(dc, GL.GL_TRIANGLE_FAN,
                shapeBuffer, colorBuffer, clusterColors);
        // FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);
    }

    public double computeDistance(CircleAnnotation wp,
            WorldWindowGLCanvas canvas) {

        Globe globe = canvas.getModel().getGlobe();
        View view = canvas.getView();

        return computeDistanceTo(wp, globe, view);
    }

    public double computeDistanceTo(CircleAnnotation wp, Globe globe, View view) {
        Position p1 = this.getPosition();
        Position p2 = wp.getPosition();

        if (p1 != null && p2 != null) {
            Vec4 point1 = globe.computePointFromPosition(p1);
            Vec4 point2 = globe.computePointFromPosition(p2);

            if (point1 != null && point2 != null) {
                point1 = view.project(point1);
                point2 = view.project(point2);

                if (point1 != null && point2 != null) {
                    return point1.distanceTo3(point2);
                }
            }
        }

        return -1;
    }
}

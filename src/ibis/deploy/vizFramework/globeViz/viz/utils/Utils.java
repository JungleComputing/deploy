package ibis.deploy.vizFramework.globeViz.viz.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.visual.EdgeItem;
import ibis.deploy.vizFramework.bundles.edgeBundles.BSplineEdgeItem;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

public class Utils {
    
    public static double MAX_EDGE_WEIGHT = 0;
    public static double MIN_EDGE_WEIGHT = 1;
    
    public static double computeDistance(Position p1, Position p2, Globe globe,
            View view) {

        if (p1 != null && p2 != null) {
            Vec4 point1 = globe.computePointFromPosition(p1);
            Vec4 point2 = globe.computePointFromPosition(p2);

            if (point1 != null && point2 != null) {

                if (view.getFrustumInModelCoordinates().contains(point1)
                        && view.getFrustumInModelCoordinates().contains(point2)) {
                    point1 = view.project(point1);
                    point2 = view.project(point2);

                    if (point1 != null && point2 != null) {
                        return point1.distanceTo3(point2);
                    }
                }
            }
        }

        return -1;
    }

    public static Vec4 fromPositionToScreen(Position pos, Globe globe, View view) {
        Vec4 vecPos;
        vecPos = globe.computePointFromPosition(pos);
        return view.project(vecPos);
    }
    
    public static Position fromScreenToPosition(double x, double y, double z, Globe globe, View view){
        Vec4 vecpos = view.unProject(new Vec4(x, y, z));
        Position pos = globe.computePositionFromPoint(vecpos);
        return new Position(pos.getLatitude(), pos.getLongitude(), 0);
        //return globe.computePositionFromPoint(temp);
    }

    @SuppressWarnings("unchecked")
    public static void forceEdgeUpdate(Visualization vis) {
        Iterator<EdgeItem> edgeIter;
        BSplineEdgeItem edge;
    
        // update all edges - when a node is moved, all edges must be recomputed
        Graph graph = (Graph) vis.getGroup(UIConstants.GRAPH);
        edgeIter = graph.edges();
        while (edgeIter.hasNext()) {
            try {
                edge = (BSplineEdgeItem) edgeIter.next();
                edge.setUpdated(false);
            } catch (IllegalArgumentException exc) {
                // same story here
                System.err.println(exc.getMessage());
            }
        }
    }

    public static float fromIntervalToInterval(double x, double minx,
            double maxx, double miny, double maxy) {
        if (maxx == minx) {
            return 1;
        }
        return (float) (maxy - (maxy - miny) * (x - minx) / (maxx - minx));
    }

    @SuppressWarnings("unchecked")
    public static void computeEdgeAlphas(Visualization vis, Tree tree) {
        Iterator<EdgeItem> edgeIter = vis.visibleItems("graph.edges");
        BSplineEdgeItem edge;
        int minlength = Integer.MAX_VALUE, maxlength = Integer.MIN_VALUE, tsize;
    
        // force control point recalculation
        forceEdgeUpdate(vis);
    
        while (edgeIter.hasNext()) {
            try {
                edge = (BSplineEdgeItem) edgeIter.next();
                edge.computeControlPoints(false, 1, edge, tree);
                tsize = edge.getControlPoints().size();
                if (tsize > maxlength) {
                    maxlength = tsize;
                }
                if (tsize < minlength) {
                    minlength = tsize;
                }
            } catch (IllegalArgumentException exc) {
                System.err.println(exc.getMessage());
                // Prefuse sometimes throws this exception if the node / edge
                // has been recently removed. I think it's due to
                // synchronization issues, plus instead of returning false,
                // Prefuse just throws an exception
            }
        }
    
        Graph graph = (Graph) vis.getGroup(UIConstants.GRAPH);
        edgeIter = graph.edges();
    
        while (edgeIter.hasNext()) {
            try {
                edge = (BSplineEdgeItem) edgeIter.next();
                edge.setAlpha(fromIntervalToInterval(edge.getControlPoints()
                        .size(), minlength, maxlength, UIConstants.minAlpha, UIConstants.maxAlpha));
            } catch (IllegalArgumentException exc) {
                // same story here
                System.err.println(exc.getMessage());
            }
    
        }
    }

    public static Color blend(Color color1, Color color2, double ratio,
            float alpha) {
        float r = (float) ratio;
        float ir = (float) 1.0 - r;
    
        float rgb1[] = new float[3];
        float rgb2[] = new float[3];
    
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
    
        rgb1[0] = rgb1[0] * r + rgb2[0] * ir;
        rgb1[1] = rgb1[1] * r + rgb2[1] * ir;
        rgb1[2] = rgb1[2] * r + rgb2[2] * ir;
    
        if (rgb1[0] > 1) {
            rgb1[0] = 1;
        } else if(rgb1[0] < 0){
            rgb1[0] = 0;
        }
    
        if (rgb1[1] > 1) {
            rgb1[1] = 1;
        } else if(rgb1[1] < 0){
            rgb1[1] = 0;
        }
    
        if (rgb1[2] > 1) {
            rgb1[2] = 1;
        } else if(rgb1[2] < 0){
            rgb1[2] = 0;
        }
        return new Color(rgb1[0], rgb1[1], rgb1[2], alpha);
    }

    public static String getRandomColor() {
        int idx = ((int) (Math.random() * 100)) % UIConstants.colors.length;
        return UIConstants.colors[idx];
    }

    public static void updateMinMaxWeights(
            HashMap<String, HashMap<String, Double>> connectionsPerIbis) {
        MAX_EDGE_WEIGHT = Double.MIN_VALUE;
        MIN_EDGE_WEIGHT = Double.MAX_VALUE;
        for (HashMap<String, Double> connections : connectionsPerIbis.values()) {
            for (Double value : connections.values()) {
                if (value > MAX_EDGE_WEIGHT) {
                    MAX_EDGE_WEIGHT = value;
                }
    
                if (value < MIN_EDGE_WEIGHT) {
                    MIN_EDGE_WEIGHT = value;
                }
            }
        }
    }

    public static String getNextColor() {
        if (UIConstants.colorIndex == UIConstants.colors.length) {
            UIConstants.colorIndex = 0;
        }
    
        return UIConstants.colors[UIConstants.colorIndex++];
    }
}

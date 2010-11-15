package ibis.deploy.gui.deployViz.helpers;

import java.awt.Color;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.Tree;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import ibis.deploy.gui.deployViz.edgeBundles.BSplineEdgeItem;

public class VizUtils {

    public static final String CLUSTER = "cluster";
    public static final String HEAD_NODE = "head node";
    public static final String COMPUTE_NODE = "compute node";

    public static final int BSPLINE_EDGE_TYPE = 100;
    public static final double INITIAL_BUNDLING_FACTOR = 0.9;

    public static final Color DEFAULT_START_COLOR = Color.green;
    public static final Color DEFAULT_STOP_COLOR = Color.red;

    public static final int SELECTED_FILL_COLOR = ColorLib.rgb(0, 0, 255);
    public static final int SELECTED_TEXT_COLOR = ColorLib.rgb(255, 255, 255);

    public static final int DEFAULT_TEXT_COLOR = ColorLib.rgb(0, 0, 0);

    private static final String[] colors = { "#FF0000", "#FF8000", "#80FF00",
            "#00FF00", "#00FF80", "#00FFFF", "#007FFF", "#8000FF", "#FF0080",
            "#FF8080", "#FFBF80", "#FFFF80", "#BFFF80", "#80FF80", "#80FFBF",
            "#80FFFF", "#80BFFF", "#8080FF", "#BF80FF", "#FF80FF", "#FF80BF",
            "#008040", "#008080" };

    private static int colorIndex = 0;

    private static double minAlpha = 0.3, maxAlpha = 0.8;

    public static String getNextColor() {
        if (colorIndex == colors.length) {
            colorIndex = 0;
        }

        return colors[colorIndex++];
    }

    public static String getRandomColor() {
        int idx = ((int) (Math.random() * 100)) % colors.length;
        return colors[idx];
    }

    public static Color blend(Color color1, Color color2, double ratio,
            float alpha) {
        float r = (float) ratio;
        float ir = (float) 1.0 - r;

        float rgb1[] = new float[3];
        float rgb2[] = new float[3];

        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);

        Color color = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r
                + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir, alpha);

        return color;
    }

    public static void computeEdgeAlphas(Visualization vis, Tree tree) {
        Iterator<EdgeItem> edgeIter = vis.visibleItems("graph.edges");
        BSplineEdgeItem edge;
        int minlength = Integer.MAX_VALUE, maxlength = Integer.MIN_VALUE, tsize;

        while (edgeIter.hasNext()) {
            edge = (BSplineEdgeItem) edgeIter.next();
            edge.computeControlPoints(false, 1, edge, tree);
            tsize = edge.getControlPoints().size();
            if (tsize > maxlength) {
                maxlength = tsize;
            }
            if (tsize < minlength) {
                minlength = tsize;
            }
        }

        edgeIter = vis.visibleItems("graph.edges");
        while (edgeIter.hasNext()) {
            edge = (BSplineEdgeItem) edgeIter.next();
            edge.setAlpha(fromIntervalToInterval(
                    edge.getControlPoints().size(), minlength, maxlength,
                    minAlpha, maxAlpha));
        }
    }

    public static float fromIntervalToInterval(double x, double minx,
            double maxx, double miny, double maxy) {
        if (maxx == minx) {
            return 1;
        }
        return (float) (maxy - (maxy - miny) * (x - minx) / (maxx - minx));
    }

    public static void forceEdgeUpdate(Visualization vis) {
        Iterator<EdgeItem> edgeIter;
        BSplineEdgeItem edge;

        // update all edges - when a node is moved, all edges must be recomputed
        edgeIter = vis.visibleItems("graph.edges");
        while (edgeIter.hasNext()) {
            edge = (BSplineEdgeItem) edgeIter.next();
            edge.setUpdated(false);
        }
    }

    public static int MAX_EDGE_WEIGHT = 50;
}

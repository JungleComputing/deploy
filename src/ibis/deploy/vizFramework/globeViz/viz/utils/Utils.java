package ibis.deploy.vizFramework.globeViz.viz.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.visual.EdgeItem;
import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.vizFramework.bundles.edgeBundles.BSplineEdgeItem;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.ipl.IbisIdentifier;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

public class Utils {

    public static double MAX_EDGE_WEIGHT = 0;
    public static double MIN_EDGE_WEIGHT = 1;

    // Bucharest, Barcelona, Amsterdam,
    // Delft, Los Angeles, Copenhagen, Moscow, Sydney, Tokyo, Leiden, Athens,
    // Cape Town, New Delhi
    private static double[] latitudes = { 44.433, 41.38, 52.35, 52, 34.05,
            55.75, 55.66, -33.9, 35.68, 52.15, 37.98, -33.91, 24.73 };
    private static double[] longitudes = { 26.1, 2.18, 4.91, 4.36, -118.24,
            37.61, 12.58, 151.2, 139.75, 4.5, 23.73, 18.42, 81.33 };

    private static HashMap<String, LatLon> latLonsPerLocation = new HashMap<String, LatLon>();

    static Cluster[] clusters;
    static GUI gui;

    public static double computeDistance(Position p1, Position p2, Globe globe,
            View view) {

        if (p1 != null && p2 != null) {
            Vec4 point1 = globe.computePointFromPosition(p1);
            Vec4 point2 = globe.computePointFromPosition(p2);

            if (point1 != null && point2 != null) {

                // if (view.getFrustumInModelCoordinates().contains(point1)
                // && view.getFrustumInModelCoordinates().contains(point2)) {

                boolean p1Visible = view.getFrustumInModelCoordinates()
                        .contains(point1);
                boolean p2Visible = view.getFrustumInModelCoordinates()
                        .contains(point2);

                point1 = view.project(point1);
                point2 = view.project(point2);

                // compute distances only if the points are on the same side of
                // the globe
                if (point1 != null && point2 != null
                        && !(p1Visible ^ p2Visible)) {
                    return Math.abs(point1.distanceTo3(point2));
                }
                // }
            }
        }

        return -1;
    }

    public static Vec4 fromPositionToScreen(Position pos, Globe globe, View view) {
        Vec4 vecPos;
        vecPos = globe.computePointFromPosition(pos);
        return view.project(vecPos);
    }

    public static Position fromScreenToPosition(Vec4 screen, Globe globe,
            View view) {
        // double yInGLCoords = viewport.height - y - 1;
        Vec4 vecPos = view.unProject(screen);
        return globe.computePositionFromPoint(vecPos);
    }

    public static Vec4 fromPositionTo3DCoords(Position pos, Globe globe) {
        Vec4 vecPos;
        vecPos = globe.computePointFromPosition(pos);
        return vecPos;
    }

    public static Position from3DCoordsToPosition(Vec4 pos, Globe globe) {
        return globe.computePositionFromPoint(pos);
    }

    public static Position from3DCoordsToPosition(double x, double y, double z,
            Globe globe) {
        Vec4 vecpos = new Vec4(x, y, z);
        return globe.computePositionFromPoint(vecpos);
    }

    public static Position fromScreenToPosition(double x, double y, double z,
            Globe globe, View view) {
        Vec4 vecpos = view.unProject(new Vec4(x, y, z));
        Position pos = globe.computePositionFromPoint(vecpos);
        return new Position(pos.getLatitude(), pos.getLongitude(), 0);
        // return globe.computePositionFromPoint(temp);
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
        synchronized (vis) {
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
                    // Prefuse sometimes throws this exception if the node /
                    // edge
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
                    if (edge.getControlPoints() != null) {
                        edge.setAlpha(fromIntervalToInterval(edge
                                .getControlPoints().size(), minlength,
                                maxlength, UIConstants.minAlpha,
                                UIConstants.maxAlpha));
                    }
                } catch (IllegalArgumentException exc) {
                    // same story here
                    System.err.println(exc.getMessage());
                }

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
        } else if (rgb1[0] < 0) {
            rgb1[0] = 0;
        }

        if (rgb1[1] > 1) {
            rgb1[1] = 1;
        } else if (rgb1[1] < 0) {
            rgb1[1] = 0;
        }

        if (rgb1[2] > 1) {
            rgb1[2] = 1;
        } else if (rgb1[2] < 0) {
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

    public static String extractLocationName(String name) {
        int index = name.lastIndexOf("@");

        if (index > 0) {
            if (GUI.fakeData) {
                return "cluster" + "@" + name.substring(index + 1);
            } else {
                return name.substring(index + 1);
            }
        }
        return name;
    }

    public static String extractIbisName(String ibisName) {
        String locationName = extractLocationName(ibisName);
        int index;
        if (GUI.fakeData) {
            index = ibisName.indexOf("-");
            if (index > 0) {
                return ibisName.substring(0, index) + "@" + locationName;
            }
        } else {
            index = ibisName.indexOf("@");
            if (index > 0) {
                return ibisName.substring(0, index) + "@" + locationName;
            }
        }
        return ibisName;
    }

    public static void resetIndex() {
        currentIdx = 0;
    }

    public static LatLon generateLatLon(boolean useFakeData, String name) {
        double lat, lon;
        if (latLonsPerLocation.containsKey(name)) {
            return latLonsPerLocation.get(name);
        }
        if (useFakeData && Utils.currentIdx < longitudes.length) {
            lat = latitudes[currentIdx];
            lon = longitudes[currentIdx++];
        } else {
            Cluster c = getClusterByName(name);
            if (c != null) {

                lat = c.getLatitude();
                lon = c.getLongitude();
            } else {
                lat = (Math.random() * 1000) % 180 - 90;
                lon = (Math.random() * 1000) % 360 - 180;
            }
        }
        latLonsPerLocation.put(name, LatLon.fromDegrees(lat, lon));
        return latLonsPerLocation.get(name);
    }

    public static double generateLatitude(boolean useClusterData, String name) {
        System.out.println("sadaaas");
        if (useClusterData && Utils.currentIdx < latitudes.length) {
            System.out.println("here");
            return latitudes[Utils.currentIdx];
        } else {
            Cluster c = Utils.getClusterByName(name);
            if (c != null) {
                return c.getLatitude();
            }
        }
        return (Math.random() * 1000) % 180 - 90;
    }

    public static double generateLongitude(boolean useClusterData, String name) {
        if (useClusterData && Utils.currentIdx < longitudes.length) {
            return longitudes[Utils.currentIdx++];
        } else {
            Cluster c = Utils.getClusterByName(name);
            if (c != null) {
                return c.getLongitude();
            }
        }
        return (Math.random() * 1000) % 360 - 180;
    }

    static int currentIdx;

    static Cluster getClusterByName(String name) {
        if (name != null) {
            for (Cluster c : Utils.clusters) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }

        return null;
    }

    public static void setGUI(GUI guiRef) {
        Utils.gui = guiRef;
        Utils.clusters = Utils.gui.getGrid().getClusters();
        currentIdx = 0;
    }
    
    public static String extractFullNameFromIbisIdentifier(IbisIdentifier id){
        return id.name() + "-" + id.location();
    }
}

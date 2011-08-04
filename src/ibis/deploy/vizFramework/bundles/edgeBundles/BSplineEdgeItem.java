package ibis.deploy.vizFramework.bundles.edgeBundles;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import prefuse.data.Tree;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.tuple.TableEdgeItem;

/**
 * @author Ana Vinatoru
 *
 */
public class BSplineEdgeItem extends TableEdgeItem {
    public BSplineEdgeItem() {
        super();
    }

    private ArrayList<Point2D.Double> controlPoints;

    private float alpha = 1;

    private boolean selected = false;

    private boolean edgeUpdated = false;
    
    public int[] xPoints;
    public int[] yPoints;

    public void setControlPoints(ArrayList<Point2D.Double> list) {
        controlPoints = list;
    }

    public ArrayList<Point2D.Double> getControlPoints() {
        return controlPoints;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float newAlpha) {
        this.alpha = newAlpha;
    }

    public boolean isUpdated() {
        return edgeUpdated;
    }

    public void setUpdated(boolean init) {
        edgeUpdated = init;
    }
    
    public void computeControlPoints(boolean removeSharedAncestor,
            double bundling, EdgeItem edge, Tree tree) {
    NodeItem source, target, parent1 = null, parent2 = null;

    Point2D.Double temp;

    ArrayList<Point2D.Double> p1 = new ArrayList<Point2D.Double>();
    ArrayList<Point2D.Double> p2 = new ArrayList<Point2D.Double>();
    ArrayList<Point2D.Double> p = new ArrayList<Point2D.Double>();

    int d1, d2, i, N;
    double ux, uy, dx, dy;

    source = edge.getSourceItem();
    target = edge.getTargetItem();

    d1 = tree.getDepth(source.getRow());
    parent1 = source;

    d2 = tree.getDepth(target.getRow());
    parent2 = target;

    // add the source and target nodes three times in the list to force the
    // edge to go through them
    p1.add(new Point2D.Double(source.getX(), source.getY()));
    p1.add(new Point2D.Double(source.getX(), source.getY()));
    p1.add(new Point2D.Double(source.getX(), source.getY()));

    p2.add(new Point2D.Double(target.getX(), target.getY()));
    p2.add(new Point2D.Double(target.getX(), target.getY()));
    p2.add(new Point2D.Double(target.getX(), target.getY()));

    while (d1 > d2) {
            parent1 = (NodeItem) tree.getParent(source);
            d1--;
            p1.add(new Point2D.Double(parent1.getX(), parent1.getY()));
            source = parent1;
    }

    while (d2 > d1) {
            parent2 = (NodeItem) tree.getParent(target);
            d2--;
            p2.add(new Point2D.Double(parent2.getX(), parent2.getY()));
            target = parent2;
    }

    while (parent1 != parent2) {
            parent1 = (NodeItem) tree.getParent(parent1);
            parent2 = (NodeItem) tree.getParent(parent2);

            p1.add(new Point2D.Double(parent1.getX(), parent1.getY()));
            p2.add(new Point2D.Double(parent2.getX(), parent2.getY()));
    }

    d1 = p1.size();
    d2 = p2.size();

    if (d1 + d2 == 4 && d1 > 1 && d2 > 1) { // shared parent
            p.add(p1.get(1));
    } else {
            int offset = removeSharedAncestor ? 1 : 0;
            for (i = 0; i < p1.size() - offset; ++i) {
                    p.add(p1.get(i));
            }
            for (i = p2.size() - 1; --i >= 0;) {
                    p.add(p2.get(i));
            }

            double b = bundling, ib = 1 - bundling;
            N = p.size();

            if (b < 1) {
                    NodeItem o = edge.getSourceItem();
                    ux = o.getX();
                    uy = o.getY();

                    o = edge.getTargetItem();
                    dx = o.getX();
                    dy = o.getY();

                    dx = (dx - ux) / (N + 2);
                    dy = (dy - uy) / (N + 2);

                    // adjust the control points, with the exception of the first
                    // three and last three, which are just copies of the start and
                    // stop nodes and shouldn't be moved
                    for (i = 3; i < N - 3; i++) {
                            temp = p.get(i);
                            temp.setLocation(
                                            b * temp.getX() + ib * (ux + (i + 2) * dx), b
                                                            * temp.getY() + ib * (uy + (i + 2) * dy));
                            p.set(i, temp);
                    }
            }
    }

    ((BSplineEdgeItem) edge).setControlPoints(p);
    computePolyline();
}

private void computePolyline(){
    
    int nSteps = 10, index, ncurves;
    double xA, yA, xB, yB, xC, yC, xD, yD;
    double a0, a1, a2, a3, b0, b1, b2, b3;
    double x = 0, y = 0, t;

    
    xPoints = new int[nSteps + 1];
    yPoints = new int[nSteps + 1];
    
    ncurves = controlPoints.size() - 3;

    xPoints = new int[ncurves * (nSteps + 1)];
    yPoints = new int[ncurves * (nSteps + 1)];

    index = 0;

    for (int i = 1; i < controlPoints.size() - 2; i++) {

            xA = controlPoints.get(i - 1).getX();
            yA = controlPoints.get(i - 1).getY();

            xB = controlPoints.get(i).getX();
            yB = controlPoints.get(i).getY();

            xC = controlPoints.get(i + 1).getX();
            yC = controlPoints.get(i + 1).getY();

            xD = controlPoints.get(i + 2).getX();
            yD = controlPoints.get(i + 2).getY();

            /*
             * Apply this matrix to the three points and obtain line-matrix: |-1
             * 3 -3 1| | 3 -6 0 3| | -3 0 3 0| | 1 4 1 0| * 1/6
             */

            a3 = (-xA + 3 * (xB - xC) + xD) / 6;
            b3 = (-yA + 3 * (yB - yC) + yD) / 6;

            a2 = (xA - 2 * xB + xC) / 2;
            b2 = (yA - 2 * yB + yC) / 2;

            a1 = (-xA + xC) / 2;
            b1 = (-yA + yC) / 2;

            a0 = (xA + 4 * xB + xC) / 6;
            b0 = (yA + 4 * yB + yC) / 6;

            xPoints[index] = (int) a0;
            yPoints[index++] = (int) b0; 

            t = 0;

            for (int j = 1; j <= nSteps; j++) {

                    t = (double) j / (double) nSteps;

                    x = (a3 * t * t + a2 * t + a1) * t + a0;
                    y = (b3 * t * t + b2 * t + b1) * t + b0;

                    xPoints[index] = (int) x;
                    yPoints[index++] = (int) y;
            }

    }
}

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean sel) {
        selected = sel;
    }
}

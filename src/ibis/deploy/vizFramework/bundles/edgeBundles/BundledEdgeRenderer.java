package ibis.deploy.vizFramework.bundles.edgeBundles;

import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import prefuse.Constants;
import prefuse.data.Tree;
import prefuse.render.EdgeRenderer;
import prefuse.util.GraphicsLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

public class BundledEdgeRenderer extends EdgeRenderer {
    private Tree tree;
    private double bfactor;
    private boolean removeSharedAncestor;

    private Color startColor, stopColor;

    private boolean colorEncodingWeight = true;

    private BasicStroke basicStroke = new BasicStroke(2);

    public BundledEdgeRenderer(int edgeType) {
        super(edgeType);
        bfactor = UIConstants.INITIAL_BUNDLING_FACTOR;
        removeSharedAncestor = false;
        startColor = UIConstants.DEFAULT_START_COLOR;
        stopColor = UIConstants.DEFAULT_STOP_COLOR;
    }

    public void setSpanningTree(Tree tree) {
        this.tree = tree;
    }

    // the parameter is true if color encoding is used to show weight and false
    // if it's used to display sender / receiver
    public void setColorEncoding(boolean ecodingWeight) {
        colorEncodingWeight = ecodingWeight;
    }

    public void setBundlingFactor(double bundling) {
        bfactor = bundling;
    }

    public void setRemoveSharedAncestor(boolean removeSA) {
        removeSharedAncestor = removeSA;
    }

    public void setStartColor(Color newColor) {
        startColor = newColor;
    }

    public Color getStartColor() {
        return startColor;
    }

    public void setStopColor(Color newColor) {
        stopColor = newColor;
    }

    public Color getStopColor() {
        return stopColor;
    }

    @Override
    public void render(Graphics2D g, VisualItem item) {
        if (tree == null) {
            throw new RuntimeException(
                    "The spanning tree needs to be initialized!");
        }

        if (m_edgeType == UIConstants.BSPLINE_EDGE_TYPE) {
            BSplineEdgeItem edge = (BSplineEdgeItem) item;
            if (!edge.isUpdated()) {
                edge.computeControlPoints(removeSharedAncestor, bfactor,
                        (EdgeItem) item, tree);
                edge.setUpdated(true);
            }
            drawCubicBSpline(g, (EdgeItem) item);
        } else {
            Shape shape = getShape(item);
            if (shape != null) {
                drawShape(g, item, shape);
            }
        }
    }

    // This is basically the overridden method from the default renderer, with a
    // special case added for the BSPLINE edge type
    @Override
    protected Shape getRawShape(VisualItem item) {
        EdgeItem edge = (EdgeItem) item;
        VisualItem item1 = edge.getSourceItem();
        VisualItem item2 = edge.getTargetItem();

        int type = m_edgeType;

        getAlignedPoint(m_tmpPoints[0], item1.getBounds(), m_xAlign1, m_yAlign1);
        getAlignedPoint(m_tmpPoints[1], item2.getBounds(), m_xAlign2, m_yAlign2);
        m_curWidth = (float) (m_width * getLineWidth(item));

        // create the arrow head, if needed
        EdgeItem e = (EdgeItem) item;
        if (e.isDirected() && m_edgeArrow != Constants.EDGE_ARROW_NONE) {
            // get starting and ending edge endpoints
            boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
            Point2D start = null, end = null;
            start = m_tmpPoints[forward ? 0 : 1];
            end = m_tmpPoints[forward ? 1 : 0];

            // compute the intersection with the target bounding box
            VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
            int i = GraphicsLib.intersectLineRectangle(start, end, dest
                    .getBounds(), m_isctPoints);
            if (i > 0)
                end = m_isctPoints[0];

            // create the arrow head shape
            AffineTransform at = getArrowTrans(start, end, m_curWidth);
            m_curArrow = at.createTransformedShape(m_arrowHead);

            // update the endpoints for the edge shape
            // need to bias this by arrow head size
            Point2D lineEnd = m_tmpPoints[forward ? 1 : 0];
            lineEnd.setLocation(0, -m_arrowHeight);
            at.transform(lineEnd, lineEnd);
        } else {
            m_curArrow = null;
        }

        // create the edge shape
        Shape shape = null;
        double n1x = m_tmpPoints[0].getX();
        double n1y = m_tmpPoints[0].getY();
        double n2x = m_tmpPoints[1].getX();
        double n2y = m_tmpPoints[1].getY();
        switch (type) {
        case Constants.EDGE_TYPE_LINE:
            m_line.setLine(n1x, n1y, n2x, n2y);
            shape = m_line;
            break;
        case Constants.EDGE_TYPE_CURVE:
            getCurveControlPoints(edge, m_ctrlPoints, n1x, n1y, n2x, n2y);
            m_cubic.setCurve(n1x, n1y, m_ctrlPoints[0].getX(), m_ctrlPoints[0]
                    .getY(), m_ctrlPoints[1].getX(), m_ctrlPoints[1].getY(),
                    n2x, n2y);
            shape = m_cubic;
            break;
        case UIConstants.BSPLINE_EDGE_TYPE:
            // this is an approximation, it works for the moment
            // see if you can use a different type of curve here at some point
            // TODO
            getCurveControlPoints(edge, m_ctrlPoints, n1x, n1y, n2x, n2y);
            m_cubic.setCurve(n1x, n1y, m_ctrlPoints[0].getX(), m_ctrlPoints[0]
                    .getY(), m_ctrlPoints[1].getX(), m_ctrlPoints[1].getY(),
                    n2x, n2y);
            shape = m_cubic;
            break;
        default:
            throw new IllegalStateException("Unknown edge type");
        }

        // return the edge shape
        return shape;
    }

    // draw uniform cubic B-spline
    void drawCubicBSpline(Graphics g, EdgeItem item) {
        int nSteps = 10, ncurves;
        double ratio = 0;
        float alpha;
        Color color;

        BSplineEdgeItem bsplineEdge = (BSplineEdgeItem) item;

        ArrayList<Point2D.Double> controlPoints = bsplineEdge
                .getControlPoints();

        Graphics2D g2d = (Graphics2D) g;

        alpha = ((BSplineEdgeItem) item).getAlpha();

        g2d.setColor(new Color(0.5f, 0.5f, 0.5f, alpha));

        startColor = new Color(startColor.getRed(), startColor.getGreen(),
                startColor.getBlue(), (int) (alpha * 255));
        stopColor = new Color(stopColor.getRed(), stopColor.getGreen(),
                stopColor.getBlue(), (int) (alpha * 255));

        g2d.setStroke(basicStroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (!bsplineEdge.isSelected()) {
            // if the encoding is by weight, then the entire edge has the same
            // color
            if (colorEncodingWeight) {
                ratio = item.getLong(UIConstants.WEIGHT) * 1.0f / Utils.MAX_EDGE_WEIGHT;
                color = Utils.blend(startColor, stopColor, ratio,
                        ((BSplineEdgeItem) item).getAlpha());
                g.setColor(color);
            } else {
                // if the encoding is by start node - stop node, the edge is
                // colored with a gradient
                int SIZE = 400;
                Point2D start = new Point2D.Float(0, SIZE);
                Point2D end = new Point2D.Float(SIZE, 0);
                float[] fractions = { 0f, 1f };
                Color[] colors = { startColor, stopColor };

                Paint paint = new LinearGradientPaint(start, end, fractions,
                        colors);
                g2d.setPaint(paint);
            }
        } else {
            g.setColor(Color.blue); // selected edges are blue
        }

        ncurves = controlPoints.size() - 3;

        // draw the entire curve at once as a sequence of segments
        g2d.drawPolyline(((BSplineEdgeItem) item).xPoints,
                ((BSplineEdgeItem) item).yPoints, ncurves * (nSteps + 1));
    }

    /**
     * Sets the type of the drawn edge. It includes the prefuse edge types and
     * BSPLINE in addition
     * 
     * @param type
     *            the new edge type
     */
    @Override
    public void setEdgeType(int type) {
        if (type < 0
                || (type != UIConstants.BSPLINE_EDGE_TYPE && type >= Constants.EDGE_TYPE_COUNT))
            throw new IllegalArgumentException("Unrecognized edge curve type: "
                    + type);
        m_edgeType = type;
    }
}

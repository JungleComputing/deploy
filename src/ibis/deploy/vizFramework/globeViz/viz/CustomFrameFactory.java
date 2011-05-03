package ibis.deploy.vizFramework.globeViz.viz;

import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.AbstractAnnotation;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLUtil;

public class CustomFrameFactory extends FrameFactory {

    private static int circleSteps = 64;

    public static void drawCustomBuffer(DrawContext dc, int mode,
            DoubleBuffer verts, FloatBuffer colorBuffer,
            ArrayList<Color> clusterColors) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (verts == null) {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int count = verts.remaining() / 2;

        GL2 gl = dc.getGL().getGL2();
        // Set up
        // gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        // gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //
        // gl.glVertexPointer(2, GL2.GL_DOUBLE, 0, verts);
        //
        // //TODO - pentru colorare custom se activeaza asta, dar daca e activat
        // nu mai e activat picking
        // gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        // if (colorBuffer != null) {
        // gl.glColorPointer(4, GL2.GL_FLOAT, 0, colorBuffer);
        // }
        // // Draw
        // gl.glDrawArrays(mode, 0, count);
        //
        // gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        //
        // //gl.glColor4f(0.5f, 0.5f, 1, 0.7f);
        //
        // // Restore
        // gl.glPopClientAttrib();

        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        // gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        // gl.glVertexPointer(2, GL2.GL_DOUBLE, 0, verts);
        // gl.glTranslated(-UIConstants.LOCATION_CIRCLE_SIZE / 2,
        // -UIConstants.LOCATION_CIRCLE_SIZE / 2, 0);

        double cx = UIConstants.LOCATION_CIRCLE_SIZE / 2;
        double cy = UIConstants.LOCATION_CIRCLE_SIZE / 2;

        double currentAngle = 0;
        double step = 2 * Math.PI / clusterColors.size();

        for (int i = 0; i < clusterColors.size(); i++) {
            float[] color = new float[4];
            if (!dc.isPickingMode()) {
                //TODO - restore colors
                clusterColors.get(i).getColorComponents(color);
                color[3] = 0.5f;
                gl.glColor4fv(color, 0);
                
                //gl.glColor3f(0, 0, 0);
            }

            for (double angle = currentAngle; angle < currentAngle + step; angle += UIConstants.increment) {
                gl.glBegin(GL2.GL_POLYGON);

                // One vertex of each triangle is at center of circle
                gl.glVertex2d(cx, cy);
                // Other two vertices form the periphery of the circle
                gl.glVertex2d(cx + Math.cos(angle)
                        * UIConstants.LOCATION_CIRCLE_SIZE,
                        cy + Math.sin(angle) * UIConstants.LOCATION_CIRCLE_SIZE);
                gl.glVertex2d(cx + Math.cos(angle + UIConstants.increment)
                        * UIConstants.LOCATION_CIRCLE_SIZE,
                        cy + Math.sin(angle + UIConstants.increment)
                                * UIConstants.LOCATION_CIRCLE_SIZE);
                gl.glEnd();
            }

            currentAngle += step;
        }

        //drawCircle(cx, cy, UIConstants.LOCATION_CIRCLE_SIZE, 50, gl);
        
        //drawArc(cx, cy, UIConstants.LOCATION_CIRCLE_SIZE, 0, 2* Math.PI, 50, gl);
        
        // if(!dc.isPickingMode()){
        // gl.glColor4f(0.5f, 0.5f, 1, 0.8f);
        // }
        //
        // // Draw
        // for (double angle = 0; angle < Math.PI; angle +=
        // UIConstants.increment) {
        // gl.glBegin(gl.GL_POLYGON);
        //
        // // One vertex of each triangle is at center of circle
        // gl.glVertex2d(cx, cy);
        // // Other two vertices form the periphery of the circle
        // gl.glVertex2d(cx + Math.cos(angle)
        // * UIConstants.LOCATION_CIRCLE_SIZE, cy + Math.sin(angle)
        // * UIConstants.LOCATION_CIRCLE_SIZE);
        // gl.glVertex2d(cx + Math.cos(angle + UIConstants.increment)
        // * UIConstants.LOCATION_CIRCLE_SIZE,
        // cy + Math.sin(angle + UIConstants.increment)
        // * UIConstants.LOCATION_CIRCLE_SIZE);
        // gl.glEnd();
        // }
        //
        // if(!dc.isPickingMode()){
        // gl.glColor4f(1f, 0.5f, 0.5f, 0.8f);
        // }
        //
        // // Draw second half
        // for (double angle = Math.PI; angle < 2*Math.PI; angle +=
        // UIConstants.increment) {
        // gl.glBegin(gl.GL_POLYGON);
        //
        // // One vertex of each triangle is at center of circle
        // gl.glVertex2d(cx, cy);
        // // Other two vertices form the periphery of the circle
        // gl.glVertex2d(cx + Math.cos(angle)
        // * UIConstants.LOCATION_CIRCLE_SIZE, cy + Math.sin(angle)
        // * UIConstants.LOCATION_CIRCLE_SIZE);
        // gl.glVertex2d(cx + Math.cos(angle + UIConstants.increment)
        // * UIConstants.LOCATION_CIRCLE_SIZE,
        // cy + Math.sin(angle + UIConstants.increment)
        // * UIConstants.LOCATION_CIRCLE_SIZE);
        // gl.glEnd();
        // }
        // gl.glDrawArrays(mode, 0, count);
        // Restore

        gl.glPopClientAttrib();
    }

    private static void drawCircle(double cx, double cy, double r,
            int num_segments, GL2 gl) {
        gl.glColor4f(0, 0, 0, 1);

        gl.glBegin(GL2.GL_LINE_LOOP);
        for (int ii = 0; ii < num_segments; ii++) {
            double theta = 2.0f * Math.PI * ii / num_segments;// get the current
                                                              // angle

            double x = r * Math.cos(theta);// calculate the x component
            double y = r * Math.cos(theta);// calculate the y component

            gl.glVertex2f((float) (x + cx), (float) (y + cy));// output vertex

        }
        gl.glEnd();
    }

    private static void drawArc(double cx, double cy, double r,
            double start_angle, double arc_angle, int num_segments, GL2 gl) {
        double theta = arc_angle / (num_segments - 1);// theta is now calculated
                                                      // from the arc angle
                                                      // instead, the - 1 bit
                                                      // comes from the fact
                                                      // that the arc is open

        double tangetial_factor = Math.tan(theta);

        double radial_factor = Math.cos(theta);

        double x = r * Math.cos(start_angle);// we now start at the start angle
        double y = r * Math.sin(start_angle);

        gl.glColor4f(0, 0, 0, 1);

        gl.glBegin(GL2.GL_LINE_LOOP);// since the arc is not a closed curve,
                                       // this is a strip now
        gl.glVertex2f((float) cx, (float) cy);
        for (int ii = 0; ii < num_segments; ii++) {
            gl.glVertex2f((float) (x + cx), (float) (y + cy));

            double tx = -y;
            double ty = x;

            x += tx * tangetial_factor;
            y += ty * tangetial_factor;

            x *= radial_factor;
            y *= radial_factor;
        }
        gl.glEnd();
    }

    public static DoubleBuffer createShapeBuffer(String shape, double width,
            double height, int cornerRadius, DoubleBuffer buffer) {
        if (shape.equals(AVKey.SHAPE_ELLIPSE)) {
            // colorBuffer = allocateColorBuffer(circleSteps, colorBuffer);
            return createEllipseBuffer(width, height, circleSteps, buffer);
        }

        return null;
    }

    public static FloatBuffer createColorBuffer(FloatBuffer colorBuffer) {
        colorBuffer = allocateColorBuffer(circleSteps + 1, colorBuffer);

        int colorIndex = 0;
        for (int i = 0; i <= circleSteps; i++) {
            // if (i < circleSteps / 2) {
            colorBuffer.put(colorIndex++, 1);
            colorBuffer.put(colorIndex++, 0.5f);
            colorBuffer.put(colorIndex++, 0.5f);
            colorBuffer.put(colorIndex++, 0.5f);
            // } else {
            // colorBuffer.put(colorIndex++, 0);
            // colorBuffer.put(colorIndex++, 1);
            // colorBuffer.put(colorIndex++, 0);
            // colorBuffer.put(colorIndex++, 1);
            // }
        }
        colorBuffer.limit(colorIndex);
        return colorBuffer;
    }

    private static DoubleBuffer createEllipseBuffer(double width,
            double height, int steps, DoubleBuffer buffer) {
        int numVertices = steps + 1;
        buffer = allocateVertexBuffer(numVertices, buffer);

        // Drawing counter clockwise from bottom-left
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfPI = Math.PI / 2;
        double x0 = halfWidth;
        double y0 = halfHeight;
        double step = Math.PI * 2 / steps;

        int idx = 0;

        for (int i = 0; i <= steps; i++) {
            double a = step * i - halfPI;
            double x = x0 + Math.cos(a) * halfWidth;
            double y = y0 + Math.sin(a) * halfHeight;
            buffer.put(idx++, x);
            buffer.put(idx++, y);
        }

        buffer.limit(idx);
        return buffer;
    }

    private static DoubleBuffer allocateVertexBuffer(int numVertices,
            DoubleBuffer buffer) {
        int numCoords = 2 * numVertices;

        if (buffer != null)
            buffer.clear();

        if (buffer == null || buffer.capacity() < numCoords)
            buffer = Buffers.newDirectDoubleBuffer(numCoords);

        return buffer;
    }

    private static FloatBuffer allocateColorBuffer(int numColorsPar,
            FloatBuffer buffer) {
        int numColors = 4 * numColorsPar;

        if (buffer != null)
            buffer.clear();

        if (buffer == null || buffer.capacity() < numColors)
            buffer = Buffers.newDirectFloatBuffer(numColors);

        return buffer;
    }
}

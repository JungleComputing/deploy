package ibis.deploy.vizFramework.globeViz.viz;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.util.Logging;

public class CustomFrameFactory extends FrameFactory {

    private static int circleSteps = 64;
    private static FloatBuffer colorBuffer = null;

    /**
     * Draw a vertex buffer in a given gl mode. Vertex buffers coming from the
     * createShapeBuffer() methods support both <code>GL2.GL_TRIANGLE_FAN</code>
     * and <code>GL2.LINE_STRIP</code>.
     * 
     * @param dc
     *            the current DrawContext.
     * @param mode
     *            the desired drawing GL mode.
     * @param count
     *            the number of vertices to draw.
     * @param verts
     *            the vertex buffer to draw.
     */
    public static void drawCustomBuffer(DrawContext dc, int mode, int count,
            DoubleBuffer verts) {
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

        GL2 gl = dc.getGL().getGL2();
        // Set up
        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL2.GL_DOUBLE, 0, verts);
        if (colorBuffer != null) {
            gl.glColorPointer(4, GL2.GL_FLOAT, 0, colorBuffer);
        }
        // Draw
        gl.glDrawArrays(mode, 0, count);
        // Restore
        gl.glPopClientAttrib();
    }

    public static DoubleBuffer createShapeBuffer(String shape, double width,
            double height, int cornerRadius, DoubleBuffer buffer) {
        if (shape.equals(AVKey.SHAPE_ELLIPSE)) {
            colorBuffer = allocateColorBuffer(circleSteps, colorBuffer);
            return createEllipseBuffer(width, height, circleSteps, buffer,
                    colorBuffer);
        }

        return null;
    }

    private static DoubleBuffer createEllipseBuffer(double width,
            double height, int steps, DoubleBuffer buffer,
            FloatBuffer colorBuffer) {
        int numVertices = steps + 1;
        buffer = allocateVertexBuffer(numVertices, buffer);
        colorBuffer = allocateColorBuffer(numVertices, colorBuffer);

        // Drawing counter clockwise from bottom-left
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfPI = Math.PI / 2;
        double x0 = halfWidth;
        double y0 = halfHeight;
        double step = Math.PI * 2 / steps;

        int idx = 0;
        int colorIndex = 0;
        for (int i = 0; i <= steps; i++) {
            double a = step * i - halfPI;
            double x = x0 + Math.cos(a) * halfWidth;
            double y = y0 + Math.sin(a) * halfHeight;
            buffer.put(idx++, x);
            buffer.put(idx++, y);

            if (i < steps / 2) {
                colorBuffer.put(colorIndex++, 1);
                colorBuffer.put(colorIndex++, 0);
                colorBuffer.put(colorIndex++, 0);
                colorBuffer.put(colorIndex++, 0.8f);
            } else {
                colorBuffer.put(colorIndex++, 0);
                colorBuffer.put(colorIndex++, 1);
                colorBuffer.put(colorIndex++, 0);
                colorBuffer.put(colorIndex++, 0.8f);
            }
        }

        buffer.limit(idx);
        colorBuffer.limit(colorIndex);
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

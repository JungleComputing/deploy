package ibis.deploy.vizFramework.globeViz.viz;

import java.awt.Color;
import java.util.ArrayList;

import javax.media.opengl.GL2;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;

public class UnclippablePolyline extends Polyline {

    public UnclippablePolyline() {
        super();
    }

    public UnclippablePolyline(Iterable<? extends Position> positions) {
        super(positions);
    }

    public UnclippablePolyline(Iterable<? extends LatLon> positions,
            double elevation) {
        super(positions, elevation);
    }
 
    protected void drawOrderedRenderable(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2();

        int attrBits = GL2.GL_HINT_BIT | GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT;
        if (!dc.isPickingMode())
        {
            if (this.color.getAlpha() != 255)
                attrBits |= GL2.GL_COLOR_BUFFER_BIT;
        }

        gl.glPushAttrib(attrBits);
        dc.getView().pushReferenceCenter(dc, this.referenceCenterPoint);

        boolean projectionOffsetPushed = false; // keep track for error recovery

        try
        {
            
            if (!dc.isPickingMode())
            {
                if (this.color.getAlpha() != 255)
                {
                    gl.glEnable(GL2.GL_BLEND);
                    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
                }
                dc.getGL().getGL2().glColor4ub((byte) this.color.getRed(), (byte) this.color.getGreen(),
                    (byte) this.color.getBlue(), (byte) this.color.getAlpha());
            }
            else
            {
                // We cannot depend on the layer to set a pick color for us because this Polyline is picked during ordered
                // rendering. Therefore we set the pick color ourselves.
                Color pickColor = dc.getUniquePickColor();
                Object userObject = this.getDelegateOwner() != null ? this.getDelegateOwner() : this;
                this.pickSupport.addPickableObject(pickColor.getRGB(), userObject, null);
                gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
            }

            if (this.stippleFactor > 0)
            {
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                gl.glLineStipple(this.stippleFactor, this.stipplePattern);
            }
            else
            {
                gl.glDisable(GL2.GL_LINE_STIPPLE);
            }

            int hintAttr = GL2.GL_LINE_SMOOTH_HINT;
            if (this.filled)
                hintAttr = GL2.GL_POLYGON_SMOOTH_HINT;
            gl.glHint(hintAttr, this.antiAliasHint);

            int primType = GL2.GL_LINE_STRIP;
            if (this.filled)
                primType = GL2.GL_POLYGON;

            if (dc.isPickingMode())
                gl.glLineWidth((float) this.lineWidth + 8);
            else
                gl.glLineWidth((float) this.lineWidth);

            if (this.followTerrain)
            {
                dc.pushProjectionOffest(0.99);
                projectionOffsetPushed = true;
            }

            if (this.currentSpans == null)
                return;

            // modified the far clipping distance here
            
            View view = dc.getView();
            Matrix projection = Matrix.fromPerspective(view.getFieldOfView(),
                            view.getViewport().getWidth(), view.getViewport ().getHeight(),
                            view.getNearClipDistance(), view.getFarClipDistance());
//                            view.getNearClipDistance(), 1.08*view.getFarClipDistance());
            double[] matrixArray = new double[16];
            projection.toArray(matrixArray, 0, false);
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadMatrixd(matrixArray, 0);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            
            
            
            
            for (ArrayList<Vec4> span : this.currentSpans)
            {
                if (span == null)
                    continue;

                // Since segements can very often be very short -- two vertices -- use explicit rendering. The
                // overhead of batched rendering, e.g., gl.glDrawArrays, is too high because it requires copying
                // the vertices into a DoubleBuffer, and DoubleBuffer creation and access performs relatively poorly.
                gl.glBegin(primType);
                for (Vec4 p : span)
                {
                    gl.glVertex3d(p.x, p.y, p.z);
                }
                gl.glEnd();
            }

            if (this.highlighted)
            {
                if (!dc.isPickingMode())
                {
                    if (this.highlightColor.getAlpha() != 255)
                    {
                        gl.glEnable(GL2.GL_BLEND);
                        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
                    }
                    dc.getGL().getGL2().glColor4ub((byte) this.highlightColor.getRed(), (byte) this.highlightColor.getGreen(),
                        (byte) this.highlightColor.getBlue(), (byte) this.highlightColor.getAlpha());

                    gl.glLineWidth((float) this.lineWidth + 2);
                    for (ArrayList<Vec4> span : this.currentSpans)
                    {
                        if (span == null)
                            continue;

                        gl.glBegin(primType);
                        for (Vec4 p : span)
                        {
                            gl.glVertex3d(p.x, p.y, p.z);
                        }
                        gl.glEnd();
                    }
                }
            }
        }
        finally
        {
            if (projectionOffsetPushed)
                dc.popProjectionOffest();

            gl.glPopAttrib();
            dc.getView().popReferenceCenter(dc);
        }
    }
}

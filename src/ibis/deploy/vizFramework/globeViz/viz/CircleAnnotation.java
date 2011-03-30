package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;

import java.nio.DoubleBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;


//TODO - maybe adjust circle size according to location size
public class CircleAnnotation extends GlobeAnnotation {
	private String locationName;

	public CircleAnnotation(Position position, AnnotationAttributes defaults,
			String name) {
		super(name, position, defaults);
		locationName = name;
	}

	public void setLocationName(String name) {
		locationName = name;
	}

	public String getLocationName() {
		return locationName;
	}

	protected void applyScreenTransform(DrawContext dc, int x, int y,
			int width, int height, double scale) {
		double finalScale = scale * this.computeScale(dc);

		GL2 gl = dc.getGL().getGL2();
		gl.glTranslated(x, y, 0);
		gl.glScaled(finalScale, finalScale, 1);
		
//		gl.glTranslated(x, y, 0);
//		gl.glScaled(finalScale, finalScale, 1);
	}

	// Override annotation drawing for a simple circle
	private DoubleBuffer shapeBuffer;

	protected void doDraw(DrawContext dc, int width, int height,
			double opacity, Position pickPosition) {
		// Draw colored circle around screen point - use annotation's text color
		if (dc.isPickingMode()) {
			this.bindPickableObject(dc, pickPosition);
		}

		this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity,
				true);

		// Draw 32x32 shape from its bottom left corner
		int size = 25;
		if (this.shapeBuffer == null)
			this.shapeBuffer = FrameFactory.createShapeBuffer(
					FrameFactory.SHAPE_ELLIPSE, size, size, 0, null);
		
		dc.getGL().getGL2().glTranslated(-size / 2, -size / 2, 0);
		FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);
	}
}

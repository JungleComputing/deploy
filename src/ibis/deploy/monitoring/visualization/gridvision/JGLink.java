package ibis.deploy.monitoring.visualization.gridvision;

import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Metric;

public class JGLink extends JGVisualAbstract implements JGVisual {
	private JGVisual source, destination;

	public JGLink(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, JGVisual source,
			JGVisual destination, Link dataLink) {
		super(goggles, parent);

		this.source = source;
		this.destination = destination;

		//jv.registerVisual(dataLink, this);
		
		for (Metric dataMetric : dataLink.getMetrics()) {
			metrics.add(new JGLinkMetric(goggles, this, glu, dataMetric));
		}
	}
	
	public void init(GL2 gl) {
		for (JGVisual metric : metrics) {
			JGLinkMetric linkmetric = (JGLinkMetric) metric;
			linkmetric.init(gl);
		}
	}
	
	public void drawSolids(GL2 gl, int renderMode) {
		State srcState = source.getState();
		State dstState = destination.getState();
		
		if (srcState == State.COLLAPSED && dstState == State.COLLAPSED) {
			for (JGVisual metric : metrics)	{				
				metric.drawSolids(gl, renderMode);
			}	
		}				
	}
	
	public void drawTransparents(GL2 gl, int renderMode) {
		State srcState = source.getState();
		State dstState = destination.getState();
		
		if (srcState == State.COLLAPSED && dstState == State.COLLAPSED) {
			for (JGVisual metric : metrics)	{
				metric.drawTransparents(gl, renderMode);
			}	
		}
	}
	

	public void setCoordinates(float[] newCoords) {
		// Calculate the angles we need to turn towards the destination
		float[] origin = source.getCoordinates();
		float[] destination = this.destination.getCoordinates();
		int xSign = 1, ySign = 1, zSign = 1;

		float xDist = destination[0] - origin[0];
		if (xDist < 0)
			xSign = -1;
		xDist = Math.abs(xDist);

		float yDist = destination[1] - origin[1];
		if (yDist < 0)
			ySign = -1;
		yDist = Math.abs(yDist);

		float zDist = destination[2] - origin[2];
		if (zDist < 0)
			zSign = -1;
		zDist = Math.abs(zDist);

		// Calculate the length of this element : V( x^2 + y^2 + z^2 )
		float length = (float) Math.sqrt(Math.pow(xDist, 2)
				+ Math.pow(yDist, 2) + Math.pow(zDist, 2));

		float xzDist = (float) Math.sqrt(Math.pow(xDist, 2)
				+ Math.pow(zDist, 2));

		float yAngle = 0.0f;
		if (xSign < 0) {
			yAngle = 180.0f + (zSign * (float) Math.toDegrees(Math.atan(zDist
					/ xDist)));
		} else {
			yAngle = (-zSign * (float) Math.toDegrees(Math.atan(zDist / xDist)));
		}

		float zAngle = ySign
				* (float) Math.toDegrees(Math.atan(yDist / xzDist));

		// Calculate the midpoint of the link
		newCoords[0] = origin[0] + (0.5f * (destination[0] - origin[0]));
		newCoords[1] = origin[1] + (0.5f * (destination[1] - origin[1]));
		newCoords[2] = origin[2] + (0.5f * (destination[2] - origin[2]));
		coordinates = newCoords;

		// Set the object rotation, x is not used, we need an extra -90 on the
		// z-axis for alignment
		float[] newRotation = new float[3];
		newRotation[0] = 0.0f;
		newRotation[1] = yAngle;
		newRotation[2] = zAngle - 90.0f;

		setCityScape(metrics, metricSeparation, length, newRotation);		
	}

	private void setCityScape(List<JGVisual> children, float[] separation, float length, float[] newRotation) {
		int childCount = children.size();
		float maxWidth = maxWidth(children);

		// get the breakoff point for rows and columns
		int[] count = new int[3];
		count[0] = (int) Math.ceil(Math.sqrt(childCount));
		count[1] = 0;
		count[2] = (int) Math.floor(Math.sqrt(childCount));

		float[] shift = { 0, 0, 0 };
		shift[0] = maxWidth + separation[0];
		//separation[1] ignored
		shift[2] = maxWidth + separation[2];

		// Center the drawing around the coordinates
		float[] maxShift = { 0, 0, 0 };
		for (int i = 0; i < 3; i++) {
			maxShift[i] = (shift[i] * Math.max((count[i]-1),0) * 0.5f);
		}		
		float[] centeredCoordinates = FloatMatrixMath.sub(coordinates, maxShift);		
		
		//calculate my own new radius
		radius = FloatMatrixMath.max(maxShift);
		width = FloatMatrixMath.max(maxShift);
		height = maxHeight(children);

		// Propagate the movement to the children
		float[] childLocation = { 0, 0, 0 };
		int i = 0;
		float[] position = { 0, 0, 0 };
		for (JGVisual child : children) {
			// cascade the new location
			childLocation = FloatMatrixMath.add(centeredCoordinates, FloatMatrixMath.mul(shift, position));

			child.setCoordinates(childLocation);

			// reduce the length by the radii of the origin and destination
			// objects
			JGLinkMetric linkMetric = (JGLinkMetric) child;
			float[] newDimensions = linkMetric.getDimensions();
			newDimensions[1] = length
					- (source.getRadius() + this.destination.getRadius());
			linkMetric.setDimensions(newDimensions);

			// and set to correct rotation
			linkMetric.setRotation(newRotation);
			
			//Calculate next position
			i++;			
			position[0] = i % count[0];

			// Move to next row (if applicable)
			if (i != 0 && position[0] == 0) {
				position[2]++;
			}
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (this.getClass() != other.getClass())
			return false;
		JGLink otherLink = (JGLink) other;

		if ((source == otherLink.source && destination == otherLink.destination)
				|| (source == otherLink.destination && destination == otherLink.source)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hashCode = source.hashCode() + destination.hashCode();
		return hashCode;
	}
}

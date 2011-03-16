package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

public abstract class JGVisualAbstract implements JGVisual {
	private final static float SPHERE_RADIUS_MULTIPLIER = 0.075f;

	protected List<JGVisual> locations;
	protected List<JGVisual> ibises;
	protected List<JGVisual> metrics;

	protected float[] coordinates;
	protected float[] rotation;

	protected CollectionShape cShape;
	protected FoldState foldState;
	protected MetricShape mShape;

	float radius;

	protected float[] locationSeparation = { 0, 0, 0 };
	protected float[] ibisSeparation = { 0, 0, 0 };
	protected float[] metricSeparation = { 0.05f, 0.05f, 0.05f };

	public JGVisualAbstract() {
		locations = new ArrayList<JGVisual>();
		ibises = new ArrayList<JGVisual>();
		metrics = new ArrayList<JGVisual>();

		coordinates = new float[3];
		coordinates[0] = 0.0f;
		coordinates[1] = 0.0f;
		coordinates[2] = 0.0f;
		
		rotation = new float[3];
		rotation[0] = 0.0f;
		rotation[1] = 0.0f;
		rotation[2] = 0.0f;
		cShape = CollectionShape.CITYSCAPE;
		foldState = FoldState.UNFOLDED;
		mShape = MetricShape.BAR;
	}

	public void init(GL2 gl) {
		for (JGVisual child : locations) {
			child.init(gl);
		}
		for (JGVisual ibis : ibises) {
			ibis.init(gl);
		}
		for (JGVisual metric : metrics) {
			metric.init(gl);
		}
	}

	public void setCoordinates(float[] newCoordinates) {
		coordinates[0] = newCoordinates[0];
		coordinates[1] = newCoordinates[1];
		coordinates[2] = newCoordinates[2];

		// First, give our location children a new home
		if (locations.size() > 0) {
			if (cShape == CollectionShape.CITYSCAPE) {
				setCityScape(locations, locationSeparation);
			} else if (cShape == CollectionShape.SPHERE) {
				setSphere(locations, locationSeparation);
			} else if (cShape == CollectionShape.CUBE) {
				setCube(locations, locationSeparation);
			} else {
				System.err
						.println("Collectionshape not defined while setting coordinates.");
				System.exit(0);
			}
		}

		if (ibises.size() > 0) {
			if (cShape == CollectionShape.CITYSCAPE) {
				setCityScape(ibises, ibisSeparation);
			} else if (cShape == CollectionShape.SPHERE) {
				setSphere(ibises, ibisSeparation);
			} else if (cShape == CollectionShape.CUBE) {
				setCube(ibises, ibisSeparation);
			} else {
				System.err
						.println("Collectionshape not defined while setting coordinates.");
				System.exit(0);
			}
		}

		if (metrics.size() > 0) {
			if (cShape == CollectionShape.CITYSCAPE) {
				setCityScape(metrics, metricSeparation);
			} else if (cShape == CollectionShape.SPHERE) {
				setSphere(metrics, metricSeparation);
			} else if (cShape == CollectionShape.CUBE) {
				setCube(metrics, metricSeparation);
			} else {
				System.err
						.println("Collectionshape not defined while setting coordinates.");
				System.exit(0);
			}
		}
	}

	private void setCityScape(List<JGVisual> children, float[] separation) {
		int childCount = children.size();
		float maxRad = maxRadius(children);

		// get the breakoff point for rows, stacks and columns
		int[] count = new int[3];
		count[0] = (int) Math.ceil(Math.sqrt(childCount));
		count[1] = 0;
		count[2] = (int) Math.floor(Math.sqrt(childCount));

		float[] shift = { 0, 0, 0 };
		for (int i = 0; i < 3; i++) {
			shift[i] = maxRad + separation[i];
		}

		// Center the drawing around the location
		float[] shiftedLocation = { 0, 0, 0 };
		for (int i = 0; i < 3; i++) {
			shiftedLocation[i] = coordinates[i]
					- ((shift[i] * count[i]) - separation[i]) * 0.5f;
		}

		// Propagate the movement to the children
		float[] childLocation = { 0, 0, 0 };
		int i = 0;
		int[] position = { 0, 0, 0 };
		for (JGVisual child : children) {
			position[0] = i % count[0];

			// Move to next row (if applicable)
			if (i != 0 && position[0] == 0) {
				position[2]++;
			}

			// cascade the new location
			for (int j = 0; j < 3; j++) {
				childLocation[j] = shiftedLocation[j] + shift[j] * position[j];
			}

			child.setCoordinates(childLocation);

			i++;
		}

		//radius = FloatMatrixMath.max(FloatMatrixMath.add(shiftedLocation, separation));
	}

	private void setSphere(List<JGVisual> children, float[] separation) {
		int childCount = children.size();
		float maxRad = maxRadius(children);

		double dlong = Math.PI * (3 - Math.sqrt(5));
		double olong = 0.0;
		double dz = 2.0 / childCount;
		double z = 1 - (dz / 2);
		float[][] pt = new float[childCount][3];
		double r = 0;
		float radius = SPHERE_RADIUS_MULTIPLIER * (maxRad + FloatMatrixMath.max(separation))
				* childCount;

		for (int k = 0; k < childCount; k++) {
			r = Math.sqrt(1 - (z * z));
			pt[k][0] = coordinates[0] + radius
					* ((float) (Math.cos(olong) * r));
			pt[k][1] = coordinates[1] + radius
					* ((float) (Math.sin(olong) * r));
			pt[k][2] = coordinates[2] + radius * ((float) z);
			z = z - dz;
			olong = olong + dlong;
		}

		int k = 0;
		for (JGVisual child : children) {
			// set the location
			child.setCoordinates(pt[k]);
			k++;
		}
		
		this.radius = radius;
	}

	private void setCube(List<JGVisual> children, float[] separation) {
		int childCount = children.size();
		float maxRad = maxRadius(children);

		// get the breakoff point for rows, stacks and columns
		int[] count = new int[3];
		count[0] = (int) Math.ceil(Math.pow(childCount, (1.0 / 3.0)));
		count[1] = (int) Math.ceil(Math.pow(childCount, (1.0 / 3.0)));
		count[2] = (int) Math.floor(Math.pow(childCount, (1.0 / 3.0)));

		float[] shift = { 0, 0, 0 };
		for (int i = 0; i < 3; i++) {
			shift[i] = maxRad + separation[i];
		}

		// Center the drawing around the location
		float[] shiftedLocation = { 0, 0, 0 };
		for (int i = 0; i < 3; i++) {
			shiftedLocation[i] = coordinates[i]
					- ((shift[i] * count[i]) - separation[i]) * 0.5f;
		}

		// Propagate the movement to the children
		float[] childLocation = { 0, 0, 0 };
		int[] position = { 0, 0, 0 };
		for (JGVisual child : children) {
			if (position[0] == count[0]) {
				position[0] = 0;
				position[2]++;
			}
			if (position[2] == count[2]) {
				position[2] = 0;
				position[1]++;
			}

			// cascade the new location
			for (int j = 0; j < 3; j++) {
				childLocation[j] = shiftedLocation[j] + shift[j] * position[j];
			}

			child.setCoordinates(childLocation);
		}
		
		radius = FloatMatrixMath.max(FloatMatrixMath.add(shiftedLocation, separation));
	}

	public void setRotation(float[] newRotation) {
		rotation[0] = newRotation[0];
		rotation[1] = newRotation[1];
		rotation[2] = newRotation[2];
	}

	public void setCollectionShape(CollectionShape newShape) {
		cShape = newShape;
	}

	public void setFoldState(FoldState newFoldState) {
		foldState = newFoldState;
	}

	public void setMetricShape(MetricShape newShape) {
		mShape = newShape;
		for (JGVisual metric : metrics) {
			metric.setMetricShape(newShape);
		}
	}

	public float[] getCoordinates() {
		float[] myCoordinates = new float[3];
		myCoordinates[0] = coordinates[0];
		myCoordinates[1] = coordinates[1];
		myCoordinates[2] = coordinates[2];

		return myCoordinates;
	}

	public float getRadius() {
		return radius;
	}

	public void update() {
		for (JGVisual child : locations) {
			child.update();
		}
		for (JGVisual ibis : ibises) {
			ibis.update();
		}
		for (JGVisual metric : metrics) {
			metric.update();
		}
	}

	public void drawThis(GL2 gl, int renderMode) {
		if (foldState == FoldState.UNFOLDED) {
			for (JGVisual ibis : locations) {
				ibis.drawThis(gl, renderMode);
			}
			for (JGVisual ibis : ibises) {
				ibis.drawThis(gl, renderMode);
			}
			for (JGVisual metric : metrics) {
				metric.drawThis(gl, renderMode);
			}
		} else {

		}
	}

	private float maxRadius(List<JGVisual> children) {
		float result = -Float.MAX_VALUE;
		for (JGVisual child : children) {
			float in = child.getRadius();
			if (in > result)
				result = in;
		}
		return result;
	}

}

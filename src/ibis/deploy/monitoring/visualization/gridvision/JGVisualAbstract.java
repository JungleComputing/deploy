package ibis.deploy.monitoring.visualization.gridvision;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.util.awt.TextRenderer;

public abstract class JGVisualAbstract implements JGVisual {
    private static final Logger logger = LoggerFactory
            .getLogger(JGLocation.class);
    private final static float SPHERE_RADIUS_MULTIPLIER = 0.175f;

    protected JungleGoggles goggles;
    protected JGVisual parent;

    protected List<JGVisual> locations;
    protected List<JGVisual> ibises;
    protected List<JGVisual> metrics;

    protected float[] coordinates;
    protected float[] rotation;

    protected CollectionShape locationColShape, ibisColShape, metricColShape;
    protected State state;
    protected MetricShape mShape;

    protected float radius, width, height;

    protected float[] locationSeparation = { 0, 0, 0 };
    protected float[] ibisSeparation = { 0.5f, 0.5f, 0.5f };
    protected float[] metricSeparation = { 0.05f, 0.05f, 0.05f };

    protected TextRenderer tr;
    protected String name;

    public JGVisualAbstract(JungleGoggles goggles, JGVisual parent) {
        this.goggles = goggles;
        this.parent = parent;

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

        locationColShape = CollectionShape.CUBE;
        ibisColShape = CollectionShape.SPHERE;
        metricColShape = CollectionShape.CITYSCAPE;
        state = State.UNFOLDED;

        radius = 0f;
        width = 0f;
        height = 0f;

        Font font = new Font("SansSerif", Font.BOLD, 36);
        tr = new TextRenderer(font);
        name = "";
    }

    @Override
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

    @Override
    public void setCoordinates(float[] newCoordinates) {
        coordinates[0] = newCoordinates[0];
        coordinates[1] = newCoordinates[1];
        coordinates[2] = newCoordinates[2];

        // First, give our location children a new home
        if (locations.size() > 0) {
            if (locationColShape == CollectionShape.CITYSCAPE) {
                setCityScape(locations, locationSeparation);
            } else if (locationColShape == CollectionShape.SPHERE) {
                setSphere(locations, locationSeparation);
            } else if (locationColShape == CollectionShape.CUBE) {
                setCube(locations, locationSeparation);
            } else {
                System.err
                        .println("Collectionshape not defined while setting coordinates.");
                System.exit(0);
            }
        }

        if (ibises.size() > 0) {
            if (ibisColShape == CollectionShape.CITYSCAPE) {
                setCityScape(ibises, ibisSeparation);
            } else if (ibisColShape == CollectionShape.SPHERE) {
                setSphere(ibises, ibisSeparation);
            } else if (ibisColShape == CollectionShape.CUBE) {
                setCube(ibises, ibisSeparation);
            } else {
                System.err
                        .println("Collectionshape not defined while setting coordinates.");
                System.exit(0);
            }
        }

        if (metrics.size() > 0) {
            // For now we will assume that all metric collections are in
            // cityscape format.

            if (metricColShape == CollectionShape.CITYSCAPE) {
                setCityScape(metrics, metricSeparation);
            } else if (metricColShape == CollectionShape.SPHERE) {
                setSphere(metrics, metricSeparation);
            } else if (metricColShape == CollectionShape.CUBE) {
                setCube(metrics, metricSeparation);
            } else {
                System.err
                        .println("Collectionshape not defined while setting coordinates.");
                System.exit(0);
            }
        }
    }

    protected void setCityScape(List<JGVisual> children, float[] separation) {
        int childCount = children.size();
        float maxWidth = maxWidth(children);

        // get the breakoff point for rows, stacks and columns
        int[] count = new int[3];
        count[0] = (int) Math.ceil(Math.sqrt(childCount));
        count[1] = 0;
        count[2] = (int) Math.floor(Math.sqrt(childCount));

        float[] shift = { 0, 0, 0 };
        shift[0] = maxWidth + separation[0];
        // separation[1] ignored
        shift[2] = maxWidth + separation[2];

        // Center the drawing around the coordinates
        float[] maxShift = { 0, 0, 0 };
        for (int i = 0; i < 3; i++) {
            maxShift[i] = (shift[i] * Math.max((count[i] - 1), 0) * 0.5f);
        }
        float[] centeredCoordinates = FloatMatrixMath
                .sub(coordinates, maxShift);

        // calculate my own new radius
        radius = FloatMatrixMath.max(maxShift);
        width = FloatMatrixMath.max(maxShift);
        height = maxHeight(children);

        // Propagate the movement to the children
        float[] childLocation = { 0, 0, 0 };
        int i = 0;
        float[] position = { 0, 0, 0 };
        for (JGVisual child : children) {
            // cascade the new location
            childLocation = FloatMatrixMath.add(centeredCoordinates,
                    FloatMatrixMath.mul(shift, position));

            child.setCoordinates(childLocation);

            // Calculate next position
            i++;
            position[0] = i % count[0];

            // Move to next row (if applicable)
            if (i != 0 && position[0] == 0) {
                position[2]++;
            }
        }
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

        float radius = SPHERE_RADIUS_MULTIPLIER
                * (maxRad + FloatMatrixMath.max(separation)) * childCount;

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
        float[] maxDims = { maxWidth(children), maxHeight(children),
                maxWidth(children), };

        // get the breakoff point for rows, stacks and columns
        int[] count = new int[3];
        count[0] = (int) Math.ceil(Math.pow(childCount, (1.0 / 3.0)));
        count[1] = (int) Math.floor(Math.pow(childCount, (1.0 / 3.0)));
        count[2] = (int) Math.ceil(Math.pow(childCount, (1.0 / 3.0)));

        float[] shift = FloatMatrixMath.add(maxDims, separation);

        // Center the drawing around the coordinates
        float[] maxShift = { 0, 0, 0 };
        for (int i = 0; i < 3; i++) {
            maxShift[i] = (shift[i] * Math.max((count[i] - 1), 0) * 0.5f);
        }
        float[] centeredCoordinates = FloatMatrixMath
                .sub(coordinates, maxShift);

        // calculate my own new radius
        radius = FloatMatrixMath.max(maxShift);
        width = Math.max(maxShift[0], maxShift[2]);
        height = maxShift[1];

        // Propagate the movement to the children
        float[] childLocation = { 0, 0, 0 };
        float[] position = { 0, 0, 0 };
        for (JGVisual child : children) {
            // cascade the new location
            childLocation = FloatMatrixMath.add(centeredCoordinates,
                    FloatMatrixMath.mul(shift, position));

            child.setCoordinates(childLocation);

            // Calculate next position
            position[0]++;

            if (position[0] == count[0]) {
                position[0] = 0;
                position[2]++;
            }
            if (position[2] == count[2]) {
                position[2] = 0;
                position[1]++;
            }
        }
    }

    @Override
    public void setRotation(float[] newRotation) {
        rotation[0] = newRotation[0];
        rotation[1] = newRotation[1];
        rotation[2] = newRotation[2];
    }

    @Override
    public void setLocationCollectionShape(CollectionShape newShape) {
        locationColShape = newShape;
        goggles.doRepositioning();
    }

    @Override
    public void setIbisCollectionShape(CollectionShape newShape) {
        ibisColShape = newShape;
        goggles.doRepositioning();
    }

    @Override
    public void setAllCollectionsShape(CollectionShape newShape) {
        locationColShape = newShape;
        ibisColShape = newShape;

        goggles.doRepositioning();
    }

    @Override
    public CollectionShape getCollectionShape() {
        return locationColShape;
    }

    @Override
    public void setMetricShape(MetricShape newShape) {
        mShape = newShape;

        for (JGVisual location : locations) {
            location.setMetricShape(newShape);
        }
        for (JGVisual ibis : ibises) {
            ibis.setMetricShape(newShape);
        }
        for (JGVisual metric : metrics) {
            metric.setMetricShape(newShape);
        }
    }

    @Override
    public float[] getCoordinates() {
        float[] myCoordinates = new float[3];
        myCoordinates[0] = coordinates[0];
        myCoordinates[1] = coordinates[1];
        myCoordinates[2] = coordinates[2];

        return myCoordinates;
    }

    @Override
    public float getRadius() {
        return radius;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
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

    @Override
    public JGVisual getParent() {
        return parent;
    }

    @Override
    public void setState(State newState) {
        // If collapsing while we are already collapsed, collapse our parent
        // instead.
        if (state == State.COLLAPSED && newState == State.COLLAPSED) {
            if (parent != null && !(parent instanceof JGUniverse)) { // If this
                // is not
                // the
                // root/universe
                parent.setState(State.COLLAPSED);
                newState = State.NOT_SHOWN;
            }
        } else if (newState == State.COLLAPSED) {
            for (JGVisual child : locations) {
                child.setState(State.NOT_SHOWN);
            }
            for (JGVisual child : ibises) {
                child.setState(State.NOT_SHOWN);
            }
        } else if (newState == State.UNFOLDED) {
            for (JGVisual child : locations) {
                child.setState(State.COLLAPSED);
            }
            for (JGVisual child : ibises) {
                child.setState(State.COLLAPSED);
            }
        } else if (newState == State.NOT_SHOWN) {
            for (JGVisual child : locations) {
                child.setState(State.NOT_SHOWN);
            }
            for (JGVisual child : ibises) {
                child.setState(State.NOT_SHOWN);
            }
        }

        state = newState;
        goggles.unselect();
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void drawSolids(GL2 gl, int renderMode) {
        if (this instanceof JGLocation || this instanceof JGUniverse) {
            if (state == State.UNFOLDED) {
                for (JGVisual location : locations) {
                    location.drawSolids(gl, renderMode);
                }
                for (JGVisual ibis : ibises) {
                    ibis.drawSolids(gl, renderMode);
                }
            } else if (state == State.COLLAPSED) {
                for (JGVisual metric : metrics) {
                    metric.drawSolids(gl, renderMode);
                }
            }
        } else {
            if (state == State.COLLAPSED || state == State.UNFOLDED) {
                for (JGVisual metric : metrics) {
                    metric.drawSolids(gl, renderMode);
                }
            }
        }
    }

    @Override
    public void drawTransparents(GL2 gl, int renderMode) {
        if (this instanceof JGLocation || this instanceof JGUniverse) {
            if (state == State.UNFOLDED) {
                for (JGVisual location : locations) {
                    location.drawTransparents(gl, renderMode);
                }
                for (JGVisual ibis : ibises) {
                    ibis.drawTransparents(gl, renderMode);
                }
            } else if (state == State.COLLAPSED) {
                for (JGVisual metric : metrics) {
                    metric.drawTransparents(gl, renderMode);
                }

                // Save the current modelview matrix
                gl.glPushMatrix();

                tr.begin3DRendering();
                if (goggles.currentlySelected(this)) {
                    tr.setColor(1f, 1f, 1f, 1f);
                } else {
                    tr.setColor(1f, 1f, 1f, 0.5f);
                }
                tr.draw3D(name, coordinates[0], coordinates[1] - height,
                        coordinates[2], 0.005f);
                tr.end3DRendering();

                // Restore the old modelview matrix
                gl.glPopMatrix();
            }
        } else {
            if (state == State.COLLAPSED || state == State.UNFOLDED) {
                for (JGVisual metric : metrics) {
                    metric.drawTransparents(gl, renderMode);
                }

                // Save the current modelview matrix
                gl.glPushMatrix();

                tr.begin3DRendering();
                if (goggles.currentlySelected(this)) {
                    tr.setColor(1f, 1f, 1f, 1f);
                } else {
                    tr.setColor(1f, 1f, 1f, 0.5f);
                }
                tr.draw3D(name, coordinates[0], coordinates[1] - height,
                        coordinates[2], 0.005f);
                tr.end3DRendering();

                // Restore the old modelview matrix
                gl.glPopMatrix();
            }
        }
    }

    @Override
    public void drawSelectionCube(GL2 gl) {
        if (!(this instanceof JGIbis || this instanceof JGLocation)) {
            return;
        }
        // Save the current modelview matrix
        gl.glPushMatrix();

        // Translate to the desired coordinates and rotate if desired
        gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
        gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
        gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
        gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);

        gl.glColor4f(0.1f, 0.1f, 0.1f, 0.1f);

        float HEIGHT = 1.5f, WIDTH = 1.5f;

        gl.glLineWidth(1.0f);

        float Xn = -0.5f * HEIGHT, Xp = 0.5f * WIDTH, Yn = -0.5f * HEIGHT, Yp = 0.5f * HEIGHT, Zn = -0.5f
                * WIDTH, Zp = 0.5f * WIDTH;

        gl.glBegin(GL2.GL_QUADS);
        // TOP
        gl.glVertex3f(Xn, Yp, Zn);
        gl.glVertex3f(Xn, Yp, Zp);
        gl.glVertex3f(Xp, Yp, Zp);
        gl.glVertex3f(Xp, Yp, Zn);

        gl.glVertex3f(Xn, Yn, Zn);
        gl.glVertex3f(Xp, Yn, Zn);
        gl.glVertex3f(Xp, Yn, Zp);
        gl.glVertex3f(Xn, Yn, Zp);

        // FRONT
        gl.glVertex3f(Xn, Yp, Zp);
        gl.glVertex3f(Xn, Yn, Zp);
        gl.glVertex3f(Xp, Yn, Zp);
        gl.glVertex3f(Xp, Yp, Zp);

        // BACK
        gl.glVertex3f(Xp, Yp, Zn);
        gl.glVertex3f(Xp, Yn, Zn);
        gl.glVertex3f(Xn, Yn, Zn);
        gl.glVertex3f(Xn, Yp, Zn);

        // LEFT
        gl.glVertex3f(Xn, Yp, Zn);
        gl.glVertex3f(Xn, Yn, Zn);
        gl.glVertex3f(Xn, Yn, Zp);
        gl.glVertex3f(Xn, Yp, Zp);

        // RIGHT
        gl.glVertex3f(Xp, Yp, Zp);
        gl.glVertex3f(Xp, Yn, Zp);
        gl.glVertex3f(Xp, Yn, Zn);
        gl.glVertex3f(Xp, Yp, Zn);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 1f);
        // TOP
        gl.glVertex3f(Xn, Yp, Zn);
        gl.glVertex3f(Xn, Yp, Zp);
        gl.glVertex3f(Xp, Yp, Zp);
        gl.glVertex3f(Xp, Yp, Zn);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 1f);
        // BOTTOM
        gl.glVertex3f(Xn, Yn, Zn);
        gl.glVertex3f(Xp, Yn, Zn);
        gl.glVertex3f(Xp, Yn, Zp);
        gl.glVertex3f(Xn, Yn, Zp);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 1f);
        // FRONT
        gl.glVertex3f(Xn, Yp, Zp);
        gl.glVertex3f(Xn, Yn, Zp);
        gl.glVertex3f(Xp, Yn, Zp);
        gl.glVertex3f(Xp, Yp, Zp);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 1f);
        // BACK
        gl.glVertex3f(Xp, Yp, Zn);
        gl.glVertex3f(Xp, Yn, Zn);
        gl.glVertex3f(Xn, Yn, Zn);
        gl.glVertex3f(Xn, Yp, Zn);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 1f);
        // LEFT
        gl.glVertex3f(Xn, Yp, Zn);
        gl.glVertex3f(Xn, Yn, Zn);
        gl.glVertex3f(Xn, Yn, Zp);
        gl.glVertex3f(Xn, Yp, Zp);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 1f);
        // RIGHT
        gl.glVertex3f(Xp, Yp, Zp);
        gl.glVertex3f(Xp, Yn, Zp);
        gl.glVertex3f(Xp, Yn, Zn);
        gl.glVertex3f(Xp, Yp, Zn);
        gl.glEnd();

        // Restore the old modelview matrix
        gl.glPopMatrix();
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

    protected float maxWidth(List<JGVisual> children) {
        float result = -Float.MAX_VALUE;
        for (JGVisual child : children) {
            float in = child.getWidth();
            if (in > result)
                result = in;
        }
        return result;
    }

    protected float maxHeight(List<JGVisual> children) {
        float result = -Float.MAX_VALUE;
        for (JGVisual child : children) {
            float in = child.getHeight();
            if (in > result)
                result = in;
        }
        return result;
    }

    public void setLocationSeparation(float[] newSep, int level) {
        locationSeparation = FloatMatrixMath.div(newSep,
                (level + 2 * level + 2));
        logger.debug("Level: " + level + " Child separation: "
                + locationSeparation[0]);

        for (JGVisual child : locations) {
            ((JGLocation) child).setLocationSeparation(locationSeparation,
                    level + 1);
        }
    }

    public void setIbisSeparation(float newSep) {
        ibisSeparation[0] = newSep;
        ibisSeparation[1] = newSep;
        ibisSeparation[2] = newSep;

        for (JGVisual child : locations) {
            ((JGLocation) child).setIbisSeparation(newSep);
        }
    }

    public void setMetricSeparation(float newSep) {
        metricSeparation[0] = newSep;
        metricSeparation[1] = newSep;
        metricSeparation[2] = newSep;

        for (JGVisual child : locations) {
            ((JGLocation) child).setMetricSeparation(newSep);
        }
        for (JGVisual child : ibises) {
            ((JGIbis) child).setMetricSeparation(newSep);
        }
    }
}

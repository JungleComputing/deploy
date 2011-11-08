package ibis.deploy.monitoring.visualization.gridvision;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.glu.gl2.GLUgl2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGLocation extends JGVisualAbstract implements JGVisual {
    private static final Logger logger = LoggerFactory.getLogger(JGLocation.class);

    public JGLocation(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, Location dataLocation, float[] newSeparation) {
        super(goggles, parent);

        locationSeparation[0] = newSeparation[0];
        locationSeparation[1] = newSeparation[1];
        locationSeparation[2] = newSeparation[2];

        ibisSeparation[0] = 1.2f;
        ibisSeparation[1] = 1.2f;
        ibisSeparation[2] = 1.2f;

        locationColShape = CollectionShape.SPHERE;
        ibisColShape = CollectionShape.SPHERE;

        goggles.registerVisual(dataLocation, this);

        ArrayList<Location> dataChildren = dataLocation.getChildren();
        for (Location datachild : dataChildren) {
            JGLocation newLocation = new JGLocation(goggles, this, glu, datachild, FloatMatrixMath.div(
                    locationSeparation, datachild.getRank()));
            logger.debug("JGLocation reports child rank: " + datachild.getRank() + " and separation: "
                    + locationSeparation[0]);
            locations.add(newLocation);
        }

        ArrayList<Ibis> dataIbises = dataLocation.getIbises();
        for (Ibis dataIbis : dataIbises) {
            ibises.add(new JGIbis(goggles, this, glu, dataIbis));
        }

        Metric dataMetrics[] = dataLocation.getMetrics();
        for (Metric dataMetric : dataMetrics) {
            metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.MIN));
            metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.NORM));
            metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.MAX));
        }

        name = dataLocation.getName();
    }

    @Override
    protected void setCityScape(List<JGVisual> children, float[] separation) {
        int childCount = children.size();
        float maxWidth = maxWidth(children);

        // get the breakoff point for rows, stacks and columns
        int[] count = new int[3];

        if (children.get(0) instanceof JGMetric) {
            count[0] = 3;
            count[1] = 0;
            count[2] = childCount / 3;
        } else {
            count[0] = (int) Math.ceil(Math.sqrt(childCount));
            count[1] = 0;
            count[2] = (int) Math.floor(Math.sqrt(childCount));
        }

        float[] shift = { 0, 0, 0 };
        shift[0] = maxWidth + separation[0];
        // separation[1] ignored
        shift[2] = maxWidth + separation[2];

        // Center the drawing around the coordinates
        float[] maxShift = { 0, 0, 0 };
        for (int i = 0; i < 3; i++) {
            maxShift[i] = (shift[i] * Math.max((count[i] - 1), 0) * 0.5f);
        }
        float[] centeredCoordinates = FloatMatrixMath.sub(coordinates, maxShift);

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
            childLocation = FloatMatrixMath.add(centeredCoordinates, FloatMatrixMath.mul(shift, position));

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
}

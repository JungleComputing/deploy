package ibis.deploy.monitoring.collection.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;
import ibis.ipl.support.management.AttributeDescription;

public class Load extends
        ibis.deploy.monitoring.collection.impl.MetricDescriptionImpl implements
        ibis.deploy.monitoring.collection.MetricDescription {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.monitoring.collection.metrics.Load");

    public Load() {
        super();

        name = "Load";
        verboseName = "System Load Average";
        type = MetricType.NODE;

        color[0] = 255f / 255f;
        color[1] = 0f / 255f;
        color[2] = 0f / 255f;

        necessaryAttributes.add(new AttributeDescription(
                "java.lang:type=OperatingSystem", "SystemLoadAverage"));

        outputTypes.add(MetricOutput.PERCENT);
    }

    public void update(Object[] results, Metric metric)
            throws IncorrectParametersException {
        ibis.deploy.monitoring.collection.impl.MetricImpl castMetric = ((ibis.deploy.monitoring.collection.impl.MetricImpl) metric);
        if (results[0] instanceof Double) {
            Double value = (Double) results[0];

            Float result = value.floatValue();

            if (result > 1.0f) {
                result = 1.0f;
            }

            try {
                castMetric.setValue(MetricModifier.NORM, MetricOutput.PERCENT,
                        result);
            } catch (BeyondAllowedRangeException e) {
                logger.debug(name
                        + " metric failed trying to set value out of bounds.");
            }

        } else {
            logger.error("Parameter is not of the required type.");
            throw new IncorrectParametersException();
        }
    }
}

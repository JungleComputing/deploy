package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.ipl.support.management.AttributeDescription;

/**
 * The implementation for a description of a metric, in which several properties
 * of this metric are defined.
 * 
 * @author Maarten van Meersbergen
 */

public abstract class MetricDescriptionImpl implements MetricDescription {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.MetricDescription");

	protected String name, verboseName;
	protected MetricType type;
	protected Float[] color;
	protected ArrayList<MetricOutput> outputTypes;
	protected ArrayList<AttributeDescription> necessaryAttributes;
	protected ArrayList<MetricDescription> necessaryMetrics;

	protected int maxForPercent = 100;

	protected MetricDescriptionImpl() {
		color = new Float[3];
		outputTypes = new ArrayList<MetricOutput>();
		necessaryAttributes = new ArrayList<AttributeDescription>();
		necessaryMetrics = new ArrayList<MetricDescription>();
	}

	// Getters
	public String getName() {
		return name;
	}
	
	public String getVerboseName() {
		return verboseName;
	}

	public MetricType getType() {
		return type;
	}

	public Float[] getColor() {
		return color;
	}

	public ArrayList<MetricOutput> getOutputTypes() {
		return outputTypes;
	}

	public ArrayList<AttributeDescription> getNecessaryAttributes() {
		return necessaryAttributes;
	}

	public Metric getMetric(Element element) {
		return new MetricImpl(element, this);
	}
	
	public int getMaxForPercentages() {
		return maxForPercent ;
	}
	
	/**
	 * Function to set the maximum value on which percentage results are based.	 
	 * @param newMax
	 * 		the new maximum value to use in percentage calculations.
	 */
	public void setMaxForPercentages(int newMax) {
		maxForPercent = newMax;
	}
}

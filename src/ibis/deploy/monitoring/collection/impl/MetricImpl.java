package ibis.deploy.monitoring.collection.impl;

import ibis.ipl.IbisIdentifier;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.BeyondAllowedRangeException;
import ibis.deploy.monitoring.collection.exceptions.IncorrectParametersException;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;

/**
 * The abstract implementation of the interface for metrics, used in the
 * gathering module
 * 
 * @author Maarten van Meersbergen
 * 
 */
public class MetricImpl implements Metric {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.collection.impl.Metric");

	protected ibis.deploy.monitoring.collection.impl.CollectorImpl c;
	protected Element element;
	protected MetricDescription myDescription;
	protected HashMap<MetricOutput, Number> values, maxValues, minValues;
	protected HashMap<MetricOutput, HashMap<Element, Number>> linkValues,
			maxLinkValues, minLinkValues;

	protected HashMap<String, Number> helperVariables;

	public MetricImpl(Element element, MetricDescription desc) {
		try {
			this.c = CollectorImpl.getCollector();
		} catch (SingletonObjectNotInstantiatedException e) {
			logger.error("Collector not instantiated properly.");
		}

		this.element = element;
		this.myDescription = desc;

		values = new HashMap<MetricOutput, Number>();
		maxValues = new HashMap<MetricOutput, Number>();
		minValues = new HashMap<MetricOutput, Number>();
		
		for (MetricOutput current : desc.getOutputTypes()) {
			if (current == MetricOutput.PERCENT) {
				values.put(current, 0.0f);
				maxValues.put(current, 0.0f);
				minValues.put(current, 0.0f);
			} else if (current == MetricOutput.RPOS) {
				values.put(current, 0.0f);
				maxValues.put(current, 0.0f);
				minValues.put(current, 0.0f);
			} else if (current == MetricOutput.R) {
				values.put(current, 0.0f);
				maxValues.put(current, 0.0f);
				minValues.put(current, 0.0f);
			} else if (current == MetricOutput.N) {
				values.put(current, 0L);
				maxValues.put(current, 0L);
				minValues.put(current, 0L);
			}
		}

		linkValues = new HashMap<MetricOutput, HashMap<Element, Number>>();
		maxLinkValues = new HashMap<MetricOutput, HashMap<Element, Number>>();
		minLinkValues = new HashMap<MetricOutput, HashMap<Element, Number>>();

		helperVariables = new HashMap<String, Number>();
	}

	public Number getHelperVariable(String name) {
		synchronized (helperVariables) {
			if (helperVariables.containsKey(name)) {
				return helperVariables.get(name);
			} else {
				return (Number) 0L;
			}
		}
	}

	public void setHelperVariable(String name, Number value) {
		synchronized (helperVariables) {
			helperVariables.put(name, value);
		}
	}

	public Number getValue(MetricModifier mod, MetricOutput outputmethod)
			throws OutputUnavailableException {
		if (values.containsKey(outputmethod)) {
			if (mod == MetricModifier.NORM) {
				return values.get(outputmethod);
			} else if (mod == MetricModifier.MAX) {
				return maxValues.get(outputmethod);
			} else {
				return minValues.get(outputmethod);
			}
		} else {
			throw new OutputUnavailableException();
		}
	}

	public Number getValue(MetricModifier mod, MetricOutput outputmethod, Element destination) throws OutputUnavailableException {
		HashMap<Element, Number> resultMap = null;
		if (values.containsKey(outputmethod)) {
			if (mod == MetricModifier.NORM) {
				resultMap = linkValues.get(outputmethod);
			} else if (mod == MetricModifier.MAX) {
				resultMap = maxLinkValues.get(outputmethod);
			} else {
				resultMap = minLinkValues.get(outputmethod);
			}
		} else {
			throw new OutputUnavailableException();
		}
		
		Number result = (Number) 0;
		if (resultMap != null) {
			Number intermediate = resultMap.get(destination);
			if (intermediate != null) {
				result = intermediate;
			}
		}
		return result;
	}

	public void setValue(MetricModifier mod, MetricOutput outputmethod,
			Number value) throws BeyondAllowedRangeException {
				
		checkRange(value, outputmethod);
		
		if (mod == MetricModifier.NORM) {
			values.put(outputmethod, value);
		} else if (mod == MetricModifier.MAX) {
			maxValues.put(outputmethod, value);
		} else if (mod == MetricModifier.MIN) {
			minValues.put(outputmethod, value);
		}
	}

	public void setValue(MetricModifier mod, MetricOutput outputmethod,
			HashMap<IbisIdentifier, Number> values)
			throws BeyondAllowedRangeException {
		HashMap<Element, Number> result = new HashMap<Element, Number>();
		
		for (Map.Entry<IbisIdentifier, Number> entry : values.entrySet()) {
			Element ibis = c.getIbis(entry.getKey());
			Number value = entry.getValue();
			
			checkRange(value, outputmethod);

			result.put(ibis, value);
		}

		if (mod == MetricModifier.NORM) {
			linkValues.put(outputmethod, result);
		} else if (mod == MetricModifier.MAX) {
			maxLinkValues.put(outputmethod, result);
		} else if (mod == MetricModifier.MIN) {
			minLinkValues.put(outputmethod, result);
		}
	}
	
	private void checkRange(Number value, MetricOutput outputmethod) throws BeyondAllowedRangeException {
		if (outputmethod == MetricOutput.PERCENT) {
			if (value.floatValue() < 0f || value.floatValue() > 1f) {
				throw new BeyondAllowedRangeException(value.floatValue() + " is not within the specified range for PERCENT.");
			}
		} else if (outputmethod == MetricOutput.N) {
			if (value.longValue() < 0) {
				throw new BeyondAllowedRangeException(value.floatValue() + " is not within the specified range for N.");
			}
		} else if (outputmethod == MetricOutput.RPOS) {
			if (value.floatValue() < 0f) {
				throw new BeyondAllowedRangeException(value.floatValue() + " is not within the specified range for RPOS.");
			}
		}
	}

	public void update(Object[] results) {
		try {
			((MetricDescriptionImpl) myDescription).update(results, this);
		} catch (IncorrectParametersException shouldnteverhappen) {
			// This is so bad, we're going to throw exceptions until someone
			// fixes it.
			shouldnteverhappen.printStackTrace();
		}
	}

	public MetricDescription getDescription() {
		return myDescription;
	}
}
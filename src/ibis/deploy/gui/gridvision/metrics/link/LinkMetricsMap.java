package ibis.deploy.gui.gridvision.metrics.link;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.gridvision.metrics.Metric;
import ibis.ipl.IbisIdentifier;

public class LinkMetricsMap extends Metric {
	public static final int METRICSGROUP = 3;
		
	protected Map<IbisIdentifier, Float> values;
	
	public LinkMetricsMap() {
		super();
		this.group = METRICSGROUP;
	}
		
	public Map<IbisIdentifier, Float> getValues() {
		Map<IbisIdentifier, Float> result = new HashMap<IbisIdentifier, Float>(values);
		return result;
	}
}
package ibis.deploy.gui.performance.metrics.link;

import java.util.Map;

import ibis.deploy.gui.performance.metrics.Metric;
import ibis.ipl.IbisIdentifier;

public class LinkMetricsMap extends Metric {
	public static final int METRICSGROUP = 3;
		
	protected Map<IbisIdentifier, Float> values;
	
	public LinkMetricsMap() {
		super();
		this.group = METRICSGROUP;
	}
		
	public Map<IbisIdentifier, Float> getValues() {
		return values;
	}
}

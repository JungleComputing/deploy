package ibis.deploy.gui.performance.metrics.link;

import ibis.deploy.gui.performance.metrics.Metric;

public class LinkMetricsObject extends Metric {
	public static final int METRICSGROUP = 2;
		
	protected float value;	
	
	public LinkMetricsObject() {
		super();
		this.group = METRICSGROUP;
	}
		
	public float getValue() {
		return value;
	}
}

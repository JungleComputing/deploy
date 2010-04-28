package ibis.deploy.gui.performance.metrics.link;

import ibis.deploy.gui.performance.metrics.MetricsObject;

public class LinkMetricsObject extends MetricsObject {
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

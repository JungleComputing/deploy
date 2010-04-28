package ibis.deploy.gui.performance.metrics.node;

import ibis.deploy.gui.performance.metrics.MetricsObject;

public class NodeMetricsObject extends MetricsObject {	
	public static final int METRICSGROUP = 1;
		
	protected float value;	
	
	public NodeMetricsObject() {
		super();
		this.group = METRICSGROUP;
	}
		
	public float getValue() {
		return value;
	}
}

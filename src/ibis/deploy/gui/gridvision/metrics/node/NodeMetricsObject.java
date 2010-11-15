package ibis.deploy.gui.gridvision.metrics.node;

import ibis.deploy.gui.gridvision.metrics.Metric;

public class NodeMetricsObject extends Metric {	
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

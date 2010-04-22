package ibis.deploy.gui.performance.newtry.metrics.node;

import ibis.deploy.gui.performance.newtry.metrics.MetricsObject;

public class NodeMetricsObject extends MetricsObject{
	public static final String NAME = "GENERIC_NODE_OBJECT";
	public static final int METRICSGROUP = 1;
		
	protected float value;	
		
	public float getValue() {
		return value;
	}
}

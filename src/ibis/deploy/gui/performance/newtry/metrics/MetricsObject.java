package ibis.deploy.gui.performance.newtry.metrics;

import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.ipl.support.management.AttributeDescription;

public class MetricsObject implements MetricsObjectInterface {
	public static final String NAME = "GENERIC";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 0;
	public static Float[] COLOR = {1.0f, 1.0f, 1.0f};
	public static final int METRICSGROUP = 0;
	
	protected AttributeDescription[] necessaryAttributes;
	protected float value;	
	
	public void update(Object[] results) throws MethodNotOverriddenException {
		throw new MethodNotOverriddenException();
	}
	
	public AttributeDescription[] getNecessaryAttributes() {		
		return necessaryAttributes;
	}	
	
	public float getValue() {
		return value;
	}

	public int getAttributesCountNeeded() {
		return DESCRIPTIONS_COUNT_NEEDED;
	}
	
	public String getName() {
		return NAME;
	}
	
	public Float[] getColor() {
		return COLOR;
	}
	
	public int getGroup() {
		return METRICSGROUP;
	}
}

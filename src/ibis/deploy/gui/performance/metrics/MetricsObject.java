package ibis.deploy.gui.performance.metrics;

import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.ipl.support.management.AttributeDescription;

public class MetricsObject implements MetricsObjectInterface {	
	protected String name;
	protected int group;
	protected Float[] color; // = {1.0f,1.0f,1.0f};
	protected AttributeDescription[] necessaryAttributes;
	protected int attributesCountNeeded;
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
		return attributesCountNeeded;
	}
	
	public String getName() {
		return name;
	}
	
	public Float[] getColor() {
		return color;
	}
	
	public int getGroup() {
		return group;
	}
}

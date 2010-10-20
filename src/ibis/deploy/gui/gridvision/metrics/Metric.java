package ibis.deploy.gui.gridvision.metrics;

import ibis.deploy.gui.gridvision.exceptions.MethodNotOverriddenException;
import ibis.ipl.support.management.AttributeDescription;

public class Metric implements MetricInterface {	
	protected String name;
	protected int group;
	protected Float[] color; // = {1.0f,1.0f,1.0f};
	protected AttributeDescription[] necessaryAttributes;
	protected int attributesCountNeeded;
	protected float value;	
		
	public Metric clone() {
		Metric clone = new Metric();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public void setColor(Float[] color) {
		this.color = color;
	}

	public void setNecessaryAttributes(AttributeDescription[] necessaryAttributes) {
		this.necessaryAttributes = necessaryAttributes;
	}

	public void setAttributesCountNeeded(int attributesCountNeeded) {
		this.attributesCountNeeded = attributesCountNeeded;
	}

	public void setValue(float value) {
		this.value = value;
	}

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

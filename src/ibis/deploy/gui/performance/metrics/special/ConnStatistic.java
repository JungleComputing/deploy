package ibis.deploy.gui.performance.metrics.special;

import ibis.deploy.gui.performance.metrics.MetricsObject;
import ibis.deploy.gui.performance.metrics.MetricsObjectInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class ConnStatistic extends MetricsObject implements MetricsObjectInterface {
	public static final String NAME = "CONN";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 0.0f};
	
	protected IbisIdentifier[] ibises;
	
	public ConnStatistic() {
		super();
		this.group = 4;
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "connections");		
	}
	
	public void update(Object[] results) {
		ibises = (IbisIdentifier[]) results[0];		
	}	
	
	public IbisIdentifier[] getIbises() {
		return ibises;
	}
	
	public ConnStatistic clone() {
		ConnStatistic clone = new ConnStatistic();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

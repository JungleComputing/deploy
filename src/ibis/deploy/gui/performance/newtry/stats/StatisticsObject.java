package ibis.deploy.gui.performance.newtry.stats;

import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class StatisticsObject {
	public static final String NAME = "GENERIC";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 0;
	protected AttributeDescription[] necessaryAttributes;
	protected float value;
	protected IbisIdentifier[] ibises;
	
	public void update(Object[] results) throws MethodNotOverriddenException {
		throw new MethodNotOverriddenException();
	}
	
	public AttributeDescription[] getNecessaryAttributes() {		
		return necessaryAttributes;
	}	
	
	public float getValue() {
		return value;
	}
		
	public IbisIdentifier[] getIbises() {
		return ibises;
	}

	public int getAttributesCountNeeded() {
		return DESCRIPTIONS_COUNT_NEEDED;
	}
	
	public String getName() {
		return NAME;
	}
}

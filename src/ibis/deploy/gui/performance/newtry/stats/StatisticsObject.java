package ibis.deploy.gui.performance.newtry.stats;

import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.ipl.support.management.AttributeDescription;

public class StatisticsObject {
	public static final String NAME = "GENERIC";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 0;
	protected AttributeDescription[] necessaryAttributes;
	protected Object[] values;
	
	public void update(Object[] results) throws MethodNotOverriddenException {
		throw new MethodNotOverriddenException();
	}
	
	public AttributeDescription[] getNecessaryAttributes() {		
		return necessaryAttributes;
	}	
	
	public Object[] getValues() {
		return values;
	}
	
	public int getAttributesCountNeeded() {
		return DESCRIPTIONS_COUNT_NEEDED;
	}
	
	public String getName() {
		return NAME;
	}
}

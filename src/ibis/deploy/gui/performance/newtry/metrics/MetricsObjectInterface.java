package ibis.deploy.gui.performance.newtry.metrics;

import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.ipl.support.management.AttributeDescription;

public interface MetricsObjectInterface {
	
	public void update(Object[] results) throws MethodNotOverriddenException;
	
	public int getAttributesCountNeeded();
	
	public AttributeDescription[] getNecessaryAttributes();
	
	public float getValue();		
	
	public String getName();
	
	public Float[] getColor();
}

package ibis.deploy.gui.gridvision.metrics;

import ibis.deploy.gui.gridvision.exceptions.MethodNotOverriddenException;
import ibis.ipl.support.management.AttributeDescription;

public interface MetricInterface {
	
	public void update(Object[] results) throws MethodNotOverriddenException;
	
	public int getAttributesCountNeeded();
	
	public AttributeDescription[] getNecessaryAttributes();
	
	public float getValue();		
	
	public String getName();
	
	public Float[] getColor();
}

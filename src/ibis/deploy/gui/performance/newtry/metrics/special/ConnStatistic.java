package ibis.deploy.gui.performance.newtry.metrics.special;

import ibis.deploy.gui.performance.newtry.metrics.MetricsObject;
import ibis.deploy.gui.performance.newtry.metrics.MetricsObjectInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class ConnStatistic extends MetricsObject implements MetricsObjectInterface {
	public static final String NAME = "CONN";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	
	protected IbisIdentifier[] ibises;
	
	public ConnStatistic() {
		super();
		
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "connections");		
	}
	
	public void update(Object[] results) {
		ibises = (IbisIdentifier[]) results[0];		
	}	
	
	public IbisIdentifier[] getIbises() {
		return ibises;
	}
}

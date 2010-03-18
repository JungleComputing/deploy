package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;

public class ConnStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "CONN";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	
	public ConnStatistic() {
		super();
		
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "connections");		
	}
	
	public void update(Object[] results) {
		values = (IbisIdentifier[]) results[0];		
	}
}

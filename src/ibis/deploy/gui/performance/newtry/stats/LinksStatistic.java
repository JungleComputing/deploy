package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;


public class LinksStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "LINKS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	private long sent_prev, sent_max;
	
	public LinksStatistic() {
		super();
		
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "bytesSent");
	}
	
	public void update(Object[] results) {				
		Long bytesSent = (Long) results[0] - sent_prev;
			
		sent_prev = (Long) results[7];
		
		sent_max = Math.max(sent_max, bytesSent);
		value = (float)bytesSent/(float)sent_max;
	}
}

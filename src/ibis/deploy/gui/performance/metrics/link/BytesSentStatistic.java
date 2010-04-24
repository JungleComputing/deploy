package ibis.deploy.gui.performance.metrics.link;

import ibis.deploy.gui.performance.metrics.MetricsObjectInterface;
import ibis.ipl.support.management.AttributeDescription;


public class BytesSentStatistic extends LinkMetricsObject implements MetricsObjectInterface {
	public static final String NAME = "LINKS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 1.0f};
	
	private long sent_prev, sent_max;
	
	public BytesSentStatistic() {
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

package ibis.deploy.gui.performance.metrics.link;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.support.management.AttributeDescription;


public class BytesSentMetric extends LinkMetricsObject implements MetricInterface {
	public static final String NAME = "BYTES_SENT";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 1.0f};
	
	private long sent_prev, sent_max;
	
	public BytesSentMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		
		sent_prev = 0;
		sent_max = 0;
		
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "bytesSent");
	}
	
	public void update(Object[] results) {
		Long bytesSent_elapsed = (Long) results[0] - sent_prev;
		sent_prev = bytesSent_elapsed;
		
		sent_max = Math.max(sent_max, bytesSent_elapsed);
		value = (float)bytesSent_elapsed/(float)sent_max;
		
		if (Float.isNaN(value) || value < 0.0f || value > 1.0f) {
			value = 0.0f;
		}
	}
	
	public BytesSentMetric clone() {
		BytesSentMetric clone = new BytesSentMetric();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

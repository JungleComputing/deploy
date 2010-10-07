package ibis.deploy.gui.performance.metrics.link;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.support.management.AttributeDescription;


public class BytesReadStatistic extends LinkMetricsObject implements MetricInterface {
	public static final String NAME = "BYTES_READ";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.5f, 1.0f};
	
	private long sent_prev, sent_max;
	
	public BytesReadStatistic() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "bytesRead");
	}
	
	public void update(Object[] results) {
		
		Long bytesSent_elapsed = (Long) results[0] - sent_prev;
		sent_prev = bytesSent_elapsed;
		
		sent_max = Math.max(sent_max, bytesSent_elapsed);
		value = (float)bytesSent_elapsed/(float)sent_max;
		
		//value = 0.5f;
	}
	
	public BytesReadStatistic clone() {
		BytesReadStatistic clone = new BytesReadStatistic();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

package ibis.deploy.gui.performance.metrics.link;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.support.management.AttributeDescription;


public class BytesWrittenStatistic extends LinkMetricsObject implements MetricInterface {
	public static final String NAME = "BYTES_WRITTEN";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 0.5f};
	
	private long sent_prev, sent_max;
	
	public BytesWrittenStatistic() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "bytesWritten");
	}
	
	public void update(Object[] results) {		
		Long bytes_elapsed = (Long) results[0] - sent_prev;
		sent_prev = bytes_elapsed;
		
		sent_max = Math.max(sent_max, bytes_elapsed);
		value = (float)bytes_elapsed/(float)sent_max;		
	}
	
	public BytesWrittenStatistic clone() {
		BytesWrittenStatistic clone = new BytesWrittenStatistic();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

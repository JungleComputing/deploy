package ibis.deploy.gui.performance.metrics.link;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;


public class BytesSentPerIbisMetric extends LinkMetricsMap implements MetricInterface {
	public static final String NAME = "BYTES_SENT_PER_IBIS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 1.0f};
	
	private Map<IbisIdentifier, Long> sent_prev, sent_max;	
	
	public BytesSentPerIbisMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		
		sent_prev = new HashMap<IbisIdentifier, Long>();
		sent_max = new HashMap<IbisIdentifier, Long>();
		values = new HashMap<IbisIdentifier, Float>();
		
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "sentBytesPerIbis");
	}
	
	@SuppressWarnings("unchecked")
	public void update(Object[] results) {
		Map<IbisIdentifier, Long> sent = (Map<IbisIdentifier, Long>) results[0];
		
		for (Map.Entry<IbisIdentifier, Long> entry : sent.entrySet()) {
			IbisIdentifier ibis = entry.getKey();			
			Long elapsed = entry.getValue() - sent_prev.get(ibis);
			sent_prev.put(ibis, elapsed);
			sent_max.put(ibis, Math.max(elapsed, sent_max.get(ibis)));
			
			Float value = (float)elapsed/(float)sent_max.get(ibis);
			
			if (Float.isNaN(value) || value < 0.0f || value > 1.0f) {
				value = 0.0f;
			} else {
				values.put(ibis, value);
			}
		}		
	}
	
	public BytesSentPerIbisMetric clone() {
		BytesSentPerIbisMetric clone = new BytesSentPerIbisMetric();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

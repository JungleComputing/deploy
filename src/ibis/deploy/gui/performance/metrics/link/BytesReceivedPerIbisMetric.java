package ibis.deploy.gui.performance.metrics.link;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;


public class BytesReceivedPerIbisMetric extends LinkMetricsObject implements MetricInterface {
	public static final String NAME = "BYTES_RECEIVED_PER_IBIS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 1.0f};
	
	private Map<IbisIdentifier, Long> received_prev, received_max;
	private Map<IbisIdentifier, Float> values;
	
	public BytesReceivedPerIbisMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		
		received_prev = new HashMap<IbisIdentifier, Long>();
		received_max = new HashMap<IbisIdentifier, Long>();
		values = new HashMap<IbisIdentifier, Float>();
		
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "receivedBytesPerIbis");
	}
	
	@SuppressWarnings("unchecked")
	public void update(Object[] results) {
		Map<IbisIdentifier, Long> received = (Map<IbisIdentifier, Long>) results[0];
		
		for (Map.Entry<IbisIdentifier, Long> entry : received.entrySet()) {
			IbisIdentifier ibis = entry.getKey();			
			Long elapsed = entry.getValue() - received_prev.get(ibis);
			received_prev.put(ibis, elapsed);
			received_max.put(ibis, Math.max(elapsed, received_max.get(ibis)));
			
			Float value = (float)elapsed/(float)received_max.get(ibis);
			
			if (Float.isNaN(value) || value < 0.0f || value > 1.0f) {
				value = 0.0f;
			} else {
				values.put(ibis, value);
			}
		}		
	}
	
	public BytesReceivedPerIbisMetric clone() {
		BytesReceivedPerIbisMetric clone = new BytesReceivedPerIbisMetric();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

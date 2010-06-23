package ibis.deploy.gui.performance.metrics.link;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;


public class BytesSentPerIbisMetric extends LinkMetricsMap implements MetricInterface {
	public static final String NAME = "BYTES_SENT_PER_IBIS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.6f, 1.0f};
	
	private Map<IbisIdentifier, Long> sent_prev, sent_max;	
	private long time_prev;
	
	public BytesSentPerIbisMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		
		time_prev = 0L;
		
		sent_prev = new HashMap<IbisIdentifier, Long>();
		sent_max = new HashMap<IbisIdentifier, Long>();
		values = new HashMap<IbisIdentifier, Float>();
				
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "sentBytesPerIbis");
		//necessaryAttributes[1] = new AttributeDescription("java.lang:type=Runtime", "Uptime");
		
	}
		
	@SuppressWarnings("unchecked")
	public void update(Object[] results) {
		long time_now = System.currentTimeMillis();
		long time_elapsed = time_now - time_prev;	
		
		Float value = 0.0f;
		
		Map<IbisIdentifier, Long> sent = (Map<IbisIdentifier, Long>) results[0];
				
		for (Map.Entry<IbisIdentifier, Long> entry : sent.entrySet()) {
			IbisIdentifier ibis = entry.getKey();
			Long bytes_elapsed = entry.getValue();
			Long bytes_per_sec = (bytes_elapsed / time_elapsed) / 1000L;
						
			if (!sent_prev.containsKey(ibis)) {				
				sent_max.put(ibis, bytes_per_sec);
			} else {
				bytes_elapsed -= sent_prev.get(ibis);								
				sent_max.put(ibis, Math.max(bytes_per_sec, sent_max.get(ibis)));
			}			
			
			sent_prev.put(ibis, bytes_elapsed);
			value = (float)bytes_per_sec/(float)sent_max.get(ibis);
			
			if (Float.isNaN(value) || value < 0.0f || value > 1.0f) {
				value = 0.0f;
			}
			
			values.put(ibis, value);					
		}	
		time_prev = time_now;
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

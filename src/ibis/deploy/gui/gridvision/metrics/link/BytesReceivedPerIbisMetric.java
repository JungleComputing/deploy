package ibis.deploy.gui.performance.metrics.link;

import java.util.HashMap;
import java.util.Map;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.support.management.AttributeDescription;


public class BytesReceivedPerIbisMetric extends LinkMetricsMap implements MetricInterface {
	public static final String NAME = "BYTES_RECEIVED_PER_IBIS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.3f, 0.3f, 1.0f};
	
	private Map<IbisIdentifier, Long> received_prev;
	private Map<IbisIdentifier, Float> bps_max;	
	private long time_prev;
	
	public BytesReceivedPerIbisMetric() {
		super();
		this.name = NAME;
		this.color = COLOR;
		
		time_prev = System.currentTimeMillis();
		
		received_prev = new HashMap<IbisIdentifier, Long>();
		bps_max = new HashMap<IbisIdentifier, Float>();
		values = new HashMap<IbisIdentifier, Float>();
		
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "receivedBytesPerIbis");
	}
	
	@SuppressWarnings("unchecked")
	public void update(Object[] results) {
		long time_now = System.currentTimeMillis();
		long time_elapsed = time_now - time_prev;	
		float time_seconds = (float)time_elapsed / 1000.0f;
		Float value = 1.0f;
		
		Map<IbisIdentifier, Long> sent = (Map<IbisIdentifier, Long>) results[0];
		
		values.clear();
				
		for (Map.Entry<IbisIdentifier, Long> entry : sent.entrySet()) {
			IbisIdentifier ibis = entry.getKey();
			
			Long bytes_elapsed = entry.getValue();
						
			if (received_prev.containsKey(ibis)) {
				bytes_elapsed -= received_prev.get(ibis);
			}
			
			float bytes_per_sec = bytes_elapsed / time_seconds;
			
			if (bps_max.containsKey(ibis)) {
				bps_max.put(ibis, Math.max(bytes_per_sec, bps_max.get(ibis)));
				value = (float)bytes_per_sec/(float)bps_max.get(ibis);
			} else {
				bps_max.put(ibis, bytes_per_sec);
			}
			
			received_prev.put(ibis, bytes_elapsed);			
			
			values.put(ibis, value);					
		}	
		time_prev = time_now;
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

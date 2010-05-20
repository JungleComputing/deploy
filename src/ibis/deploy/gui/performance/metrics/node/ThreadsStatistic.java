package ibis.deploy.gui.performance.metrics.node;

import ibis.deploy.gui.performance.metrics.MetricInterface;
import ibis.ipl.support.management.AttributeDescription;

public class ThreadsStatistic extends NodeMetricsObject implements MetricInterface {
	public static final String NAME = "THREADS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.5f, 0.5f, 0.5f};
	
	private int thread_max;
	
	public ThreadsStatistic() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("java.lang:type=Threading", "ThreadCount");
		
		thread_max = 0;
	}
	
	public void update(Object[] results) {		
		int num_threads		= (Integer) results[0];
		thread_max = Math.max(thread_max, num_threads);		
		
		value = ((long)num_threads / (long)thread_max);
	}
	
	public ThreadsStatistic clone() {
		ThreadsStatistic clone = new ThreadsStatistic();
		clone.setName(name);
		clone.setGroup(group);
		clone.setColor(color);
		clone.setNecessaryAttributes(necessaryAttributes);
		clone.setAttributesCountNeeded(attributesCountNeeded);
		clone.setValue(value);	
		return clone;
	}
}

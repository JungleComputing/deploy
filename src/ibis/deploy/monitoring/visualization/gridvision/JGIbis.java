package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;


public class JGIbis extends JGVisualAbstract implements JGVisual  {	
	public JGIbis(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, Ibis dataIbis) {
		super(goggles, parent);
		
		state = State.COLLAPSED;
				
		goggles.registerVisual(dataIbis, this);
		
		Metric dataMetrics[] = dataIbis.getMetrics();
		
		if (containsAllMemory(dataMetrics)) {
			Metric[] memMetrics = new Metric[3];			
					
			for (Metric dataMetric : dataMetrics) {
				if (dataMetric.getDescription().getName().compareTo("MEM_SYS") == 0) {
					memMetrics[0] = dataMetric;
				} else if (dataMetric.getDescription().getName().compareTo("MEM_HEAP") == 0) {
					memMetrics[2] = dataMetric;
				} else if (dataMetric.getDescription().getName().compareTo("MEM_NONHEAP") == 0) {
					memMetrics[1] = dataMetric;
				} else {
					metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.NORM));
				}
			}
			metrics.add(new JGCombinedMetric(goggles, this, memMetrics));
		} else {
			for (Metric dataMetric : dataMetrics) {
				metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.NORM));
			}
		}
		
		name = dataIbis.getLocation().getName();
	}
	
	private boolean containsAllMemory(Metric[] metrics) {		
		int found = 0;
		for (Metric metric : metrics) {
			if (metric.getDescription().getName().compareTo("MEM_SYS") == 0 ||				
				metric.getDescription().getName().compareTo("MEM_HEAP") == 0||
				metric.getDescription().getName().compareTo("MEM_NONHEAP") == 0) {
				found ++;
			}
		}
		if (found ==3) {
			return true;			
		} else return false;
	}
}

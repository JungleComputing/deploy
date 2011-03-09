package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Metric;


public class JGIbis extends JGVisualAbstract implements JGVisual  {	
	
	public JGIbis(JungleGoggles jv, GLUgl2 glu, Ibis dataIbis) {
		super();		
		separation = 0.05f;
		
		jv.registerVisual(dataIbis, this);
		
		Metric dataMetrics[] = dataIbis.getMetrics();
		
		for (Metric dataMetric : dataMetrics) {			
			metrics.add(new JGMetric(jv, glu, dataMetric));
		}
		
		constructDimensions();
	}
}

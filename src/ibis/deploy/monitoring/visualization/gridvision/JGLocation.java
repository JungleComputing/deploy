package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;


public class JGLocation extends JGVisualAbstract implements JGVisual {		
	public JGLocation(JungleGoggles jv, GLUgl2 glu, Location dataLocation) {
		super();

		locationSeparation[0] = 12;
		locationSeparation[1] =-12;
		locationSeparation[2] = 12;
				 
		ibisSeparation[0] = 1;
		ibisSeparation[1] = -4;
		ibisSeparation[2] = 1;
				
		locationColShape = CollectionShape.CITYSCAPE;
		ibisColShape = CollectionShape.SPHERE;
		
		jv.registerVisual(dataLocation, this);
				
		ArrayList<Location> dataChildren = dataLocation.getChildren();		
		for (Location datachild : dataChildren) {
			locations.add(new JGLocation(jv, glu, datachild));
		}
		
		ArrayList<Ibis> dataIbises = dataLocation.getIbises();		
		for (Ibis dataIbis : dataIbises) {
			ibises.add(new JGIbis(jv, glu, dataIbis));
		}
		
		Metric dataMetrics[] = dataLocation.getMetrics();
		for (Metric dataMetric : dataMetrics) {			
			metrics.add(new JGMetric(jv, glu, dataMetric, MetricModifier.MIN));
			metrics.add(new JGMetric(jv, glu, dataMetric, MetricModifier.NORM));
			metrics.add(new JGMetric(jv, glu, dataMetric, MetricModifier.MAX));
		}
	}
}

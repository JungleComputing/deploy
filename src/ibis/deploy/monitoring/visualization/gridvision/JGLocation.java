package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;


public class JGLocation extends JGVisualAbstract implements JGVisual {		
	public JGLocation(JungleGoggles goggles, GLUgl2 glu, Location dataLocation) {
		super(goggles);

		locationSeparation[0] = 12;
		locationSeparation[1] =-12;
		locationSeparation[2] = 12;
		
		ibisSeparation[0] = 1;
		ibisSeparation[1] = -4;
		ibisSeparation[2] = 1;
				
		locationColShape = CollectionShape.CITYSCAPE;
		ibisColShape = CollectionShape.CITYSCAPE;
		
		goggles.registerVisual(dataLocation, this);
				
		ArrayList<Location> dataChildren = dataLocation.getChildren();		
		for (Location datachild : dataChildren) {
			locations.add(new JGLocation(goggles, glu, datachild));
		}
		
		ArrayList<Ibis> dataIbises = dataLocation.getIbises();		
		for (Ibis dataIbis : dataIbises) {
			ibises.add(new JGIbis(goggles, glu, dataIbis));
		}
		
		Metric dataMetrics[] = dataLocation.getMetrics();
		for (Metric dataMetric : dataMetrics) {			
			metrics.add(new JGMetric(goggles, glu, this, dataMetric, MetricModifier.MIN));
			metrics.add(new JGMetric(goggles, glu, this, dataMetric, MetricModifier.NORM));
			metrics.add(new JGMetric(goggles, glu, this, dataMetric, MetricModifier.MAX));
		}
	}	
}

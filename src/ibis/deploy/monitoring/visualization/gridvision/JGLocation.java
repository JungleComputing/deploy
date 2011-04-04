package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;


public class JGLocation extends JGVisualAbstract implements JGVisual {		
	public JGLocation(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, Location dataLocation, float[] newSeparation) {
		super(goggles, parent);

		locationSeparation[0] = newSeparation[0];
		locationSeparation[1] = newSeparation[1];
		locationSeparation[2] = newSeparation[2];
		
		ibisSeparation[0] = 1;
		ibisSeparation[1] = 1.25f;
		ibisSeparation[2] = 1;
				
		locationColShape = CollectionShape.SPHERE;
		ibisColShape = CollectionShape.SPHERE;
		
		goggles.registerVisual(dataLocation, this);
				
		ArrayList<Location> dataChildren = dataLocation.getChildren();		
		for (Location datachild : dataChildren) {
			JGLocation newLocation = new JGLocation(goggles, this, glu, datachild, FloatMatrixMath.div(locationSeparation, 2));			
			locations.add(newLocation);			
		}
		
		ArrayList<Ibis> dataIbises = dataLocation.getIbises();		
		for (Ibis dataIbis : dataIbises) {
			ibises.add(new JGIbis(goggles, this, glu, dataIbis));
		}
		
		Metric dataMetrics[] = dataLocation.getMetrics();
		for (Metric dataMetric : dataMetrics) {			
			metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.MIN));
			metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.NORM));
			metrics.add(new JGMetric(goggles, this, dataMetric, MetricModifier.MAX));
		}
	}	
}

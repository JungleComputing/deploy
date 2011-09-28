package ibis.deploy.monitoring.visualization.gridvision.models;

import ibis.deploy.monitoring.visualization.gridvision.common.*;
import ibis.deploy.monitoring.visualization.gridvision.models.base.Sphere;

public class MetricSphere {	
	private Model sphere, transparentSphere;
	
	public MetricSphere(int ndiv, float radius, Color4 color, Color4 transparentColor) {
		transparentSphere = new Sphere(ndiv, 1f, transparentColor);
		sphere = new Sphere(ndiv, radius, color);	
	}
	
	public Model getSolids() {
		return sphere;
	}
	
	public Model getTransparents() {
		return transparentSphere;
	}
}

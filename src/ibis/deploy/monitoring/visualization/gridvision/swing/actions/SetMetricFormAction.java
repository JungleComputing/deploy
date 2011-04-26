package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import java.awt.event.ActionEvent;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.MetricShape;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;

public class SetMetricFormAction implements GoggleAction {
	private static final long serialVersionUID = 7987449048219770239L;
	
	JungleGoggles goggles;
	MetricShape myShape;
	
	public SetMetricFormAction(JungleGoggles goggles, String label) {
		this.goggles = goggles;
		
		if (label.compareTo("Bars") 	== 0) myShape = JGVisual.MetricShape.BAR;
		else if (label.compareTo("Tubes") 	== 0) myShape = JGVisual.MetricShape.TUBE;
		else if (label.compareTo("Spheres") 	== 0) myShape = JGVisual.MetricShape.SPHERE;
	}

	public void actionPerformed(ActionEvent e) {		
		goggles.setMetricForm(myShape);			
	}
	
	public GoggleAction clone(String label) {
		return new SetMetricFormAction(goggles, label);		
	}
}

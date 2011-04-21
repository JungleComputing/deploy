package ibis.deploy.monitoring.visualization.gridvision.swing;

import java.awt.event.ActionEvent;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.MetricShape;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;

public class SetNetworkFormAction implements GoggleAction {
	private static final long serialVersionUID = 7987449048219770239L;
	
	JungleGoggles goggles;
	MetricShape myShape;
	
	public SetNetworkFormAction(JungleGoggles goggles, String label) {
		this.goggles = goggles;
		
		if (label.compareTo("Tubes") 	== 0) myShape = JGVisual.MetricShape.TUBE;
		else if (label.compareTo("AlphaTubes") 	== 0) myShape = JGVisual.MetricShape.ALPHATUBE;
		else if (label.compareTo("Particles") 	== 0) myShape = JGVisual.MetricShape.PARTICLES;
	}

	public void actionPerformed(ActionEvent e) {		
		goggles.setNetworkForm(myShape);			
	}
	
	public GoggleAction clone(String label) {
		return new SetNetworkFormAction(goggles, label);		
	}
}

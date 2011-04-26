package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import java.awt.event.ActionEvent;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.CollectionShape;

public class SetCollectionFormAction implements GoggleAction {
	private static final long serialVersionUID = 7987449048219770239L;
	
	JGVisual caller;
	CollectionShape myShape;
	
	public SetCollectionFormAction(JGVisual caller, String label) {
		this.caller = caller;
		
		if (label.compareTo("Cityscape") 	== 0) myShape = JGVisual.CollectionShape.CITYSCAPE;
		else if (label.compareTo("Cube") 	== 0) myShape = JGVisual.CollectionShape.CUBE;
		else if (label.compareTo("Sphere") 	== 0) myShape = JGVisual.CollectionShape.SPHERE;
	}

	public void actionPerformed(ActionEvent e) {
		caller.setCollectionShape(myShape);
	}
	
	public GoggleAction clone(String label) {
		return new SetCollectionFormAction(caller, label);		
	}
}

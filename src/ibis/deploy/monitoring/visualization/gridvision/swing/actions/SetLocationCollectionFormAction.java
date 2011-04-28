package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual.CollectionShape;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;

public class SetLocationCollectionFormAction implements ActionListener {
	private static final long serialVersionUID = 7987449048219770239L;
	
	JungleGoggles goggles;
	CollectionShape myShape;
	
	public SetLocationCollectionFormAction(JungleGoggles goggles, String label, CollectionShape shape) {
		this.goggles = goggles;
		
		this.myShape = shape;
	}

	public void actionPerformed(ActionEvent e) {
		goggles.setLocationCollectionForm(myShape);
	}
	
	public SetLocationCollectionFormAction clone(String label, CollectionShape shape) {
		return new SetLocationCollectionFormAction(goggles, label, shape);		
	}
}

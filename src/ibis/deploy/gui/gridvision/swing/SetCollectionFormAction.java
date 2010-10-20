package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.visuals.VisualElementInterface;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetCollectionFormAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	VisualElementInterface caller;
	int myCollections;
	
	public SetCollectionFormAction(VisualElementInterface caller, String label) {
		super(label);
		this.caller = caller;
		
		if (label.compareTo("Cityscape") == 0) 	myCollections = VisualElementInterface.COLLECTION_CITYSCAPE;
		else if (label.compareTo("Circle") == 0) 	myCollections = VisualElementInterface.COLLECTION_CIRCLE;
		else if (label.compareTo("Sphere") == 0) 	myCollections = VisualElementInterface.COLLECTION_SPHERE;
	}

	public void actionPerformed(ActionEvent e) {	
		try {
			caller.setForm(myCollections);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}		
	}
}

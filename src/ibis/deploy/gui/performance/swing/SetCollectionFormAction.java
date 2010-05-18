package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.visuals.Vobject;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetCollectionFormAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	Vobject caller;
	int myCollections;
	
	public SetCollectionFormAction(Vobject caller, String label) {
		super(label);
		this.caller = caller;
		
		if (label.compareTo("Cityscape") == 0) 	myCollections = Vobject.COLLECTION_CITYSCAPE;
		else if (label.compareTo("Circle") == 0) 	myCollections = Vobject.COLLECTION_CIRCLE;
	}

	public void actionPerformed(ActionEvent e) {	
		try {
			caller.setForm(myCollections);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}		
	}
}

package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.visuals.Vpool;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetCollectionsAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	PerfVis perfvis;
	int myCollections;
	
	public SetCollectionsAction(PerfVis perfvis, String label) {
		super(label);
		this.perfvis = perfvis;
		
		if (label.compareTo("Cityscapes") == 0) 	myCollections = Vpool.CITYSCAPE;
		else if (label.compareTo("Circles") == 0) 	myCollections = Vpool.CIRCLE;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			perfvis.setCollectionForm(myCollections);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}	
	}
}

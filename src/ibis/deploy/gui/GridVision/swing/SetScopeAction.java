package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetScopeAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	PerfVis perfvis;
	int myScope;
	
	public SetScopeAction(PerfVis perfvis, String label) {
		super(label);
		
		this.perfvis = perfvis;
		
		/*
		if (label.compareTo("Grid Overview") == 0) 	myScope = PerfVis.SCOPE_GRID;
		else if (label.compareTo("Nodes") == 0) 	myScope = PerfVis.SCOPE_NODES;
		*/
	}

	public void actionPerformed(ActionEvent e) {
		/*
		try {
			perfvis.setScope(myScope);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}
		*/	
	}
}

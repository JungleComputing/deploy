package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.visuals.VisualElementInterface;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ToggleAveragesAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	String label;
	VisualElementInterface caller;
	
	public ToggleAveragesAction(VisualElementInterface caller, String label) {
		super(label);
		this.caller = caller;
		this.label = label;		
	}

	public void actionPerformed(ActionEvent e) {		
		caller.toggleAverages();		
	}
}

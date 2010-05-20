package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.performance.visuals.Vobject;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ToggleAveragesAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	String label;
	Vobject caller;
	
	public ToggleAveragesAction(Vobject caller, String label) {
		super(label);
		this.caller = caller;
		this.label = label;		
	}

	public void actionPerformed(ActionEvent e) {		
		caller.toggleAverages();		
	}
}

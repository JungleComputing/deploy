package ibis.deploy.monitoring.visualization.gridvision.swing;

import java.awt.event.ActionEvent;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.State;

public class SetCollapseAction implements GoggleAction {
	private static final long serialVersionUID = 7987449048219770239L;
	
	JGVisual caller;
	State myState;
	
	public SetCollapseAction(JGVisual caller, String label) {
		this.caller = caller;
		
		if (label.compareTo("Collapse") 	== 0) myState = JGVisual.State.COLLAPSED;
		else if (label.compareTo("Unfold") 	== 0) myState = JGVisual.State.UNFOLDED;
	}

	public void actionPerformed(ActionEvent e) {		
		caller.setState(myState);			
	}
	
	public GoggleAction clone(String label) {
		return new SetCollapseAction(caller, label);		
	}
}

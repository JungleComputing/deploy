package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.exceptions.StatNotRequestedException;
import ibis.deploy.gui.gridvision.visuals.VisualElementInterface;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ToggleMetricAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	String label;
	VisualElementInterface caller;
	
	public ToggleMetricAction(VisualElementInterface caller, String label) {
		super(label);
		this.caller = caller;
		this.label = label;		
	}

	public void actionPerformed(ActionEvent e) {		
		try {
			caller.toggleMetricShown(label);
		} catch (StatNotRequestedException ex) {
			ex.printStackTrace();
		}		
	}
}

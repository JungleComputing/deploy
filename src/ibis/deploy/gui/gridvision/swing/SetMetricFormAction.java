package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;
import ibis.deploy.gui.gridvision.visuals.VisualElementInterface;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetMetricFormAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	int button;
	VisualElementInterface caller;
	
	public SetMetricFormAction(VisualElementInterface caller, String label) {
		super(label);
		this.caller = caller;
		
		if (label.compareTo("Bars") == 0) button = VisualElementInterface.METRICS_BAR;
		else if (label.compareTo("Tubes") == 0) button = VisualElementInterface.METRICS_TUBE;
		else if (label.compareTo("Spheres") == 0) button = VisualElementInterface.METRICS_SPHERE;
	}

	public void actionPerformed(ActionEvent e) {		
		try {
			caller.setForm(button);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}		
	}
}

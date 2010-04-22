package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.newtry.Vrarchy.Vmetric;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetElementsAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	PerfVis perfvis;
	int myElements;
	
	public SetElementsAction(PerfVis perfvis, String label) {
		super(label);
		this.perfvis = perfvis;
		
		if (label.compareTo("Bars") == 0) myElements = Vmetric.BAR;
		else if (label.compareTo("Tubes") == 0) myElements = Vmetric.TUBE;
		else if (label.compareTo("Spheres") == 0) myElements = Vmetric.SPHERE;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			perfvis.setElementForm(myElements);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}
	}
}

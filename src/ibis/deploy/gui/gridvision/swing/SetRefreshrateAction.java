package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.PerfVis;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetRefreshrateAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	PerfVis perfvis;
	int myRefreshrate;
	
	public SetRefreshrateAction(PerfVis perfvis, String label) {
		super(label);
		this.perfvis = perfvis;
		
		myRefreshrate = Integer.parseInt(label);
	}

	public void actionPerformed(ActionEvent e) {	
		perfvis.setRefreshrate(myRefreshrate);
	}
}

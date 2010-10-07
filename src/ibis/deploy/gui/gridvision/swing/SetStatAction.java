package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.PerfVis;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetStatAction extends AbstractAction {	
	private static final long serialVersionUID = 7380908030018875303L;
	
	PerfVis perfvis;
	int myStat;
	
	public SetStatAction(PerfVis perfvis, String label) {
		super(label);
		this.perfvis = perfvis;
		
		/*
		if (label.compareTo("All") == 0) 				myStat = PerfVis.STAT_ALL;
		else if (label.compareTo("CPU Usage") == 0) 	myStat = PerfVis.STAT_CPU;
		else if (label.compareTo("Memory Usage") == 0) 	myStat = PerfVis.STAT_MEM;
		*/
	}

	public void actionPerformed(ActionEvent e) {
		/*
		try {
			perfvis.setStat(myStat);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}
		*/
	}
}

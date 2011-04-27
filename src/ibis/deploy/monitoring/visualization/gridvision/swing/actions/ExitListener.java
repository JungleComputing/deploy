package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel.TweakState;

import java.awt.event.ItemEvent;


public class ExitListener implements GoggleListener {	
	GogglePanel gp;
	
	public ExitListener(GogglePanel gp) {		
		this.gp = gp;		
	}

	public void itemStateChanged(ItemEvent arg0) {
		gp.setTweakState(TweakState.NONE);
	}
	
	public GoggleListener clone(String label) {
		return new ExitListener(gp);		
	}

	
}

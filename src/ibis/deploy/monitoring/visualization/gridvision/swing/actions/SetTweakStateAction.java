package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import java.awt.event.ActionEvent;

import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel.TweakState;

public class SetTweakStateAction implements GoggleAction {
	private static final long serialVersionUID = 7987449048219770239L;
	
	GogglePanel gogglePanel;
	TweakState state;
	
	public SetTweakStateAction(GogglePanel gogglePanel, String label) {
		this.gogglePanel = gogglePanel;
		
		if (label.compareTo("None") 		== 0) state = TweakState.NONE;
		else if (label.compareTo("Gathering") == 0) state = TweakState.GATHERING;
		else if (label.compareTo("Metrics") == 0) state = TweakState.METRICS;
		else if (label.compareTo("Network") == 0) state = TweakState.NETWORK;
		else if (label.compareTo("Visual") == 0) state = TweakState.VISUAL;
	}

	public void actionPerformed(ActionEvent e) {		
		gogglePanel.setTweakState(state);
	}
	
	public GoggleAction clone(String label) {
		return new SetTweakStateAction(gogglePanel, label);		
	}
}

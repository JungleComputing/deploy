package ibis.deploy.monitoring.visualization.gridvision.swing.listeners;

import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;

import java.awt.event.ItemEvent;


public class ParentSkipListener implements GoggleListener {	
	JungleGoggles goggles;
	MetricDescription[] myDescs;
	
	public ParentSkipListener(JungleGoggles goggles) {		
		this.goggles = goggles;
	}

	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.DESELECTED || arg0.getStateChange() == ItemEvent.SELECTED) {
			goggles.toggleParentSkip();
		}
	}
	
	public GoggleListener clone(String label) {
		return new ParentSkipListener(goggles);		
	}

	
}

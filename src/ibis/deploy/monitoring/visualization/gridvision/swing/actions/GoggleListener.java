package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public interface GoggleListener extends ItemListener {
	public void itemStateChanged(ItemEvent arg0);
	public GoggleListener clone(String newLabel);
}

package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public interface GoggleAction extends ActionListener {
	public void actionPerformed(ActionEvent e);
	public GoggleAction clone(String newLabel);
}

package ibis.deploy.vizFramework;

import java.awt.BorderLayout;

import ibis.deploy.monitoring.collection.Collector;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JPanel;

public class GlobePanel extends JPanel {
	private static final long serialVersionUID = 4754345291079348455L;

	GLJPanel gljpanel;

	public GlobePanel(final Collector collector) {
		setLayout(new BorderLayout(0, 0));

		// Set up the window
		add(gljpanel);

		gljpanel.requestFocusInWindow();
	}
	
	public GLJPanel getPanel() {
		return gljpanel;
	}
}

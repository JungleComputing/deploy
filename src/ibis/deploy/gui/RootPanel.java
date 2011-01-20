package ibis.deploy.gui;

import ibis.deploy.gui.applications.ApplicationEditorPanel;
import ibis.deploy.gui.clusters.ClusterEditorPanel;
import ibis.deploy.gui.deployViz.DeployVizPanel;
import ibis.deploy.gui.experiment.ExperimentsPanel;
import ibis.deploy.gui.gridvision.GridVisionPanel;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootPanel extends JPanel {
	private static Logger logger = LoggerFactory.getLogger(RootPanel.class);

	JTabbedPane tabs;

	DetachableTab experimentTab;
	DetachableTab applicationTab;
	DetachableTab clusterTab;
	DetachableTab smartSocketsTab;
	DetachableTab gridVisionTab;
	DetachableTab deployVizTab;

	private static final long serialVersionUID = 2685960743908025422L;

	public RootPanel(GUI gui) throws Exception {
		setLayout(new BorderLayout());
		tabs = new JTabbedPane();

		experimentTab = new DetachableTab("Experiments",
				"images/utilities-system-monitor.png",
				new ExperimentsPanel(gui), tabs);

		if (!gui.isReadOnly()) {
			applicationTab = new DetachableTab("Applications",
					"images/applications-other.png",
					new ApplicationEditorPanel(gui), tabs);

			clusterTab = new DetachableTab("Clusters",
					"images/network-transmit-receive.png",
					new ClusterEditorPanel(gui), tabs);

		}

		smartSocketsTab = new DetachableTab("Network Overlay",
				"images/gridvision.png",
				new SmartsocketsViz(Color.BLACK, Color.WHITE, false, gui
						.getDeploy().getRootHubAddress()), tabs);

		gridVisionTab = new DetachableTab("3D Visualization",
				"images/gridvision.png", new GridVisionPanel(gui), tabs);

		deployVizTab = new DetachableTab("Connection Overview",
				"images/gridvision.png", new DeployVizPanel(gui), tabs);

		add(tabs, BorderLayout.CENTER);
	}
}

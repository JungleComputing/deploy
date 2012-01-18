package ibis.deploy.gui;

import ibis.deploy.gui.applications.ApplicationEditorPanel;
import ibis.deploy.gui.deployViz.DeployVizPanel;
import ibis.deploy.gui.experiment.ExperimentsPanel;
import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.resources.ResourceEditorPanel;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RootPanel extends JPanel {
    JTabbedPane tabs;

    private static final long serialVersionUID = 2685960743908025422L;

    public RootPanel(GUI gui, String[] logos) throws Exception {
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();

        tabs.addTab("Experiments", Utils.createImageIcon(
                "images/utilities-system-monitor.png", "Experiments Tab"),
                new ExperimentsPanel(gui, logos));

        if (gui.getMode() == Mode.NORMAL) {
            tabs.addTab("Applications", Utils.createImageIcon(
                    "images/applications-other.png", "Applications Tab"),
                    new ApplicationEditorPanel(gui));

            tabs.addTab("Jungle", Utils.createImageIcon(
                    "images/network-transmit-receive.png", "Jungle Tab"),
                    new ResourceEditorPanel(gui));
        }

        tabs.addTab("Network Overlay", Utils.createImageIcon(
                "images/gridvision.png", "Network Overlay"),
                new SmartsocketsViz(Color.BLACK, Color.WHITE, false, true,
                        false, gui.getDeploy().getRootHubAddress()));

        if (gui.isMonitoringEnabled()) {
            tabs.addTab("Connection Overview", Utils.createImageIcon(
                    "images/gridvision.png", "Connection Overview Tab"),
                    new DeployVizPanel(gui));

            tabs.addTab("3D Visualization", Utils.createImageIcon(
                    "images/gridvision.png", "3D Visualization Tab"),
                    new GogglePanel(gui, gui.getCollector()));
        }

        add(tabs, BorderLayout.CENTER);
    }
}

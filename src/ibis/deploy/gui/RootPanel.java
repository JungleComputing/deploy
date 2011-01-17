package ibis.deploy.gui;

import ibis.deploy.gui.applications.ApplicationEditorPanel;
import ibis.deploy.gui.clusters.ClusterEditorPanel;
import ibis.deploy.gui.deployViz.DeployVizPanel;
import ibis.deploy.gui.experiment.ExperimentsPanel;
import ibis.deploy.gui.gridvision.GridVisionPanel;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RootPanel extends JPanel {
    JTabbedPane tabs;
   
    private static final long serialVersionUID = 2685960743908025422L;

    public RootPanel(GUI gui) {
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();

        tabs.addTab("Experiment", Utils.createImageIcon(
                "images/utilities-system-monitor.png", "Experiment"),
                new ExperimentsPanel(gui));
        // tabs.setTabComponentAt(0, new TabTitlePanel("Experiment", Utils
        // .createImageIcon("images/utilities-system-monitor.png",
        // "Experiment Tab")));
        if (!gui.isReadOnly()) {
            tabs.addTab("Applications", Utils.createImageIcon(
                    "images/applications-other.png", "Applications Tab"),
                    new ApplicationEditorPanel(gui));

            tabs.addTab("Clusters", Utils.createImageIcon(
                    "images/network-transmit-receive.png", "Clusters Tab"),
                    new ClusterEditorPanel(gui));

        }

        add(tabs, BorderLayout.CENTER);
    }

    public void toggleGridVisionPane(GUI gui, GridVisionPanel panel) {
        Component[] comps = tabs.getComponents();
        boolean present = false;
        for (Component comp : comps) {
            if (comp == panel) {
                present = true;
            }
        }

        if (!present) {
            tabs.addTab("GridVision", Utils.createImageIcon(
                    "images/gridvision.png", "GridVision Tab"), panel);
            panel.initialize(gui);
        }
    }

    public void toggleDeployVizPane(GUI gui, DeployVizPanel deployVizPanel) {
        Component[] comps = tabs.getComponents();
        boolean present = false;
        for (Component comp : comps) {
            if (comp == deployVizPanel) {
                present = true;
            }
        }

        if (!present) {
            tabs.addTab("DeployViz", Utils.createImageIcon(
                    "images/gridvision.png", "Clusters Tab"), deployVizPanel);
            deployVizPanel.setCollectData(true);
        } else {
            deployVizPanel.setCollectData(false);
            int idx = tabs.indexOfComponent(deployVizPanel);
            tabs.remove(deployVizPanel);
            tabs.removeTabAt(idx);
        }
    }

}

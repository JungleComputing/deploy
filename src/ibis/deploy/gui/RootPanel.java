package ibis.deploy.gui;

import ibis.deploy.gui.applications.ApplicationEditorPanel;
import ibis.deploy.gui.clusters.ClusterEditorPanel;
import ibis.deploy.gui.deployViz.DeployVizPanel;
import ibis.deploy.gui.experiment.ExperimentsPanel;
import ibis.deploy.gui.gridvision.GridVisionPanel;
import ibis.deploy.gui.misc.Utils;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RootPanel extends JPanel {
    JTabbedPane tabs;

    DetachableTab applicationTab;
    DetachableTab clusterTab;
    DetachableTab smartSocketsTab;
    DetachableTab gridVisionTab;
    DetachableTab deployVizTab;

    private static final long serialVersionUID = 2685960743908025422L;

    public RootPanel(GUI gui) throws Exception {
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();
        
        tabs.addTab("Experiments", Utils.createImageIcon("images/utilities-system-monitor.png", "Experiments Tab"), new ExperimentsPanel(gui));
        
//        experimentTab = new DetachableTab("Experiments",
//                "images/utilities-system-monitor.png",
//                new ExperimentsPanel(gui), tabs);

        if (!gui.isReadOnly()) {
            applicationTab = new DetachableTab("Applications",
                    "images/applications-other.png",
                    new ApplicationEditorPanel(gui), tabs);

            clusterTab = new DetachableTab("Clusters",
                    "images/network-transmit-receive.png",
                    new ClusterEditorPanel(gui), tabs);
        }

        smartSocketsTab = new DetachableTab("Network Overlay",
                "images/gridvision.png", new SmartsocketsViz(gui.getDeploy()
                        .getRootHubAddress()), tabs);

        gridVisionTab = new DetachableTab("3D Visualization",
                "images/gridvision.png", new GridVisionPanel(gui), tabs);

        deployVizTab = new DetachableTab("Connection Overview",
                "images/gridvision.png", new DeployVizPanel(gui), tabs);

        add(tabs, BorderLayout.CENTER);
    }
    
    
}

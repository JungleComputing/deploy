package ibis.deploy.gui;

import ibis.deploy.gui.applications.ApplicationEditorPanel;
import ibis.deploy.gui.clusters.ClusterEditorPanel;
import ibis.deploy.gui.experiment.ExperimentsPanel;
import ibis.deploy.gui.misc.Utils;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;
import ibis.deploy.vizFramework.IVisualization;
import ibis.deploy.vizFramework.MetricManager;
import ibis.deploy.vizFramework.bundles.BundlesVisualization;
import ibis.deploy.vizFramework.globeViz.GlobePanel;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RootPanel extends JPanel {
    JTabbedPane tabs;

    DetachableTab applicationTab;
    DetachableTab clusterTab;
    DetachableTab smartSocketsTab;
    DetachableTab gridVisionTab;
    DetachableTab deployVizTab;
    DetachableTab globeTab;

    private static final long serialVersionUID = 2685960743908025422L;

    public RootPanel(GUI gui, String[] logos) throws Exception {
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();

        tabs.addTab("Experiments", Utils.createImageIcon(
                "images/utilities-system-monitor.png", "Experiments Tab"),
                new ExperimentsPanel(gui, logos));

        // experimentTab = new DetachableTab("Experiments",
        // "images/utilities-system-monitor.png",
        // new ExperimentsPanel(gui), tabs);

        if (!gui.isReadOnly()) {
            applicationTab = new DetachableTab("Applications",
                    "images/applications-other.png",
                    new ApplicationEditorPanel(gui), tabs);

            clusterTab = new DetachableTab("Clusters",
                    "images/network-transmit-receive.png",
                    new ClusterEditorPanel(gui), tabs);
        }

        smartSocketsTab = new DetachableTab("Network Overlay",
                "images/gridvision.png", new SmartsocketsViz(Color.BLACK,
                        Color.WHITE, false, true, false, gui.getDeploy()
                                .getRootHubAddress()), tabs);

//        if (!gui.isReadOnly()) {
//            gridVisionTab = new DetachableTab("3D Visualization",
//                    "images/gridvision.png",
//                    new GogglePanel(gui.getCollector()), tabs);
//        }

        BundlesVisualization bundlePanel = new BundlesVisualization(gui);

        deployVizTab = new DetachableTab("Connection Overview",
                "images/gridvision.png", bundlePanel, tabs);

        // GlobeVisualization globe = new GlobeVisualization();
        GlobePanel globePanel = new GlobePanel(gui);

        globeTab = new DetachableTab("Global Overview",
                "images/gridvision.png", globePanel, tabs);

        ArrayList<IVisualization> visualizations = new ArrayList<IVisualization>();
        visualizations.add(globePanel.getGlobe());
        visualizations.add(bundlePanel);

        MetricManager mgr = new MetricManager(gui.getCollector(),
                visualizations);
        add(tabs, BorderLayout.CENTER);
    }
}

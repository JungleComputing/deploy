package ibis.deploy.gui;

import ibis.deploy.gui.applications.ApplicationEditorPanel;
import ibis.deploy.gui.clusters.ClusterEditorPanel;
import ibis.deploy.gui.experiment.ExperimentsPanel;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RootPanel extends JPanel {

    private static class TabTitlePanel extends JPanel {

        private static final long serialVersionUID = -396708441003040950L;

        public TabTitlePanel(String name, ImageIcon icon) {
            setOpaque(false);
            add(new JLabel(icon));
            add(new JLabel(name));
        }

    }

    private static final long serialVersionUID = 2685960743908025422L;

    public RootPanel(GUI gui) {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.add(new ExperimentsPanel(gui));
        tabs.setTabComponentAt(0, new TabTitlePanel("Experiment", Utils
                .createImageIcon("images/utilities-system-monitor.png",
                        "Experiment Tab")));
        tabs.add(new ApplicationEditorPanel(gui));
        tabs.setTabComponentAt(1, new TabTitlePanel("Applications", Utils
                .createImageIcon("images/applications-other.png",
                        "Applications Tab")));
        tabs.add(new ClusterEditorPanel(gui));
        tabs.setTabComponentAt(2, new TabTitlePanel("Clusters", Utils
                .createImageIcon("images/network-transmit-receive.png",
                        "Clusters Tab")));
        add(tabs, BorderLayout.CENTER);

    }

}

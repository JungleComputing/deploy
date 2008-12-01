package ibis.deploy.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RootPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2685960743908025422L;

    public RootPanel(GUI gui) {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.add(new ExperimentsPanel(gui));
        tabs.setTabComponentAt(0, new TabTitlePanel("Experiment", GUIUtils
                .createImageIcon("images/utilities-system-monitor.png",
                        "Experiment Tab")));
        tabs.add(new ApplicationEditorPanel(gui));
        tabs.setTabComponentAt(1, new TabTitlePanel("Applications", GUIUtils
                .createImageIcon("images/applications-other.png",
                        "Applications Tab")));
        tabs.add(new ClusterEditorPanel(gui));
        tabs.setTabComponentAt(2, new TabTitlePanel("Clusters", GUIUtils
                .createImageIcon("images/network-transmit-receive.png",
                        "Clusters Tab")));
        add(tabs, BorderLayout.CENTER);

    }

}

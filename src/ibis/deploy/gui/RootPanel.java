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
        add(new ToolBar(gui), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("experiments", new ExperimentsPanel(gui));
        tabs.add("applications", new ApplicationEditorPanel(gui));
        tabs.add("clusters", new ClusterEditorPanel(gui));
        add(tabs, BorderLayout.CENTER);

    }

    // menu bar

    // tool bar (with icons)

    // tabbed pane

}

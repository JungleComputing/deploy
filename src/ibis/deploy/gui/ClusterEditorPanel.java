package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

public class ClusterEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 4896930998856242792L;
    
    private final List<EditorListener> clusterListeners = new ArrayList<EditorListener>();

    public ClusterEditorPanel(GUI gui) {
        setLayout(new BorderLayout());
        JTabbedPane clusterTabs = new JTabbedPane();
        ClusterListPanel applicationListPanel = new ClusterListPanel(
                gui, clusterTabs, this);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                applicationListPanel, clusterTabs);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }

    public void addEditorListener(EditorListener editorListener) {
        clusterListeners.add(editorListener);
    }
    
    public void fireClusterEdited(Cluster cluster) {
        for (EditorListener listener : clusterListeners) {
            listener.edited(cluster);
        }
    }
}

package ibis.deploy.gui.clusters;

import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.editor.EditorListener;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ClusterEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 4896930998856242792L;

    private final List<EditorListener> clusterListeners = new ArrayList<EditorListener>();

    public ClusterEditorPanel(GUI gui) {
        setLayout(new BorderLayout());
        // JTabbedPane clusterTabs = new JTabbedPane();
        JPanel editPanel = new JPanel(new BorderLayout());
        ClusterListPanel clusterListPanel = new ClusterListPanel(gui,
                editPanel, this);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                clusterListPanel, editPanel);
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

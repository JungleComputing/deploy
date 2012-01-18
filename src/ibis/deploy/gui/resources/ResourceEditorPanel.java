package ibis.deploy.gui.resources;

import ibis.deploy.Resource;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.editor.EditorListener;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ResourceEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 4896930998856242792L;

    private final List<EditorListener> resourceListeners = new ArrayList<EditorListener>();

    public ResourceEditorPanel(GUI gui) {
        setLayout(new BorderLayout());
        JPanel editPanel = new JPanel(new BorderLayout());
        ResourceListPanel resourceListPanel = new ResourceListPanel(gui,
                editPanel, this);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                resourceListPanel, editPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }

    public void addEditorListener(EditorListener editorListener) {
        resourceListeners.add(editorListener);
    }

    public void fireResourceEdited(Resource resource) {
        for (EditorListener listener : resourceListeners) {
            listener.edited(resource);
        }
    }
}

package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

public class ApplicationEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 664252233473989876L;

    private final List<EditorListener> applicationListeners = new ArrayList<EditorListener>();

    private GUI gui;

    public ApplicationEditorPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        JTabbedPane applicationTabs = new JTabbedPane();
        ApplicationListPanel applicationListPanel = new ApplicationListPanel(
                gui, applicationTabs, this);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                applicationListPanel, applicationTabs);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }

    public void addEditorListener(EditorListener editorListener) {
        applicationListeners.add(editorListener);
    }

    public void fireApplicationEdited(Application application) {
        if (application == gui.getApplicationSet().getDefaults()) {
            // TODO special code that updates each open tab!
            System.out.println("TODO special code that updates each open tab!");
            return;
        }
        for (EditorListener listener : applicationListeners) {
            listener.edited(application);
        }
    }

}

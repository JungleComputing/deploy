package ibis.deploy.gui.applications;

import ibis.deploy.Application;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.editor.EditorListener;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ApplicationEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 664252233473989876L;

    private final List<EditorListener> applicationListeners = new ArrayList<EditorListener>();

    //private GUI gui;

    private JSplitPane splitPane;

    public ApplicationEditorPanel(GUI gui) {
        //this.gui = gui;
        setLayout(new BorderLayout());
        JPanel editPanel = new JPanel(new BorderLayout());
        ApplicationListPanel applicationListPanel = new ApplicationListPanel(
                gui, editPanel, this);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                applicationListPanel, editPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }

    public void addEditorListener(EditorListener editorListener) {
        applicationListeners.add(editorListener);
    }

    public void fireApplicationEdited(Application application) {
//        if (application == gui.getApplicationSet().getDefaults()) {
//            // TODO special code that updates each open tab!
//            System.out.println("TODO special code that updates each open tab!");
//            return;
//        }
        for (EditorListener listener : applicationListeners) {
            listener.edited(application);
        }
    }

}

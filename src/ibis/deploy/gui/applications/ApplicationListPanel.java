package ibis.deploy.gui.applications;

import ibis.deploy.Application;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.editor.EditorListener;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ApplicationListPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7171659896010561242L;

    //private static final String DEFAULTS = "<html><i>defaults</i></html>";

    private ApplicationEditorPanel applicationEditorPanel;

    public ApplicationListPanel(final GUI gui, final JPanel editPanel,
            final ApplicationEditorPanel applicationEditorPanel) {

        this.applicationEditorPanel = applicationEditorPanel;

        setLayout(new BorderLayout());
        final DefaultListModel model = new DefaultListModel();
        final JList applicationList = new JList(model);
        applicationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final HashMap<Application, JPanel> editApplicationPanels = new HashMap<Application, JPanel>();

        applicationList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                final Application selectedApplication = (Application) applicationList
                        .getSelectedValue();
                editPanel.removeAll();
                editPanel.add(editApplicationPanels.get(selectedApplication),
                        BorderLayout.CENTER);
                editPanel.getRootPane().repaint();
            }
        });

        applicationEditorPanel.addEditorListener(new EditorListener() {

            public void edited(Object object) {
                applicationList.repaint();
            }

        });

        add(new ApplicationListTopPanel(gui, applicationList,
                editApplicationPanels, applicationEditorPanel),
                BorderLayout.NORTH);

        add(new JScrollPane(applicationList), BorderLayout.CENTER);
        
        this.applicationEditorPanel = applicationEditorPanel;

        init(gui, model, editApplicationPanels);

        gui.addApplicationSetWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                init(gui, model, editApplicationPanels);
            }

        });

    }

    private void init(GUI gui, DefaultListModel model,
            Map<Application, JPanel> editApplicationPanels) {

        model.clear();

        for (Application application : gui.getApplicationSet()
                .getApplications()) {
            model.addElement(application);
        }

        // create a hash map with all the panels
        editApplicationPanels.clear();
        for (Application application : gui.getApplicationSet()
                .getApplications()) {
            editApplicationPanels.put(application,
                    new ApplicationEditorTabPanel(application,
                            applicationEditorPanel, gui));
        }

    }
}

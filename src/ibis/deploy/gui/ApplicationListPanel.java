package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

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

    private static final String DEFAULTS = "<html><i>defaults</i></html>";

    public ApplicationListPanel(final GUI gui, final JPanel editPanel,
            final ApplicationEditorPanel applicationEditorPanel) {

        setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        try {
            gui.getApplicationSet().getDefaults().setName(DEFAULTS);
        } catch (Exception e) {
            // ignore
        }
        model.addElement(gui.getApplicationSet().getDefaults());

        for (Application application : gui.getApplicationSet()
                .getApplications()) {
            model.addElement(application);
        }

        final JList applicationList = new JList(model);

        // create a hash map with all the panels
        final HashMap<Application, JPanel> editApplicationPanels = new HashMap<Application, JPanel>();
        editApplicationPanels.put(gui.getApplicationSet().getDefaults(),
                new ApplicationEditorTabPanel(gui.getApplicationSet()
                        .getDefaults(), applicationEditorPanel, gui, true));
        for (Application application : gui.getApplicationSet()
                .getApplications()) {
            editApplicationPanels.put(application,
                    new ApplicationEditorTabPanel(application,
                            applicationEditorPanel, gui, false));
        }
        add(new ApplicationListTopPanel(gui, applicationList,
                editApplicationPanels, applicationEditorPanel),
                BorderLayout.NORTH);

        applicationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(applicationList), BorderLayout.CENTER);

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

    }
}

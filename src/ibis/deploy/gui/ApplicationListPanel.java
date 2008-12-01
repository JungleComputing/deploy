package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

public class ApplicationListPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7171659896010561242L;

    public ApplicationListPanel(GUI gui, final JTabbedPane applicationTabs,
            final ApplicationEditorPanel applicationEditorPanel) {

        setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        for (Application application : gui.getApplicationSet()
                .getApplications()) {
            model.addElement(application);
        }
        final JList applicationList = new JList(model);
        add(new ApplicationListTopPanel(gui, applicationList, applicationTabs,
                applicationEditorPanel), BorderLayout.NORTH);

        applicationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(applicationList), BorderLayout.CENTER);

        applicationList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    for (int i = 0; i < applicationTabs.getTabCount(); i++) {
                        if (applicationTabs.getTabComponentAt(i).getName()
                                .equals(
                                        ((Application) applicationList
                                                .getSelectedValue()).getName())) {
                            applicationTabs.setSelectedIndex(i);
                            return;
                        }
                    }
                    final Application selectedApplication = (Application) applicationList
                            .getSelectedValue();
                    ApplicationEditorTabPanel newTab = new ApplicationEditorTabPanel(
                            selectedApplication, applicationEditorPanel);

                    final ClosableTabTitlePanel titlePanel = new ClosableTabTitlePanel(
                            selectedApplication.getName(), applicationTabs);

                    applicationEditorPanel
                            .addEditorListener(new EditorListener() {

                                public void edited(Object object) {
                                    // only change text if the application of
                                    // this tab has changed
                                    if (object == selectedApplication) {
                                        titlePanel
                                                .setText(((Application) object)
                                                        .getName());
                                    }
                                }

                            });

                    applicationTabs.addTab(null, newTab);
                    applicationTabs.setTabComponentAt(applicationTabs
                            .getTabCount() - 1, titlePanel);
                    applicationTabs.setSelectedComponent(newTab);
                }
            }
        });

        applicationEditorPanel.addEditorListener(new EditorListener() {

            public void edited(Object object) {
                applicationList.repaint();
            }

        });

    }
}

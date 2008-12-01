package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ApplicationListTopPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6183574615247336429L;

    public ApplicationListTopPanel(final GUI gui, final JList applicationList,
            final JTabbedPane applicationTabs,
            final ApplicationEditorPanel applicationEditorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Applications");
        // label.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add(label);
        JPanel buttonPanel = new JPanel();
        JButton addButton = GUIUtils.createImageButton(
                "images/list-add-small.png", "add application", null);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    ((DefaultListModel) applicationList.getModel())
                            .addElement(gui.getApplicationSet()
                                    .createNewApplication("New-Application"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gui.fireApplicationSetUpdated();
            }

        });
        buttonPanel.add(addButton);
        JButton removeButton = GUIUtils.createImageButton(
                "images/list-remove-small.png", "remove application", null);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (applicationList.getSelectedIndex() >= 0) {
                        Application selectedApplication = (Application) ((DefaultListModel) applicationList
                                .getModel()).get(applicationList
                                .getSelectedIndex());
                        gui.getApplicationSet().removeApplication(
                                selectedApplication);
                        ((DefaultListModel) applicationList.getModel())
                                .removeElementAt(applicationList
                                        .getSelectedIndex());
                        // close any open tab of the removed application
                        for (int i = 0; i < applicationTabs.getTabCount(); i++) {
                            if (applicationTabs.getTabComponentAt(i).getName()
                                    .equals(selectedApplication.getName())) {
                                applicationTabs.removeTabAt(i);
                                // there's at max one open tab so break the loop
                                break;
                            }
                        }
                        gui.fireApplicationSetUpdated();
                    }
                } catch (Exception e) {
                    // ignore name is never null
                }
                gui.fireApplicationSetUpdated();
            }

        });

        buttonPanel.add(removeButton);

        final String defaultsString = "<html><i>defaults</i></html>";

        JButton editDefaults = GUIUtils
                .createImageButton("images/document-properties.png",
                        "edit default settings", null);
        editDefaults.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < applicationTabs.getTabCount(); i++) {
                    if (applicationTabs.getTabComponentAt(i).getName().equals(
                            (defaultsString))) {
                        applicationTabs.setSelectedIndex(i);
                        return;
                    } 
                }

                ApplicationEditorTabPanel newTab = new ApplicationEditorTabPanel(
                        gui.getApplicationSet().getDefaults(),
                        applicationEditorPanel, true);

                final ClosableTabTitlePanel titlePanel = new ClosableTabTitlePanel(
                        defaultsString, applicationTabs);

                applicationTabs.addTab(null, newTab);
                applicationTabs.setTabComponentAt(
                        applicationTabs.getTabCount() - 1, titlePanel);
                applicationTabs.setSelectedComponent(newTab);

            }
        });
        buttonPanel.add(editDefaults);

        add(buttonPanel);
    }
}

package ibis.deploy.gui.applications;

import ibis.deploy.Application;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.misc.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

public class ApplicationListTopPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6183574615247336429L;

    private static long ID_COUNTER = 0;

    public ApplicationListTopPanel(final GUI gui, final JList applicationList,
            final HashMap<Application, JPanel> editApplicationPanels,
            final ApplicationEditorPanel applicationEditorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Applications");
        // label.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add(label);
        JPanel buttonPanel = new JPanel();
        JButton addButton = Utils.createImageButton(
                "images/list-add-small.png", "Add application",
                "Add");
        addButton.setPreferredSize(new Dimension(Utils.buttonWidth, addButton
                .getPreferredSize().height));
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    String appName = "New-Application-" + ID_COUNTER;
                    ID_COUNTER++;
                    while (gui.getApplicationSet().hasApplication(appName)) {
                        appName = "New-Application-" + ID_COUNTER;
                        ID_COUNTER++;
                    }

                    Application newApplication = new Application(appName);
                    gui.getApplicationSet().addApplication(newApplication);
                    
                    ((DefaultListModel) applicationList.getModel())
                            .addElement(newApplication);
                    editApplicationPanels.put(newApplication,
                            new ApplicationEditorTabPanel(newApplication,
                                    applicationEditorPanel, gui));
                } catch (Exception e) {
                }
                gui.fireApplicationSetUpdated();
            }

        });
        buttonPanel.add(addButton);
        JButton removeButton = Utils.createImageButton(
                "images/list-remove-small.png", "Remove selected application",
                "Remove");
        removeButton.setPreferredSize(new Dimension(Utils.buttonWidth, removeButton.getPreferredSize().height));
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    // make sure that something is selected
                    if (applicationList.getSelectedIndex() >= 0) {
                        Application selectedApplication = (Application) ((DefaultListModel) applicationList
                                .getModel()).get(applicationList
                                .getSelectedIndex());
                        (editApplicationPanels.get(selectedApplication))
                                .getParent().remove(
                                        editApplicationPanels
                                                .get(selectedApplication));
                        editApplicationPanels.remove(selectedApplication);
                        gui.getApplicationSet().removeApplication(
                                selectedApplication);
                        ((DefaultListModel) applicationList.getModel())
                                .removeElementAt(applicationList
                                        .getSelectedIndex());
                        applicationEditorPanel.repaint();
                        gui.fireGridUpdated();
                    }
                } catch (Exception e) {
                    // ignore name is never null
                }
                gui.fireApplicationSetUpdated();
            }

        });

        buttonPanel.add(removeButton);

        add(buttonPanel);
    }
}

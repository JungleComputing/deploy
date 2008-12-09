package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ApplicationListTopPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6183574615247336429L;

    public ApplicationListTopPanel(final GUI gui, final JList applicationList,
            final HashMap<Application, JPanel> editApplicationPanels,
            final ApplicationEditorPanel applicationEditorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Applications");
        // label.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add(label);
        JPanel buttonPanel = new JPanel();
        JButton addButton = GUIUtils.createImageButton(
                "/images/list-add-small.png", "add cluster", null);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    Application newApplication = gui.getApplicationSet()
                            .createNewApplication("New-Application");
                    ((DefaultListModel) applicationList.getModel())
                            .addElement(newApplication);
                    editApplicationPanels.put(newApplication,
                            new ApplicationEditorTabPanel(newApplication,
                                    applicationEditorPanel, gui, false));
                } catch (Exception e) {
                }
                gui.fireGridUpdated();
            }

        });
        buttonPanel.add(addButton);
        JButton removeButton = GUIUtils.createImageButton(
                "/images/list-remove-small.png", "remove cluster", null);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    // defaults cannot be removed
                    if (applicationList.getSelectedIndex() >= 1) {
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
                    } else if (applicationList.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(getRootPane(),
                                "Cannot remove the defaults", "Error",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (Exception e) {
                    // ignore name is never null
                }
                gui.fireGridUpdated();
            }

        });

        buttonPanel.add(removeButton);

        add(buttonPanel);
    }
}

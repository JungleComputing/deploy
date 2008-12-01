package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ClusterListTopPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6183574615247336429L;

    public ClusterListTopPanel(final GUI gui, final JList clusterList,
            final JTabbedPane clusterTabs,
            final ClusterEditorPanel clusterEditorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Clusters");
        // label.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add(label);
        JPanel buttonPanel = new JPanel();
        JButton addButton = GUIUtils.createImageButton(
                "images/list-add-small.png", "add cluster", null);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    ((DefaultListModel) clusterList.getModel()).addElement(gui
                            .getGrid().createNewCluster("New-Cluster"));
                } catch (Exception e) {
                }
                gui.fireGridUpdated();
            }

        });
        buttonPanel.add(addButton);
        JButton removeButton = GUIUtils.createImageButton(
                "images/list-remove-small.png", "remove cluster", null);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (clusterList.getSelectedIndex() >= 0) {
                        Cluster selectedCluster = (Cluster) ((DefaultListModel) clusterList
                                .getModel())
                                .get(clusterList.getSelectedIndex());
                        gui.getGrid().removeCluster(selectedCluster);
                        ((DefaultListModel) clusterList.getModel())
                                .removeElementAt(clusterList.getSelectedIndex());
                        // close any open tab of the removed application
                        for (int i = 0; i < clusterTabs.getTabCount(); i++) {
                            if (clusterTabs.getTabComponentAt(i).getName()
                                    .equals(selectedCluster.getName())) {
                                clusterTabs.removeTabAt(i);
                                // there's at max one open tab so break the loop
                                break;
                            }
                        }
                        gui.fireGridUpdated();
                    }
                } catch (Exception e) {
                    // ignore name is never null
                }
                gui.fireGridUpdated();
            }

        });

        buttonPanel.add(removeButton);

        final String defaultsString = "<html><i>defaults</i></html>";

        JButton editDefaults = GUIUtils
                .createImageButton("images/document-properties.png",
                        "edit default settings", null);
        editDefaults.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < clusterTabs.getTabCount(); i++) {
                    if (clusterTabs.getTabComponentAt(i).getName().equals(
                            (defaultsString))) {
                        clusterTabs.setSelectedIndex(i);
                        return;
                    }
                }

                ClusterEditorTabPanel newTab = new ClusterEditorTabPanel(gui
                        .getGrid().getDefaults(), clusterEditorPanel, gui, true);

                final ClosableTabTitlePanel titlePanel = new ClosableTabTitlePanel(
                        defaultsString, clusterTabs);

                clusterTabs.addTab(null, newTab);
                clusterTabs.setTabComponentAt(clusterTabs.getTabCount() - 1,
                        titlePanel);
                clusterTabs.setSelectedComponent(newTab);

            }
        });
        buttonPanel.add(editDefaults);
        add(buttonPanel);
    }
}

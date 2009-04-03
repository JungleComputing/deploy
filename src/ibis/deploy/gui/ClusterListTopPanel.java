package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ClusterListTopPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6183574615247336429L;

    public ClusterListTopPanel(final GUI gui, final JList clusterList,
            final HashMap<Cluster, JPanel> editClusterPanels,
            final ClusterEditorPanel clusterEditorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Clusters");
        // label.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add(label);
        JPanel buttonPanel = new JPanel();
        JButton addButton = GUIUtils.createImageButton(
                "/images/list-add-small.png", "add cluster", null);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    Cluster newCluster = gui.getGrid().createNewCluster(
                            "New-Cluster");
                    ((DefaultListModel) clusterList.getModel())
                            .addElement(newCluster);
                    editClusterPanels.put(newCluster,
                            new ClusterEditorTabPanel(newCluster,
                                    clusterEditorPanel, gui, false));
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
                    if (clusterList.getSelectedIndex() >= 1) {
                        Cluster selectedCluster = (Cluster) ((DefaultListModel) clusterList
                                .getModel())
                                .get(clusterList.getSelectedIndex());
                        (editClusterPanels.get(selectedCluster)).getParent()
                                .remove(editClusterPanels.get(selectedCluster));
                        editClusterPanels.remove(selectedCluster);
                        gui.getGrid().removeCluster(selectedCluster);
                        ((DefaultListModel) clusterList.getModel())
                                .removeElementAt(clusterList.getSelectedIndex());
                        clusterEditorPanel.repaint();
                        gui.fireGridUpdated();
                    } else if (clusterList.getSelectedIndex() == 0) {
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

package ibis.deploy.gui.clusters;

import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.misc.Utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

public class ClusterListTopPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6183574615247336429L;

    private static long ID_COUNTER = 0;

    public ClusterListTopPanel(final GUI gui, final JList clusterList,
            final HashMap<Cluster, JPanel> editClusterPanels,
            final ClusterEditorPanel clusterEditorPanel) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Clusters");
        // label.setAlignmentX(Component.CENTER_ALIGNMENT);
        // add(label);
        JPanel buttonPanel = new JPanel();
        JButton addButton = Utils.createImageButton(
                "images/list-add-small.png", "Add cluster", null);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    String clusterName = "New-Cluster-" + ID_COUNTER;
                    ID_COUNTER++;
                    while (gui.getGrid().hasCluster(clusterName)) {
                        clusterName = "New-Cluster-" + ID_COUNTER;
                        ID_COUNTER++;
                    }

                    Cluster newCluster = gui.getGrid().createNewCluster(
                            clusterName);
                    ((DefaultListModel) clusterList.getModel())
                            .addElement(newCluster);
                    editClusterPanels.put(newCluster,
                            new ClusterEditorTabPanel(newCluster,
                                    clusterEditorPanel, gui));
                } catch (Exception e) {
                }
                gui.fireGridUpdated();
            }

        });
        buttonPanel.add(addButton);
        JButton removeButton = Utils.createImageButton(
                "images/list-remove-small.png", "Remove cluster", null);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    // make sure that there is something selected in the list
                    if (clusterList.getSelectedIndex() >= 0) {
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

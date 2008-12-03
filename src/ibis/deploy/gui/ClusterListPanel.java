package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ClusterListPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7171659896010561242L;

    private static final String DEFAULTS = "<html><i>defaults</i></html>";

    public ClusterListPanel(final GUI gui, final JPanel editPanel,
            final ClusterEditorPanel clusterEditorPanel) {

        setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        try {
            gui.getGrid().getDefaults().setName(DEFAULTS);
        } catch (Exception e) {
            // ignore
        }
        model.addElement(gui.getGrid().getDefaults());

        for (Cluster cluster : gui.getGrid().getClusters()) {
            model.addElement(cluster);
        }
        final JList clusterList = new JList(model);
        // create a hash map with all the panels
        final HashMap<Cluster, JPanel> editClusterPanels = new HashMap<Cluster, JPanel>();
        editClusterPanels.put(gui.getGrid().getDefaults(),
                new ClusterEditorTabPanel(gui.getGrid().getDefaults(),
                        clusterEditorPanel, gui, true));
        for (Cluster cluster : gui.getGrid().getClusters()) {
            editClusterPanels.put(cluster, new ClusterEditorTabPanel(cluster,
                    clusterEditorPanel, gui, false));
        }

        add(new ClusterListTopPanel(gui, clusterList, editClusterPanels,
                clusterEditorPanel), BorderLayout.NORTH);

        clusterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(clusterList), BorderLayout.CENTER);

        clusterList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                final Cluster selectedCluster = (Cluster) clusterList
                        .getSelectedValue();
                editPanel.removeAll();
                editPanel.add(editClusterPanels.get(selectedCluster),
                        BorderLayout.CENTER);
                editPanel.getRootPane().repaint();
            }
        });

        clusterEditorPanel.addEditorListener(new EditorListener() {

            public void edited(Object object) {
                clusterList.repaint();
            }

        });

    }
}

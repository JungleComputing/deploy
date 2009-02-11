package ibis.deploy.gui;

import ibis.deploy.Cluster;
import ibis.deploy.gui.listener.EditorListener;
import ibis.deploy.gui.listener.WorkSpaceChangedListener;

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

public class ClusterListPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7171659896010561242L;

    private static final String DEFAULTS = "<html><i>defaults</i></html>";

    private ClusterEditorPanel clusterEditorPanel;

    public ClusterListPanel(final GUI gui, final JPanel editPanel,
            final ClusterEditorPanel clusterEditorPanel) {

        setLayout(new BorderLayout());
        final DefaultListModel model = new DefaultListModel();
        final JList clusterList = new JList(model);
        clusterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final HashMap<Cluster, JPanel> editClusterPanels = new HashMap<Cluster, JPanel>();

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

        add(new ClusterListTopPanel(gui, clusterList,
                editClusterPanels, clusterEditorPanel),
                BorderLayout.NORTH);

        add(new JScrollPane(clusterList), BorderLayout.CENTER);

        init(gui, model, editClusterPanels);

        gui.addGridWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                init(gui, model, editClusterPanels);
            }

        });

    }

    private void init(GUI gui, DefaultListModel model,
            Map<Cluster, JPanel> editClusterPanels) {

        try {
            gui.getGrid().getDefaults().setName(DEFAULTS);
        } catch (Exception e) {
            // ignore
        }
        model.clear();
        model.addElement(gui.getGrid().getDefaults());

        for (Cluster cluster : gui.getGrid()
                .getClusters()) {
            model.addElement(cluster);
        }

        // create a hash map with all the panels
        editClusterPanels.clear();
        editClusterPanels.put(gui.getGrid().getDefaults(),
                new ClusterEditorTabPanel(gui.getGrid()
                        .getDefaults(), clusterEditorPanel, gui, true));
        for (Cluster cluster : gui.getGrid()
                .getClusters()) {
            editClusterPanels.put(cluster,
                    new ClusterEditorTabPanel(cluster,
                            clusterEditorPanel, gui, false));
        }

    }
}

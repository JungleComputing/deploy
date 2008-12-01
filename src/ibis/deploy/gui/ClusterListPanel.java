package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

public class ClusterListPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7171659896010561242L;

    public ClusterListPanel(final GUI gui, final JTabbedPane clusterTabs,
            final ClusterEditorPanel clusterEditorPanel) {

        setLayout(new BorderLayout());
        DefaultListModel model = new DefaultListModel();
        for (Cluster cluster : gui.getGrid().getClusters()) {
            model.addElement(cluster);
        }
        final JList clusterList = new JList(model);
        add(new ClusterListTopPanel(gui, clusterList, clusterTabs,
                clusterEditorPanel), BorderLayout.NORTH);

        clusterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(clusterList), BorderLayout.CENTER);

        clusterList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    for (int i = 0; i < clusterTabs.getTabCount(); i++) {
                        if (clusterTabs.getTabComponentAt(i).getName().equals(
                                ((Cluster) clusterList.getSelectedValue())
                                        .getName())) {
                            clusterTabs.setSelectedIndex(i);
                            return;
                        }
                    }
                    final Cluster selectedCluster = (Cluster) clusterList
                            .getSelectedValue();
                    ClusterEditorTabPanel newTab = new ClusterEditorTabPanel(
                            selectedCluster, clusterEditorPanel, gui);

                    final ClosableTabTitlePanel titlePanel = new ClosableTabTitlePanel(
                            selectedCluster.getName(), clusterTabs);

                    clusterEditorPanel.addEditorListener(new EditorListener() {

                        public void edited(Object object) {
                            // only change text if the application of
                            // this tab has changed
                            if (object == selectedCluster) {
                                titlePanel
                                        .setText(((Cluster) object).getName());
                            }
                        }

                    });

                    clusterTabs.addTab(null, newTab);
                    clusterTabs.setTabComponentAt(
                            clusterTabs.getTabCount() - 1, titlePanel);
                    clusterTabs.setSelectedComponent(newTab);
                }
            }
        });

        clusterEditorPanel.addEditorListener(new EditorListener() {

            public void edited(Object object) {
                clusterList.repaint();
            }

        });

    }
}

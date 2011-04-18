package ibis.deploy.gui.experiment.composer;

import ibis.deploy.Cluster;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.WorldMapPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClusterSelectionPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1552888252030474929L;

    private final JComboBox clusterComboBox = new JComboBox();

    private ResourceCountPanel resourceCountPanel;

    public ClusterSelectionPanel(final GUI gui,
            final WorldMapPanel worldMapPanel) {
        gui.addGridWorkSpaceListener(new WorkSpaceChangedListener() {
            public void workSpaceChanged(GUI gui) {
                clusterComboBox.removeAllItems();
                for (Cluster cluster : gui.getGrid().getClusters()) {
                    clusterComboBox.addItem(cluster);
                }
                clusterComboBox.repaint();
            }
        });

        gui.addSubmitJobListener(new SubmitJobListener() {

            public void modify(JobDescription jobDescription) throws Exception {
                jobDescription.getCluster().setName(clusterComboBox.getSelectedItem()
                        .toString());
            }

        });

        // register by world map panel
        if (worldMapPanel != null) {
            worldMapPanel.registerClusterSelectionPanel(this);
        }

        setLayout(new BorderLayout());
        for (Cluster cluster : gui.getGrid().getClusters()) {
            clusterComboBox.addItem(cluster);
        }
        clusterComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                if (worldMapPanel != null) {
                    worldMapPanel.setSelected((Cluster) clusterComboBox
                            .getSelectedItem());
//                    resourceCountPanel.setResourceCount(worldMapPanel
//                            .getResourceCount());
                }

            }

        });
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(new JLabel("Select Cluster"), BorderLayout.WEST);
        titlePanel.add(Utils.createImageLabel("images/network-server.png",
                "resource count"), BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.add(clusterComboBox, BorderLayout.CENTER);
        resourceCountPanel = new ResourceCountPanel(gui, worldMapPanel);
        selectPanel.add(resourceCountPanel, BorderLayout.EAST);

        add(selectPanel, BorderLayout.SOUTH);

    }

    public void setSelected(Cluster cluster) {
        clusterComboBox.setSelectedItem(cluster);
    }

    public void setResourceCount(int i) {
        resourceCountPanel.setResourceCount(i);
    }
}

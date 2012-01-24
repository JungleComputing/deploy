package ibis.deploy.gui.experiment.composer;

import ibis.deploy.Resource;
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

public class ResourceSelectionPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1552888252030474929L;

    private final JComboBox resourceComboBox = new JComboBox();

    private ResourceCountPanel resourceCountPanel;

    public ResourceSelectionPanel(final GUI gui,
            final WorldMapPanel worldMapPanel) {
        gui.addJungleWorkSpaceListener(new WorkSpaceChangedListener() {
            public void workSpaceChanged(GUI gui) {
                resourceComboBox.removeAllItems();
                for (Resource resource : gui.getJungle().getResources()) {
                    resourceComboBox.addItem(resource);
                }
                resourceComboBox.repaint();
            }
        });

        gui.addSubmitJobListener(new SubmitJobListener() {

            public void modify(JobDescription jobDescription) throws Exception {
                jobDescription.getResource().setName(resourceComboBox.getSelectedItem()
                        .toString());
            }

        });

        // register by world map panel
        if (worldMapPanel != null) {
            worldMapPanel.registerResourceSelectionPanel(this);
        }

        setLayout(new BorderLayout());
        for (Resource resource : gui.getJungle().getResources()) {
            resourceComboBox.addItem(resource);
        }
        resourceComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                if (worldMapPanel != null) {
                    worldMapPanel.setSelected((Resource) resourceComboBox
                            .getSelectedItem());
//                    resourceCountPanel.setResourceCount(worldMapPanel
//                            .getResourceCount());
                }

            }

        });
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(new JLabel("Resource"), BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);

        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.add(Utils.createImageLabel("images/network-server.png",
                "resource count"), BorderLayout.WEST);
        selectPanel.add(resourceComboBox, BorderLayout.CENTER);
        resourceCountPanel = new ResourceCountPanel(gui, worldMapPanel);
        selectPanel.add(resourceCountPanel, BorderLayout.EAST);

        add(selectPanel, BorderLayout.SOUTH);

    }

    public void setSelected(Resource resource) {
        resourceComboBox.setSelectedItem(resource);
    }

    public void setResourceCount(int i) {
        resourceCountPanel.setResourceCount(i);
    }
}

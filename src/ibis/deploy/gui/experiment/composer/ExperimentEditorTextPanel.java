package ibis.deploy.gui.experiment.composer;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.worldmap.WorldMapPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class ExperimentEditorTextPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -5262692042637073227L;

    public ExperimentEditorTextPanel(GUI gui, JobTableModel model, WorldMapPanel worldMapPanel) {
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(new PoolNamePanel(gui));

        //separator
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        topPanel.add(new ApplicationSelectionPanel(gui));

        //separator
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        topPanel.add(new ResourceSelectionPanel(gui, worldMapPanel));

        //separator
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        topPanel.add(new RuntimePanel(gui), worldMapPanel);

        //separator
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        add(topPanel, BorderLayout.NORTH);
        
        //buttons on the bottom
        add(new RunButtonPanel(gui, model), BorderLayout.SOUTH);

    }

}

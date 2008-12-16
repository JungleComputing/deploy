package ibis.deploy.gui;

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

    public ExperimentEditorTextPanel(GUI gui, JobTableModel model,
            WorldMapPanel worldMapPanel) {
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(new PoolNamePanel(gui));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        topPanel.add(new ApplicationSelectionPanel(gui));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new ClusterSelectionPanel(gui, worldMapPanel));
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        add(topPanel, BorderLayout.NORTH);
        add(new JPanel(), BorderLayout.CENTER);
        add(new RunButtonPanel(gui, model), BorderLayout.SOUTH);

    }

}

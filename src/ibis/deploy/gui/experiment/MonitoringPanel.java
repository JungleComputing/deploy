package ibis.deploy.gui.experiment;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.experiment.jobs.JobTablePanel;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class MonitoringPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2050832546714791634L;

    public MonitoringPanel(GUI gui, JobTableModel model) {
        setBorder(BorderFactory.createTitledBorder("Experiment Monitor"));
        setLayout(new BorderLayout());
        JPanel smartSocketsPanel = new JPanel();

        try {
            DirectSocketAddress rootHub = DirectSocketAddress.getByAddress(gui
                    .getDeploy().getRootHubAddress());
            smartSocketsPanel = new SmartsocketsViz(Color.BLACK, Color.WHITE,
                    rootHub);
        } catch (Exception e) {
            smartSocketsPanel.add(new JLabel("" + e));
            e.printStackTrace();
        }
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                smartSocketsPanel, new JobTablePanel(gui, model));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);
    }
}

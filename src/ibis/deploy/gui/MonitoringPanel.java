package ibis.deploy.gui;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class MonitoringPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2050832546714791634L;

    public MonitoringPanel(GUI gui, MyTableModel model) {
        setBorder(BorderFactory.createTitledBorder("Experiment Monitor"));
        setLayout(new BorderLayout());
        JPanel smartSocketsPanel = new JPanel();

        List<DirectSocketAddress> rootHubAddressList = new ArrayList<DirectSocketAddress>();
        try {
            rootHubAddressList.add(DirectSocketAddress.getByAddress(gui
                    .getDeploy().getRootHubAddress()));
            smartSocketsPanel = new SmartsocketsViz(rootHubAddressList);
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

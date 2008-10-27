package deployer.ibis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;

import deployer.Application;
import deployer.Cluster;
import deployer.Deploy;
import deployer.Deployer;
import deployer.gui.SelectionComponent;
import deployer.ibis.gui.IbisApplicationEditorPanel;
import deployer.ibis.gui.IbisGridEditorPanel;
import deployer.ibis.gui.IbisHubSelectionComponent;
import deployer.ibis.gui.IbisPoolSelectionComponent;

public class IbisDeploy extends Deploy {

    public IbisDeploy(String name, String[] args) {
        super(name, new IbisDeployer(), args);
        List<SelectionComponent> deploySelectionComponents = new ArrayList<SelectionComponent>();
        deploySelectionComponents.add(new IbisPoolSelectionComponent());
        deploySelectionComponents.add(new IbisHubSelectionComponent());
        getSelectionComponentGroups().add(deploySelectionComponents);
    }

    public JPanel createApplicationEditor(Deployer deployer) {
        return new IbisApplicationEditorPanel(deployer, "applications");
    }

    public JPanel createGridEditor(Deployer deployer) {
        return new IbisGridEditorPanel(deployer, "grids");
    }

    public static void main(String[] args) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        final IbisDeploy deploy = new IbisDeploy("Ibis Deploy", args);
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(deploy);
            }
        });
    }

    protected ActionListener getSubmitButtonActionListener(
            DefaultTableModel defaultTableModel) {
        return new IbisSubmitButtonActionListener(defaultTableModel);
    }

    private class IbisSubmitButtonActionListener implements ActionListener {

        DefaultTableModel model;

        public IbisSubmitButtonActionListener(DefaultTableModel model) {
            this.model = model;
        }

        public void actionPerformed(ActionEvent event) {
            try {
                final Application application = (Application) getSelectionComponentGroups()
                        .get(0).get(0).getValues()[0];
                final int processCount = (Integer) getSelectionComponentGroups()
                        .get(0).get(0).getValues()[1];
                final Cluster cluster = (Cluster) getSelectionComponentGroups()
                        .get(0).get(1).getValues()[0];
                final int resourceCount = (Integer) getSelectionComponentGroups()
                        .get(0).get(1).getValues()[1];
                final IbisPool pool = (IbisPool) getSelectionComponentGroups()
                        .get(1).get(0).getValues()[0];
                final String server = (String) getSelectionComponentGroups()
                        .get(1).get(1).getValues()[0];
                final String hub = (String) getSelectionComponentGroups()
                        .get(1).get(1).getValues()[1];
                final String name = (String) getSelectionComponentGroups().get(
                        1).get(1).getValues()[2];

                model.addRow(new Object[] { "n.a.", "INITIAL", null, null,
                        null, (Job) null });
                model.fireTableChanged(new TableModelEvent(model));
                final int row = model.getRowCount() - 1;
                new Thread() {
                    public void run() {
                        try {
                            String serverAddress = null;
                            String hubAddress = null;
                            if (server == null) {
                                Server ibisServer = new Server(name,
                                        (IbisCluster) cluster,
                                        (IbisApplication) application);
                                ibisServer.startServer(new MetricListener() {
                                    public void processMetricEvent(
                                            MetricEvent event) {
                                        model.setValueAt("SERVER "
                                                + event.getValue().toString(),
                                                row, 1);
                                        model.fireTableCellUpdated(row, 1);
                                    }
                                });
                                serverAddress = ibisServer.getServerClient()
                                        .getLocalAddress();
                                hubAddress = ibisServer.getServerClient()
                                        .getLocalAddress();
                                ((IbisHubSelectionComponent) getSelectionComponentGroups()
                                        .get(1).get(1)).addServer(ibisServer);
                                ((IbisHubSelectionComponent) getSelectionComponentGroups()
                                        .get(1).get(1)).addHub(ibisServer);
                            } else if (hub == null) {
                                Server ibisHub = new Server(name,
                                        (IbisCluster) cluster,
                                        (IbisApplication) application, server);
                                ibisHub.startServer(new MetricListener() {

                                    public void processMetricEvent(
                                            MetricEvent event) {
                                        model.setValueAt("HUB "
                                                + event.getValue().toString(),
                                                row, 1);
                                        model.fireTableCellUpdated(row, 1);
                                    }

                                });
                                serverAddress = server;
                                hubAddress = ibisHub.getServerClient()
                                        .getLocalAddress();
                                ((IbisHubSelectionComponent) getSelectionComponentGroups()
                                        .get(1).get(1)).addHub(ibisHub);
                            } else {
                                serverAddress = server;
                                hubAddress = hub;
                            }
                            pool.addSubmitted(processCount);
                            (getSelectionComponentGroups().get(1).get(0))
                                    .getPanel().repaint();

                            Job job = ((IbisDeployer) deployer).deploy(
                                    application, processCount, cluster,
                                    resourceCount, pool, serverAddress,
                                    hubAddress, new JobMonitoringTableUpdater(
                                            model));
                            model.setValueAt(job.getJobID(), row, 0);
                            model.setValueAt(job.getState(), row, 1);
                            model.setValueAt(job, row, 5);
                            model.fireTableChanged(new TableModelEvent(model));
                        } catch (Exception e) {
                            model.setValueAt("DEPLOY FAILED ", row, 1);
                            model.fireTableCellUpdated(row, 1);
                            e.printStackTrace();
                        }
                    }
                }.start();
            } catch (Exception e) {
                System.err.println("ARGH FAILED");
                e.printStackTrace();
            }
        }

    }

}

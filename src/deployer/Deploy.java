package deployer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Job.JobState;

import deployer.gui.ApplicationEditorPanel;
import deployer.gui.ApplicationSelectionComponent;
import deployer.gui.ClusterSelectionComponent;
import deployer.gui.GridEditorPanel;
import deployer.gui.SelectionComponent;

public class Deploy {

    protected Deployer deployer;

    private List<List<SelectionComponent>> selectionComponentGroups = new ArrayList<List<SelectionComponent>>();

    private String name;

    protected Deploy(String name) {
        this(name, new Deployer());

    }

    protected Deploy(String name, Deployer deployer) {
        this.name = name;
        this.deployer = deployer;
        // defaults for Deploy
        List<SelectionComponent> deploySelectionComponents = new ArrayList<SelectionComponent>();
        deploySelectionComponents.add(new ApplicationSelectionComponent(
                deployer));
        deploySelectionComponents.add(new ClusterSelectionComponent(deployer));
        getSelectionComponentGroups().add(deploySelectionComponents);
    }

    protected List<List<SelectionComponent>> getSelectionComponentGroups() {
        return selectionComponentGroups;
    }

    /**
     * Starts the deploy application.
     * 
     * @param args
     *                arguments are ignored
     */
    public static void main(String[] args) {
        final Deploy deploy = new Deploy("JavaGAT Deploy");
        // for (LookAndFeelInfo lf : UIManager.getInstalledLookAndFeels()) {
        // System.out.println(lf.getClassName());
        // }
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(deploy);
            }
        });
    }

    protected static void createAndShowGUI(Deploy deploy) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        JFrame frame = new JFrame(deploy.name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Deploy", createDeployPanel(deploy));
        tabbedPane.add("Applications", deploy
                .getApplicationEditor(deploy.deployer));
        tabbedPane.add("Grids", deploy.getGridEditor(deploy.deployer));
        tabbedPane.addChangeListener(new TabbedPaneChangeListener(deploy));
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Display the window.
        frame.pack();
        frame.setVisible(true);

    }

    protected JPanel getApplicationEditor(Deployer deployer) {
        return new ApplicationEditorPanel(deployer, "applications");
    }

    protected JPanel getGridEditor(Deployer deployer) {
        return new GridEditorPanel(deployer, "grids");
    }

    private static class JobMonitoringTableModel extends DefaultTableModel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public JobMonitoringTableModel(String[] strings, int i) {
            super(strings, i);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 5);
        }

        @SuppressWarnings("unchecked")
        public Class getColumnClass(int c) {
            if (getValueAt(0, c) == null) {
                return String.class;
            }
            if (c == 5) {
                return Job.class;
            }
            return getValueAt(0, c).getClass();
        }

    }

    private static JComponent createDeployPanel(final Deploy deploy) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel selectionComponentsPanel = new JPanel(new GridLayout(1,
                deploy.selectionComponentGroups.size()));

        for (List<SelectionComponent> selectionComponentGroup : deploy
                .getSelectionComponentGroups()) {
            JPanel columnPanel = new JPanel();
            columnPanel.setLayout(new BoxLayout(columnPanel,
                    BoxLayout.PAGE_AXIS));
            for (SelectionComponent selectionComponent : selectionComponentGroup) {
                columnPanel.add(selectionComponent.getPanel());
            }
            selectionComponentsPanel.add(columnPanel);
        }
        panel.add(selectionComponentsPanel);

        JPanel buttonPanel = new JPanel();

        JobMonitoringTableModel model = new JobMonitoringTableModel(
                new String[] { "Job ID", "Job State", "exitvalue", "stdout",
                        "stderr", "stop" }, 0);
        JTable jobMonitoringTable = new JTable(model);
        jobMonitoringTable.setDefaultRenderer(Job.class,
                new ButtonCellRenderer());
        jobMonitoringTable.setDefaultEditor(Job.class, new ButtonEditor());

        jobMonitoringTable.setCellSelectionEnabled(true);
        jobMonitoringTable.addMouseListener(new JobMonitoringMouseListener());
        JScrollPane pane = new JScrollPane(jobMonitoringTable);
        pane
                .setPreferredSize(new Dimension(pane.getPreferredSize().width,
                        200));
        pane.setMaximumSize(new Dimension(pane.getMaximumSize().width, 300));
        JButton submitButton = new JButton("Submit");
        submitButton
                .addActionListener(deploy
                        .getSubmitButtonActionListener(((DefaultTableModel) jobMonitoringTable
                                .getModel())));
        buttonPanel.setMaximumSize(new Dimension(
                buttonPanel.getMaximumSize().width, buttonPanel
                        .getPreferredSize().height));
        buttonPanel.add(submitButton);
        panel.add(buttonPanel);
        panel.add(pane);
        return panel;
    }

    protected ActionListener getSubmitButtonActionListener(
            DefaultTableModel defaultTableModel) {
        return new SubmitButtonActionListener(defaultTableModel);
    }

    private static class TabbedPaneChangeListener implements ChangeListener {

        Deploy deploy;

        public TabbedPaneChangeListener(Deploy deploy) {
            this.deploy = deploy;
        }

        public void actionPerformed(ActionEvent event) {
            for (List<SelectionComponent> selectionComponentGroup : deploy
                    .getSelectionComponentGroups()) {
                for (SelectionComponent selectionComponent : selectionComponentGroup) {
                    selectionComponent.update();
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            for (List<SelectionComponent> selectionComponentGroup : deploy
                    .getSelectionComponentGroups()) {
                for (SelectionComponent selectionComponent : selectionComponentGroup) {
                    selectionComponent.update();
                }
            }
        }
    }

    private class SubmitButtonActionListener implements ActionListener {

        DefaultTableModel model;

        public SubmitButtonActionListener(DefaultTableModel model) {
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
                model.addRow(new Object[] { "n.a.", "INITIAL" });
                model.fireTableChanged(new TableModelEvent(model));
                final int row = model.getRowCount() - 1;
                new Thread() {
                    public void run() {
                        try {
                            Job job = deployer.deploy(application,
                                    processCount, cluster, resourceCount,
                                    new JobMonitoringTableUpdater(model));
                            model.setValueAt(job, row, 5);
                            model.setValueAt(job.getJobID(), row, 0);
                            model.setValueAt(job.getState(), row, 1);
                            model.fireTableChanged(new TableModelEvent(model));
                        } catch (Exception e) {
                            System.err.println(e);
                            System.err.println("submission failed!");
                            e.printStackTrace();
                        }
                    }

                }.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    protected class JobMonitoringTableUpdater implements MetricListener {

        DefaultTableModel model;

        public JobMonitoringTableUpdater(DefaultTableModel model) {
            this.model = model;
        }

        public void processMetricEvent(MetricEvent event) {
            System.out.println(event);
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    if ((Integer) model.getValueAt(i, 0) == ((Job) event
                            .getSource()).getJobID()) {
                        model.setValueAt(event.getValue(), i, 1);
                        model.fireTableCellUpdated(i, 1);
                        if (event.getValue() == JobState.STOPPED) {
                            try {
                                model.setValueAt(((Job) event.getSource())
                                        .getExitStatus(), i, 2);
                            } catch (GATInvocationException e) {
                                model.setValueAt("n.a.", i, 2);
                            } finally {
                                model.fireTableCellUpdated(i, 2);
                                model.fireTableCellUpdated(i, 5);
                            }
                            model.setValueAt(((JobDescription) ((Job) event
                                    .getSource()).getJobDescription())
                                    .getSoftwareDescription().getStdout()
                                    .getPath(), i, 3);
                            model.setValueAt(((JobDescription) ((Job) event
                                    .getSource()).getJobDescription())
                                    .getSoftwareDescription().getStderr()
                                    .getPath(), i, 4);
                        }

                    }
                } catch (ClassCastException e) {
                }
            }
        }
    }

    private static class JobMonitoringMouseListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
                    && ((JTable) e.getSource()).getSelectedColumn() >= 3) {
                String filename = (String) (((JTable) e.getSource()).getModel()
                        .getValueAt(((JTable) e.getSource()).getSelectedRow(),
                                ((JTable) e.getSource()).getSelectedColumn()));
                if (filename == null) {
                    return;
                } else {
                    // ((JComponent)e.getSource()).getRootPane()
                    FileReader reader;
                    try {
                        reader = new FileReader(filename);
                    } catch (FileNotFoundException e1) {
                        return;
                    }
                    String text = "";
                    char[] buffer = new char[1024];
                    int result = 0;
                    do {
                        try {
                            result = reader.read(buffer);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        if (result > 0) {
                            text += new String(buffer, 0, result);
                        }
                    } while (result >= 0);
                    JFrame frame = new JFrame(filename);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    JTextArea textArea = new JTextArea();
                    textArea.setText(text);
                    frame.getContentPane().add(new JScrollPane(textArea));
                    frame.pack();
                    frame.setVisible(true);

                }
            }

        }

        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        public void mouseExited(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        public void mousePressed(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        public void mouseReleased(MouseEvent e) {
            // TODO Auto-generated method stub

        }

    }

    private static class ButtonCellRenderer extends JButton implements
            TableCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable table,
                Object job, boolean isSelected, boolean hasFocus, int row,
                int column) {
            if (job == null) {
                JButton button = new JButton("n.a.");
                button.setEnabled(false);
                return button;
            }
            if (((Job) job).getState() == JobState.STOPPED) {
                JButton button = new JButton("stopped");
                button.setEnabled(false);
                return button;
            }
            return new JButton("stop");
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private Job job;

        public ButtonEditor() {
            super(new JCheckBox());
        }

        public Component getTableCellEditorComponent(JTable table, Object job,
                boolean isSelected, int row, int column) {
            this.job = (Job) job;
            if (job == null) {
                JButton button = new JButton("n.a.");
                button.setEnabled(false);
                return button;
            }
            if (((Job) job).getState() == JobState.STOPPED) {
                JButton button = new JButton("stopped");
                button.setEnabled(false);
                return button;
            }
            try {
                ((Job) job).stop();
            } catch (GATInvocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JButton button = new JButton("stopped");
            button.setEnabled(false);
            return button;
        }

        public Object getCellEditorValue() {
            return job;
        }

    }

}

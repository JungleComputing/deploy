package ibis.deploy.gui;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;

class MyTableRenderer extends JLabel implements TableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = -1269380843774208099L;

    private GUI gui;

    public MyTableRenderer(GUI gui) {
        super();
        this.gui = gui;
    }

    public Component getTableCellRendererComponent(final JTable table,
            final Object object, boolean isSelected, boolean hasFocus,
            final int row, int column) {
        setText("N.A.");
        setOpaque(isSelected);
        setBackground(UIManager.getColor("Table.selectionBackground"));
        if (column == 0) {
            if (object instanceof Job
                    && !table.getValueAt(row, 2).equals("STOPPED")) {
                // job submitted, not yet stopped
                final JButton stopButton = GUIUtils.createImageButton(
                        "images/media-playback-stop.png", null, null);
                stopButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        stopButton.setEnabled(false);
                        ((Job) object).kill();
                    }
                });
                return stopButton;
            } else {
                // job not yet submitted or stopped
                final JButton startButton = GUIUtils.createImageButton(
                        "images/media-playback-start.png", null, null);
                startButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        startButton.setEnabled(false);
                        JobDescription jd = null;
                        if (object instanceof Job) {
                            // stopped job
                            jd = ((Job) object).getDescription();
                        } else {
                            // not yet submitted job
                            jd = (JobDescription) object;
                        }
                        try {
                            Job job = gui.getDeploy().submitJob(jd,
                                    gui.getApplicationSet(), gui.getGrid(),
                                    new MetricListener() {

                                        public void processMetricEvent(
                                                MetricEvent event) {
                                            table
                                                    .getModel()
                                                    .setValueAt(
                                                            event.getValue()
                                                                    .toString(),
                                                            row, 2);
                                        }

                                    }, new MetricListener() {

                                        public void processMetricEvent(
                                                MetricEvent event) {
                                            table
                                                    .getModel()
                                                    .setValueAt(
                                                            event.getValue()
                                                                    .toString(),
                                                            row, 3);
                                        }

                                    });
                            ((MyTableModel) table.getModel()).setRow(job, row);
                            ((MyTableModel) table.getModel())
                                    .fireTableChanged(new TableModelEvent(
                                            ((MyTableModel) table.getModel())));
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(getRootPane(), e
                                    .getMessage(), "Job creation failed",
                                    JOptionPane.PLAIN_MESSAGE);
                            e.printStackTrace(System.err);
                        }
                    }
                });
                return startButton;
            }
        } else if (column == 1) {
            if (object instanceof Job) {
                setText(((Job) object).getDescription().getName());
            } else {
                setText(((JobDescription) object).getName());
            }
        } else if (column == 2) {
            setText(object.toString());
        } else if (column == 3) {
            setText(object.toString());
        } else if (column == 4) {
            if (object instanceof Job) {
                setText(((Job) object).getDescription().getClusterName());
            } else {
                setText(((JobDescription) object).getClusterName());
            }
        } else if (column == 5) {
            if (object instanceof Job) {
                setText(((Job) object).getDescription().getApplicationName());
            } else {
                setText(((JobDescription) object).getApplicationName());
            }
        } else if (column == 6) {
            if (object instanceof Job) {
                setText("" + ((Job) object).getDescription().getProcessCount());
            } else {
                setText("" + ((JobDescription) object).getProcessCount());
            }
        } else if (column == 7) {
            if (object instanceof Job) {
                setText("" + ((Job) object).getDescription().getResourceCount());
            } else {
                setText("" + ((JobDescription) object).getResourceCount());
            }
        } else if (column == 8) {
            setText("stdout");
        } else if (column == 9) {
            setText("stderr");
        } else if (column == 10) {
            setText("?");
        }
        return this;
    }
}

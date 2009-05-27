package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.gui.misc.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

class JobTableRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = -1269380843774208099L;

    private final JobTableModel model;

    public JobTableRenderer(JobTableModel model) {
        super();
        this.model = model;
    }

    public Component getTableCellRendererComponent(final JTable table,
            final Object value, boolean isSelected, boolean hasFocus,
            final int row, int column) {
        setText("");
        setOpaque(isSelected);
        setBackground(UIManager.getColor("Table.selectionBackground"));
        setForeground(Color.BLACK);
        
        if (column == JobRow.CONTROL_COLUMN) {
            boolean start = (Boolean) value;

            if (start) {
                final JButton startButton = Utils.createImageButton(

                "/images/media-playback-start.png", null, null);

                startButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        startButton.setEnabled(false);
                        model.start(table.convertRowIndexToModel(row));
                    }
                });

                return startButton;
            } else {
                final JButton stopButton = Utils.createImageButton(
                        "/images/media-playback-stop.png", null, null);
                stopButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        stopButton.setEnabled(false);
                        model.stop(table.convertRowIndexToModel(row));
                    }
                });
                return stopButton;
            }
        } else if (column == JobRow.POOL_COLUMN) {
            setText("" + value);
        } else if (column == JobRow.NAME_COLUMN) {
            setText("" + value);
        } else if (column == JobRow.JOB_STATUS_COLUMN) {
            State state = (State) value;

            if (state != null && state != State.UNKNOWN) {
                if (state == State.DEPLOYED) {
                    // green
                    setForeground(Color.decode("#16B400"));
                } else if (state == State.ERROR) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }

                setText(state.toString());
            }
        } else if (column == JobRow.HUB_STATUS_COLUMN) {
            State state = (State) value;

            if (state != null && state != State.UNKNOWN) {
                if (state == State.DEPLOYED) {
                    // green
                    setForeground(Color.decode("#16B400"));
                } else if (state == State.ERROR) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }

                setText(state.toString());
            }
        } else if (column == JobRow.CLUSTER_COLUMN) {
            setOpaque(true);

            JobDescription jobDescription = model.getJobDescription(table
                    .convertRowIndexToModel(row));

            if (isSelected) {
                setBackground(Utils.getColor(jobDescription
                        .getClusterOverrides().getColorCode()));
            } else {
                setBackground(Utils.getLightColor(jobDescription
                        .getClusterOverrides().getColorCode()));
            }

            setText(jobDescription.getClusterName());
        } else if (column == JobRow.MIDDLEWARE_COLUMN) {
            String adaptor = (String) value;
            if (adaptor != null && adaptor.equalsIgnoreCase("sshTrilead")) {
                adaptor = "ssh";
            }
            setText(adaptor);
        } else if (column == JobRow.APPLICATION_COLUMN) {
            setText("" + value);
        } else if (column == JobRow.PROCESS_COUNT_COLUMN) {
            setText("" + value);
        } else if (column == JobRow.RESOURCE_COUNT_COLUMN) {
            setText("" + value);
        } else if (column == JobRow.OUTPUT_COLUMN) {
            boolean enabled = (Boolean) value;

            final Job job = model.getJob(table.convertRowIndexToModel(row));

            JButton button = new JButton("output");
            button.setMargin(new Insets(2, 2, 2, 2));
            // button.setPreferredSize(new Dimension(10, 10));
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    System.err.println("Output!");
                    JDialog dialog = new JDialog(SwingUtilities
                            .getWindowAncestor(table), "Output Files for "
                            + job.getDescription().getName());
                    dialog.setContentPane(new OutputPanel(job));
                    dialog.pack();
                    dialog.setLocationRelativeTo(SwingUtilities
                            .getWindowAncestor(table));
                    dialog.setVisible(true);

                }

            });
            button.setEnabled(enabled);
          
            return button;
        }
        return this;
    }
}

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
import javax.swing.border.EmptyBorder;
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
        setBorder(new EmptyBorder(5,5,5,5));
        setText("");
        setOpaque(isSelected);
        
        setBackground(UIManager.getColor("Table.selectionBackground"));
        setForeground(Color.BLACK);

        String columnName = table.getColumnName(column);

        if (columnName.equalsIgnoreCase(JobTableModel.CONTROL_COLUMN_NAME)) {
            boolean start = (Boolean) value;

            if (start) {
                final JButton startButton = Utils.createImageButton(

                "images/media-playback-start.png", "Start job", null);

                startButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        startButton.setEnabled(false);
                        model.start(table.convertRowIndexToModel(row));
                    }
                });

                return startButton;
            } else {
                final JButton stopButton = Utils.createImageButton(
                        "images/media-playback-stop.png", "Stop job", null);
                stopButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        stopButton.setEnabled(false);
                        model.stop(table.convertRowIndexToModel(row));
                    }
                });
                return stopButton;
            }
        } else if (columnName.equalsIgnoreCase(JobTableModel.POOL_COLUMN_NAME)) {
            setText("" + value);
        } else if (columnName.equalsIgnoreCase(JobTableModel.NAME_COLUMN_NAME)) {
            setText("" + value);
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.JOB_STATUS_COLUMN_NAME)) {
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
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.HUB_STATUS_COLUMN_NAME)) {
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
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.CLUSTER_COLUMN_NAME)) {
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
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.MIDDLEWARE_COLUMN_NAME)) {
            String adaptor = (String) value;
            if (adaptor != null && adaptor.equalsIgnoreCase("sshTrilead")) {
                adaptor = "ssh";
            }
            setText(adaptor);
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.APPLICATION_COLUMN_NAME)) {
            setText("" + value);
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.PROCESS_COUNT_COLUMN_NAME)) {
            setText("" + value);
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.RESOURCE_COUNT_COLUMN_NAME)) {
            setText("" + value);
        } else if (columnName
                .equalsIgnoreCase(JobTableModel.OUTPUT_COLUMN_NAME)) {
            boolean enabled = (Boolean) value;

            final Job job = model.getJob(table.convertRowIndexToModel(row));

            final JButton button = Utils.createImageButton(

                    null, "Show output of job", "output");
            
            //JButton button = new JButton("output");
            //button.setMargin(new Insets(2, 2, 2, 2));
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

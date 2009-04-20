package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.misc.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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

    /**
     * 
     */
    private static final long serialVersionUID = -1269380843774208099L;

    private GUI gui;

    public JobTableRenderer(GUI gui) {
        super();
        this.gui = gui;
    }

    public Component getTableCellRendererComponent(final JTable table,
            final Object object, boolean isSelected, boolean hasFocus,
            final int row, int column) {
        setText("");
        setOpaque(isSelected);
        setBackground(UIManager.getColor("Table.selectionBackground"));
        setForeground(Color.BLACK);
        final Job job = ((JobRowObject) object).getJob();
        JobDescription jobDescription = null;
        JobRowObject data = (JobRowObject) object;
        try {
            jobDescription = ((JobRowObject) object).getJobDescription()
                    .resolve(gui.getApplicationSet(), gui.getGrid());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (column == 0) {
            boolean startedAndNotFinished = false;
            if (job != null) {
                startedAndNotFinished = true;
                try {
                    startedAndNotFinished = !job.isFinished();
                } catch (Exception e) {
                    startedAndNotFinished = false;
                }
            }
            if (startedAndNotFinished) {
                // job submitted, not yet stopped
                final JButton stopButton = Utils.createImageButton(
                        "/images/media-playback-stop.png", null, null);
                stopButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        stopButton.setEnabled(false);
                        job.kill();
                    }
                });
                return stopButton;
            } else {
                // job not yet submitted or stopped
                final JButton startButton = Utils.createImageButton(
                        new SubmitExistingJobAction(row, false, false, table,
                                gui, getRootPane()),
                        "/images/media-playback-start.png", null, null);
                return startButton;
            }
        } else if (column == 1) {
            setText(jobDescription.getPoolName());
        } else if (column == 2) {
            setText(jobDescription.getName());
        } else if (column == 3) {
            State state = data.getJobState();

            if (state != null) {
                if (state == State.DEPLOYED) {
                    //green
                    setForeground(Color.decode("#00CC00"));
                } else if (state == State.ERROR) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }

                setText(state.toString());
            }
        } else if (column == 4) {
            State state = data.getHubState();

            if (state != null) {
                if (state == State.DEPLOYED) {
                    //green
                    setForeground(Color.decode("#00CC00"));
                } else if (state == State.ERROR) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }

                setText(state.toString());
            }
        } else if (column == 5) {
            setOpaque(true);

            if (isSelected) {
                setBackground(jobDescription.getClusterOverrides().getColor());
            } else {
                setBackground(jobDescription.getClusterOverrides()
                        .getLightColor());
            }

            setText(jobDescription.getClusterName());
        } else if (column == 6) {
            String adaptor = jobDescription.getClusterOverrides().getJobAdaptor();
            if (adaptor.equalsIgnoreCase("sshTrilead")) {
                adaptor = "ssh";
            }
            setText(adaptor);
        } else if (column == 7) {
            setText(jobDescription.getApplicationName());
        } else if (column == 8) {
            setText("" + jobDescription.getProcessCount());
        } else if (column == 9) {
            setText("" + jobDescription.getResourceCount());
        } else if (column == 10) {
            JButton button = new JButton("output");
            button.setMargin(new Insets(2, 2, 2, 2));
            button.setPreferredSize(new Dimension(10,10));
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
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
            try {
                button.setEnabled(job != null && job.isFinished());
            } catch (Exception e) {
                button.setEnabled(false);
            }
            return button;
        }
        return this;
    }
}

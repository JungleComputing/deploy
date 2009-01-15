package ibis.deploy.gui;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.action.SubmitExistingJobAction;

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
        setText("N.A.");
        setOpaque(isSelected);
        setBackground(UIManager.getColor("Table.selectionBackground"));
        final Job job = ((JobRowObject) object).getJob();
        JobDescription jobDescription = null;
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
                    startedAndNotFinished = ! job.isFinished();
                } catch(Exception e) {
                    // ignored
                }
            }
            if (startedAndNotFinished) {
                // job submitted, not yet stopped
                final JButton stopButton = GUIUtils.createImageButton(
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
                final JButton startButton = GUIUtils.createImageButton(
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
            setText(((JobRowObject) object).getJobState());
        } else if (column == 4) {
            setText(((JobRowObject) object).getHubState());
        } else if (column == 5) {
            setText(jobDescription.getClusterName());
        } else if (column == 6) {
            setText(jobDescription.getApplicationName());
        } else if (column == 7) {
            setText("" + jobDescription.getProcessCount());
        } else if (column == 8) {
            setText("" + jobDescription.getResourceCount());
        } else if (column == 9) {
            JButton button = new JButton("output");
            button.setMargin(new Insets(2, 2, 2, 2));
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
            } catch(Exception e) {
                button.setEnabled(false);
            }
            return button;
        }
        return this;
    }
}

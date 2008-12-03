package ibis.deploy.gui;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

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
        final Job job = (object instanceof Job) ? (Job) object : null;
        final JobDescription jobDescription = (object instanceof JobDescription) ? (JobDescription) object
                : (object instanceof Job) ? ((Job) object).getDescription()
                        : null;
        if (column == 0) {
            if (job != null && !table.getValueAt(row, 3).equals("STOPPED")) {
                // job submitted, not yet stopped
                final JButton stopButton = GUIUtils.createImageButton(
                        "images/media-playback-stop.png", null, null);
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
                        "images/media-playback-start.png", null, null);
                return startButton;
            }
        } else if (column == 1) {
            setText(jobDescription.getPoolName());
        } else if (column == 2) {
            setText(jobDescription.getName());
        } else if (column == 3) {
            setText(object.toString());
        } else if (column == 4) {
            setText(object.toString());
        } else if (column == 5) {
            setText(jobDescription.getClusterName());
        } else if (column == 6) {
            setText(jobDescription.getApplicationName());
        } else if (column == 7) {
            setText("" + jobDescription.getProcessCount());
        } else if (column == 8) {
            setText("" + jobDescription.getResourceCount());
        } else if (column == 9) {
            String stdout = jobDescription.getPoolName() + "."
                    + jobDescription.getName() + ".out";
            setName(stdout);
            if (job != null && table.getValueAt(row, 3).equals("STOPPED")) {
                setText("<html><a href=.>" + stdout + "</a></html>");
            } else {
                setText(null);
            }
        } else if (column == 10) {
            String stderr = jobDescription.getPoolName() + "."
                    + jobDescription.getName() + ".err";
            setName(stderr);
            if (job != null && table.getValueAt(row, 3).equals("STOPPED")) {
                setText("<html><a href=.>" + stderr + "</a></html>");
            } else {
                setText(null);
            }
        } else if (column == 11) {
            if (job != null && table.getValueAt(row, 3).equals("STOPPED")) {
                setText(job.getExitValue());
            }
        }
        return this;
    }
}

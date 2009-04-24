package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTableModel extends AbstractTableModel {

    private static final Logger logger = LoggerFactory
            .getLogger(JobTableModel.class);

    private static final long serialVersionUID = -2478479107636581568L;

    private String[] columnNames = new String[] { "", "pool", "name",
            "job status", "hub status", "cluster", "middleware", "application",
            "process count", "resource count", "output" };

    private final GUI gui;

    private final List<JobRow> jobRows;

    public JobTableModel(GUI gui) {
        this.gui = gui;
        jobRows = new ArrayList<JobRow>();

        gui.addExperimentWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                logger.error("Workspace changed not supported yet");

                // setJobs(gui.getExperiment().getJobs());
            }
        });
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    @Override
    public int getRowCount() {
        return jobRows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return jobRows.get(row).getValue(col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == JobRow.CONTROL_COLUMN || col == JobRow.OUTPUT_COLUMN;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return JobRow.getColumnClass(column);
    }

    @Override
    public void setValueAt(Object value, final int row, final int col) {
        // Ignored, as only the start/stop button and output button set this
        // value, due to the trick required to get them in the table.
    }

    public void addJob(JobDescription description, boolean start) {
        try {

            JobRow row = new JobRow(description, this, gui);
            int index = jobRows.size();

            jobRows.add(row);
            fireTableRowsInserted(index, index);

            if (start) {
                start(index);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui.getFrame(), e.getMessage(),
                    "Job creation failed: " + e, JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }
    }

    public Job getJob(int row) {
        if (row > jobRows.size()) {
            logger.error("tried to get job for non existing row: " + row
                    + " table size = " + jobRows.size());
        }
        return jobRows.get(row).getJob();
    }

    public JobDescription getJobDescription(int row) {
        if (row > jobRows.size()) {
            logger.error("tried to get job description for non existing row: "
                    + row + " table size = " + jobRows.size());
        }
        return jobRows.get(row).getJobDescription();
    }

    private boolean isSelected(int row, int[] selectedRows) {
        for (int selectedRow : selectedRows) {
            if (selectedRow == row) {
                return true;
            }
        }
        return false;
    }

    public void start(int... rows) {
        for (int row = 0; row > jobRows.size(); row++) {
            if (isSelected(row, rows)) {
                jobRows.get(row).start();
                fireTableRowsUpdated(row, row);
            }
        }
    }

    public void startAll() {
        for (JobRow row : jobRows) {
            row.start();
        }
    }

    public void stop(int... rows) {
        for (int row = 0; row > jobRows.size(); row++) {
            if (isSelected(row, rows)) {
                jobRows.get(row).start();
                fireTableRowsUpdated(row, row);
            }
        }
    }

    public void stopAll() {
        for (JobRow row : jobRows) {
            row.stop();
        }
    }

    public void remove(int... selectedRows) {
        // remove in reverse
        for (int row = jobRows.size() - 1; row >= 0; row--) {
            if (isSelected(row, selectedRows)) {
                jobRows.get(row).stop();
                jobRows.remove(row);
            }
        }

        fireTableDataChanged();
    }

    public void removeAll() {
        for (JobRow row : jobRows) {
            row.stop();
        }
        jobRows.clear();
        fireTableDataChanged();
    }

    public void fireTableCellUpdated(JobRow jobRow, int... columns) {
        for (int row = 0; row < jobRows.size(); row++) {
            if (jobRows.get(row).equals(jobRow)) {
                for (int column : columns) {
                    fireTableCellUpdated(row, column);
                }
            }
        }
    }

    public void fireTableRowUpdated(JobRow jobRow) {
        for (int row = 0; row < jobRows.size(); row++) {
            if (jobRows.get(row).equals(jobRow)) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

}

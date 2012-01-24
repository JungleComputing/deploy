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

    private static final Logger logger = LoggerFactory.getLogger(JobTableModel.class);

    private static final long serialVersionUID = -2478479107636581568L;

    public static final String CONTROL_COLUMN_NAME = "";
    public static final String POOL_COLUMN_NAME = "pool";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String JOB_STATUS_COLUMN_NAME = "job status";
    public static final String HUB_STATUS_COLUMN_NAME = "hub status";
    public static final String RESOURCE_COLUMN_NAME = "resource";
    public static final String MIDDLEWARE_COLUMN_NAME = "middleware";
    public static final String APPLICATION_COLUMN_NAME = "application";
    public static final String PROCESS_COUNT_COLUMN_NAME = "processes";
    public static final String RESOURCE_COUNT_COLUMN_NAME = "resources";
    public static final String RUNTIME_COLUMN_NAME = "runtime";
    public static final String OUTPUT_COLUMN_NAME = "output";

    private String[] columnNames = new String[] { CONTROL_COLUMN_NAME, POOL_COLUMN_NAME, NAME_COLUMN_NAME,
            JOB_STATUS_COLUMN_NAME, HUB_STATUS_COLUMN_NAME, RESOURCE_COLUMN_NAME, MIDDLEWARE_COLUMN_NAME,
            APPLICATION_COLUMN_NAME, PROCESS_COUNT_COLUMN_NAME, RESOURCE_COUNT_COLUMN_NAME, RUNTIME_COLUMN_NAME,
            OUTPUT_COLUMN_NAME };

    private final GUI gui;

    private final List<JobRow> rows;

    public JobTableModel(GUI gui) {
        this.gui = gui;
        rows = new ArrayList<JobRow>();

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
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return rows.get(row).getValue(col);
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
            int index = rows.size();

            rows.add(row);
            fireTableRowsInserted(index, index);

            if (start) {
                start(index);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui.getFrame(), e.getMessage(), "Job creation failed: " + e,
                    JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }
    }

    public Job getJob(int row) {
        if (row > rows.size()) {
            logger.error("tried to get job for non existing row: " + row + " table size = " + rows.size());
        }
        return rows.get(row).getJob();
    }

    public JobDescription getJobDescription(int row) {
        if (row > rows.size()) {
            logger.error("tried to get job description for non existing row: " + row + " table size = " + rows.size());
        }
        return rows.get(row).getJobDescription();
    }

    private boolean isSelected(int row, int[] selection) {
        for (int selectedRow : selection) {
            if (selectedRow == row) {
                return true;
            }
        }
        return false;
    }

    public void start(int... selection) {
        for (int row = 0; row < rows.size(); row++) {
            if (isSelected(row, selection)) {
                rows.get(row).start();
                fireTableRowsUpdated(row, row);
            }
        }
    }

    public void startAll() {
        for (JobRow row : rows) {
            row.start();
        }
        fireTableRowsUpdated(0, rows.size());
    }

    public void stop(int... selection) {
        for (int row = 0; row < rows.size(); row++) {
            if (isSelected(row, selection)) {
                rows.get(row).stop();
                fireTableRowsUpdated(row, row);
            }
        }
    }

    public void stopAll() {
        for (JobRow row : rows) {
            row.stop();
        }
        fireTableRowsUpdated(0, rows.size() - 1);
    }

    public void remove(int... selection) {
        // remove in reverse
        for (int row = rows.size() - 1; row >= 0; row--) {
            if (isSelected(row, selection)) {
                JobRow r = rows.get(row);
                r.stop();
                r.remove();
                rows.remove(row);
            }
        }

        fireTableDataChanged();
    }

    public void removeAll() {
        for (JobRow row : rows) {
            row.stop();
            row.remove();
        }
        rows.clear();
        fireTableDataChanged();
    }

    public void fireTableCellUpdated(JobRow jobRow, int... columns) {
        for (int row = 0; row < rows.size(); row++) {
            if (rows.get(row).equals(jobRow)) {
                for (int column : columns) {
                    fireTableCellUpdated(row, column);
                }
            }
        }
    }

    public void fireTableRowUpdated(JobRow jobRow) {
        for (int row = 0; row < rows.size(); row++) {
            if (rows.get(row).equals(jobRow)) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

}

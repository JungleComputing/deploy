package ibis.deploy.gui.experiment.monitor;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobMonitorTableModel extends AbstractTableModel implements Runnable {

    private static final Logger logger = LoggerFactory
            .getLogger(JobMonitorTableModel.class);

    private static final long serialVersionUID = -2478479107636581568L;

    public static final String POOL_COLUMN_NAME = "pool";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String JOB_STATUS_COLUMN_NAME = "job status";
    public static final String HUB_STATUS_COLUMN_NAME = "hub status";

    public static final String RESOURCE_COLUMN_NAME = "resource";
    public static final String MIDDLEWARE_COLUMN_NAME = "middleware";
    public static final String APPLICATION_COLUMN_NAME = "application";
    public static final String PROCESS_COUNT_COLUMN_NAME = "process count";
    public static final String RESOURCE_COUNT_COLUMN_NAME = "resource count";
    public static final String RUNTIME_COLUMN_NAME = "runtime";
    public static final String OUTPUT_COLUMN_NAME = "output";

    private String[] columnNames = new String[] { POOL_COLUMN_NAME,
            NAME_COLUMN_NAME, JOB_STATUS_COLUMN_NAME, HUB_STATUS_COLUMN_NAME, RESOURCE_COLUMN_NAME,
            MIDDLEWARE_COLUMN_NAME, APPLICATION_COLUMN_NAME,
            PROCESS_COUNT_COLUMN_NAME, RESOURCE_COUNT_COLUMN_NAME, RUNTIME_COLUMN_NAME,
            OUTPUT_COLUMN_NAME };

    private final GUI gui;

    private final List<JobMonitorRow> rows;

    public JobMonitorTableModel(GUI gui) {
        this.gui = gui;
        rows = new ArrayList<JobMonitorRow>();

        ThreadPool.createNew(this, "job monitor");
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
        return col == JobMonitorRow.OUTPUT_COLUMN;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return JobMonitorRow.getColumnClass(column);
    }

    @Override
    public void setValueAt(Object value, final int row, final int col) {
        // Ignored, as only the start/stop button and output button set this
        // value, due to the trick required to get them in the table.
    }

    public JobDescription getJobDescription(int row) {
        if (row > rows.size()) {
            logger.error("tried to get job description for non existing row: "
                    + row + " table size = " + rows.size());
        }
        return rows.get(row).getJobDescription();
    }

    public Job getJob(int row) {
        if (row > rows.size()) {
            logger.error("tried to get job for non existing row: " + row
                    + " table size = " + rows.size());
        }
        return rows.get(row).getJob();
    }

    public void fireTableCellUpdated(JobMonitorRow jobRow, int... columns) {
        for (int row = 0; row < rows.size(); row++) {
            if (rows.get(row).equals(jobRow)) {
                for (int column : columns) {
                    fireTableCellUpdated(row, column);
                }
            }
        }
    }

    public void fireTableRowUpdated(JobMonitorRow jobRow) {
        for (int row = 0; row < rows.size(); row++) {
            if (rows.get(row).equals(jobRow)) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            Job[] jobs = gui.getDeploy().getJobs();

            for (Job job : jobs) {
                try {
                    addJob(job);
                } catch (Exception e) {
                    logger.error("Error while adding job", e);
                }
            }
            try {
        	Thread.sleep(1000);
            } catch(Throwable e) {
        	// ignored.
            }
        }
    }

    private void addJob(Job job) throws Exception {
        boolean present = false;
        for (JobMonitorRow row : rows) {
            if (job.equals(row.getJob())) {
                present = true;
            }
        }
        if (!present) {
            JobMonitorRow row = new JobMonitorRow(job, this);
            int index = rows.size();

            rows.add(row);
            fireTableRowsInserted(index, index);
        }
    }
}

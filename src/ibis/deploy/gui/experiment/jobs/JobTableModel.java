package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.State;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class JobTableModel extends AbstractTableModel {
    
    public static final int CONTROL_COLUMN = 0;
    public static final int POOL_COLUMN = 1;
    public static final int NAME_COLUMN = 2;
    public static final int JOB_STATUS_COLUMN = 3;
    public static final int HUB_STATUS_COLUMN = 4;
    public static final int CLUSTER_COLUMN = 5;
    public static final int MIDDLEWARE_COLUMN = 6;
    public static final int APPLICATION_COLUMN = 7;
    public static final int PROCESS_COUNT_COLUMN = 8;
    public static final int RESOURCE_COUNT_COLUMN = 9;
    
    public static final int OUTPUT_COLUMN = 10;
    
    public static final int NUMBER_OF_COLUMNS = 11;
    

    /**
     * 
     */
    private static final long serialVersionUID = -2478479107636581568L;

    private String[] columnNames = new String[] { "", "pool", "name", "job status",
            "hub status", "cluster", "middleware", "application", "process count", "resource count",
            "output" };

    private List<JobRowObject> jobRows = new ArrayList<JobRowObject>();

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
        return jobRows.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col) {
        return jobRows.get(row);
    }

    public boolean isCellEditable(int row, int col) {
        return col == CONTROL_COLUMN || col == OUTPUT_COLUMN;
    }

    public Class<?> getColumnClass(int column) {
        return JobRowObject.class;
    }

    public void setValueAt(Object value, final int row, final int col) {
        if (row >= jobRows.size()) {
            // this can happen when we get an upcall for a state change, before
            // the row is added!
            return;
        }
        if (col == JobTableModel.JOB_STATUS_COLUMN) {
            jobRows.get(row).setJobState((State) value);
            // the start/stop button
            fireTableCellUpdated(row, CONTROL_COLUMN);
            // the output value
            fireTableCellUpdated(row, OUTPUT_COLUMN);
        } else if (col == JobTableModel.HUB_STATUS_COLUMN) {
            jobRows.get(row).setHubState((State) value);
            fireTableCellUpdated(row, CONTROL_COLUMN);
        } else {
            jobRows.set(row, (JobRowObject) value);
        }
        fireTableCellUpdated(row, col);
    }

    public void addRow(JobRowObject jobRow) {
        jobRows.add(jobRow);
    }

    public void setRow(Object value, int row) {
        jobRows.set(row, (JobRowObject) value);
    }

    public void removeRow(int row) {
        jobRows.remove(row);
    }

    public void clear() {
        jobRows.clear();
    }
}

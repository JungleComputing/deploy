package ibis.deploy.gui.experiment.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class JobTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = -2478479107636581568L;

    private String[] columnNames = new String[] { "", "pool", "name", "status",
            "hub", "cluster", "application", "process count", "resource count",
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
        return col == 0 || col == 9;
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
        if (col == 3) {
            jobRows.get(row).setJobState((String) value);
            // the start/stop button
            fireTableCellUpdated(row, 0);
            // the output value
            fireTableCellUpdated(row, 9);
        } else if (col == 4) {
            jobRows.get(row).setHubState((String) value);
            fireTableCellUpdated(row, 0);
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

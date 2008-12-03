package ibis.deploy.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class MyTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = -2478479107636581568L;

    private String[] columnNames = new String[] { "", "pool", "name", "status",
            "hub", "cluster", "application", "process count", "resource count",
            "stdout", "stderr", "exit value" };

    private List<Object> jobs = new ArrayList<Object>();

    private List<Object> jobStates = new ArrayList<Object>();

    private List<Object> hubStates = new ArrayList<Object>();

    private JTable table;

    public void setTable(JTable table) {
        this.table = table;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
        return jobs.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col) {
        if (col == 3) {
            return jobStates.get(row);
        } else if (col == 4) {
            return hubStates.get(row);
        } else {
            return jobs.get(row);
        }
    }

    public boolean isCellEditable(int row, int col) {
        return col == 0 || col == 9 || col == 10;
    }

    public Class<?> getColumnClass(int column) {
        return Object.class;
    }

    public void setValueAt(Object value, final int row, final int col) {
        fireTableCellUpdated(row, 0);
        if (col == 3) {
            if (row == jobStates.size()) {
                jobStates.add(value);
            } else {
                jobStates.set(row, value);
            }
            // the start/stop button
            fireTableCellUpdated(row, 0);
            // the stdout value
            fireTableCellUpdated(row, 9);
            // the stderr value
            fireTableCellUpdated(row, 10);
            // the exit value
            fireTableCellUpdated(row, 11);
        } else if (col == 4) {
            if (row == hubStates.size()) {
                hubStates.add(value);
            } else {
                hubStates.set(row, value);
            }
            fireTableCellUpdated(row, 0);
        } else {
            jobs.set(row, value);
        }
        fireTableCellUpdated(row, col);
    }

    public void addRow(Object value) {
        jobs.add(value);
        if (jobStates.size() < jobs.size()) {
            jobStates.add("INITIAL");
        }
        if (hubStates.size() < jobs.size()) {
            hubStates.add("INITIAL");
        }
    }

    public void setRow(Object value, int row) {
        jobs.set(row, value);
    }

    public void clear() {
        jobs.clear();
        jobStates.clear();
        hubStates.clear();
    }
}

package ibis.deploy.gui.action;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.JobTableModel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JRootPane;
import javax.swing.JTable;

public class StopExistingJobAction extends AbstractAction {

    private int startRow;

    private boolean untilLastRow;

    private boolean onlySelectedRows;

    private JobTableModel model;

    private JTable table;

    private GUI gui;

    private JRootPane rootPane;

    public StopExistingJobAction(int startRow, boolean untilLastRow,
            boolean onlySelectedRows, JTable table, GUI gui, JRootPane rootPane) {
        this.startRow = startRow;
        this.untilLastRow = untilLastRow;
        this.onlySelectedRows = onlySelectedRows;
        this.table = table;
        this.model = (JobTableModel) table.getModel();
        this.gui = gui;
        this.rootPane = rootPane;
    }

    public void actionPerformed(ActionEvent event) {
        int endRow = untilLastRow ? model.getRowCount() - 1 : startRow;
        int[] selectedRows = table.getSelectedRows();

        for (int row = startRow; row <= endRow; row++) {
            if (onlySelectedRows) {
                // ! selected
                boolean selected = false;
                for (int selectedRow : selectedRows) {
                    selected = (selectedRow == row);
                    if (selected) {
                        break;
                    }
                }
                if (!selected) {
                    continue;
                }
            }
            Object object = model.getValueAt(row, 0);
            JobDescription jd = null;
            if (object instanceof Job) {
                ((Job) object).kill();
            }
        }
    }

}

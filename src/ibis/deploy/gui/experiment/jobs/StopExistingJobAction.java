package ibis.deploy.gui.experiment.jobs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

public class StopExistingJobAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int startRow;

    private boolean untilLastRow;

    private boolean onlySelectedRows;

    private JobTableModel model;

    private JTable table;

    public StopExistingJobAction(int startRow, boolean untilLastRow,
            boolean onlySelectedRows, JTable table) {
        this.startRow = startRow;
        this.untilLastRow = untilLastRow;
        this.onlySelectedRows = onlySelectedRows;
        this.table = table;
        this.model = (JobTableModel) table.getModel();
    }

    public void actionPerformed(ActionEvent event) {
        new Thread() {
            public void run() {
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
                    JobRowObject jobRow = (JobRowObject) model.getValueAt(row,
                            0);
                    if (jobRow.getJob() != null) {
                        jobRow.getJob().kill();
                    }
                }

            }
        }.start();
    }
}

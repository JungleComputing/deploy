package ibis.deploy.gui;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;

public class StopExistingJobAction extends AbstractAction {

    private int startRow;

    private boolean untilLastRow;

    private boolean onlySelectedRows;

    private MyTableModel model;

    private JTable table;

    private GUI gui;

    private JRootPane rootPane;

    public StopExistingJobAction(int startRow, boolean untilLastRow,
            boolean onlySelectedRows, JTable table, GUI gui, JRootPane rootPane) {
        this.startRow = startRow;
        this.untilLastRow = untilLastRow;
        this.onlySelectedRows = onlySelectedRows;
        this.table = table;
        this.model = (MyTableModel) table.getModel();
        this.gui = gui;
        this.rootPane = rootPane;
    }

    public void actionPerformed(ActionEvent event) {
        int endRow = untilLastRow ? model.getRowCount() - 1 : startRow;
        int[] selectedRows = table.getSelectedRows();
        for (int selectedRow : selectedRows) {
            System.out.println("selected row: " + selectedRow);
        }
        for (int row = startRow; row <= endRow; row++) {
            if (onlySelectedRows) {
                // ! selected
                boolean selected = false;
                for (int selectedRow : selectedRows) {
                    selected = (selectedRow == row);
                    if (selected) {
                        System.out.println("found for row: " + row);
                        break;
                    }
                }
                if (!selected) {
                    System.out.println("stop for row: " + row);
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

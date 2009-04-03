package ibis.deploy.gui.action;

import ibis.deploy.Job;
import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.JobRowObject;
import ibis.deploy.gui.JobTableModel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTable;

public class SubmitExistingJobAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int startRow;

    private boolean untilLastRow;

    private boolean onlySelectedRows;

    private JobTableModel model;

    private JTable table;

    private GUI gui;

    private JRootPane rootPane;

    public SubmitExistingJobAction(int startRow, boolean untilLastRow,
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
            final JobRowObject jobRow = (JobRowObject) model.getValueAt(row, 0);
            if (jobRow.getJob() != null) {
                try {
                    // continue for non stopped jobs
                    if (!jobRow.getJob().isFinished()) {
                        continue;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            final int rowValue = row;
            try {
                Job job = gui.getDeploy().submitJob(jobRow.getJobDescription(),
                        gui.getApplicationSet(), gui.getGrid(),
                        new StateListener() {

                            public void stateUpdated(State state, Exception e) {
                                jobRow.setJobState(state.toString());
                                model.setValueAt(state.toString(), rowValue, 3);
                            }

                        }, new StateListener() {

                            public void stateUpdated(State state, Exception e) {
                                jobRow.setHubState(state.toString());
                                model.setValueAt(state.toString(), rowValue, 4);
                            }

                        });
                jobRow.setJob(job);
                // model.fireTableChanged(new TableModelEvent(model));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e.getMessage(),
                        "Job submission failed", JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }
        }

    }
}

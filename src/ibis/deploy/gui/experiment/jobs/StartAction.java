package ibis.deploy.gui.experiment.jobs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

public class StartAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private boolean all;

    private JobTableModel model;

    private JTable table;

    public StartAction(boolean all, JTable table) {
        this.all = all;
        this.table = table;
        this.model = (JobTableModel) table.getModel();
    }

    public void actionPerformed(ActionEvent event) {
        if (all) {
            model.startAll();
            return;
        }
        
        int[] selectedRows = table.getSelectedRows();
        int[] converted = new int[selectedRows.length];
        
        for (int i = 0 ; i < selectedRows.length; i++) {
            converted[i] = table.convertRowIndexToModel(selectedRows[i]);
        }
    
        
        model.start(converted);
    }
}

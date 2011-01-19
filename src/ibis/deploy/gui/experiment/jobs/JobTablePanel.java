package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class JobTablePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9193516947528088612L;

    public JobTablePanel(GUI gui, final JobTableModel model) {
        setLayout(new BorderLayout());
        JTable table = new JTable(model);

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(30);
        table.setDragEnabled(false);
        table.setGridColor(Color.LIGHT_GRAY);
        //table.setIntercellSpacing(new Dimension(5, 5));

        JobTableRenderer renderer = new JobTableRenderer(model);

        TableRowSorter<JobTableModel> sorter = new TableRowSorter<JobTableModel>(
                model);
        sorter.setSortsOnUpdates(true);
        table.setRowSorter(sorter);

        TableColumn column = null;
        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            column.setCellRenderer(renderer);
            if (i == JobRow.CONTROL_COLUMN) {
                column.setCellEditor(new ButtonEditor());
                column.setMaxWidth(40);// first column is fixed
                column.setPreferredWidth(40);
                column.setMinWidth(40);
            } else if (i == JobRow.OUTPUT_COLUMN) {
                column.setCellEditor(new ButtonEditor());
                column.setMaxWidth(70);// last column is fixed
                column.setPreferredWidth(70);
                column.setMinWidth(70);
            } else {
                column.setPreferredWidth(80);
            }
        }

        // initialize table with all existing jobs
        for (JobDescription description : gui.getExperiment().getJobs()) {
            model.addJob(description, false);
        }

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
        add(new JobTableControlPanel(gui, table), BorderLayout.SOUTH);
    }

    private static class ButtonEditor implements TableCellEditor,
            ActionListener {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private Object object;

        private CellEditorListener listener;

        public Component getTableCellEditorComponent(JTable table,
                Object object, boolean isSelected, int row, int column) {
            this.object = object;
            Component component = table.getCellRenderer(row, column)
                    .getTableCellRendererComponent(table, object, isSelected,
                            true, row, column);
            ((JButton) component).addActionListener(this);
            return component;
        }

        public Object getCellEditorValue() {
            return object;
        }

        public void addCellEditorListener(CellEditorListener listener) {
            this.listener = listener;
        }

        public void cancelCellEditing() {
            this.listener.editingCanceled(new ChangeEvent(this));
        }

        public boolean isCellEditable(EventObject eventObject) {
            return true;
        }

        public void removeCellEditorListener(CellEditorListener listener) {
            listener = null;
        }

        public boolean shouldSelectCell(EventObject arg0) {
            return false;
        }

        public synchronized boolean stopCellEditing() {
            this.listener.editingStopped(new ChangeEvent(this));
            return true;
        }

        public synchronized void actionPerformed(ActionEvent arg0) {
            ((JButton) arg0.getSource()).removeActionListener(this);
            stopCellEditing();
        }
    }

}

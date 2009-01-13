package ibis.deploy.gui;

import ibis.deploy.JobDescription;
import ibis.deploy.gui.listener.WorkSpaceChangedListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class JobTablePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9193516947528088612L;

    public JobTablePanel(GUI gui, final JobTableModel model) {
        setLayout(new BorderLayout());
        JTable table = new JTable(model);
        model.setTable(table);

        table.setDefaultRenderer(Object.class, new JobTableRenderer(gui));
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(30);
        table.setDragEnabled(false);
        table.setIntercellSpacing(new Dimension(5, 5));

        TableColumn column = null;
        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setCellEditor(new ButtonEditor());
                column.setMaxWidth(30);// first column is fixed
                column.setPreferredWidth(30);
                column.setMinWidth(30);
            } else if (i == 9) {
                column.setCellEditor(new ButtonEditor());
                column.setMaxWidth(52);// last column is fixed
                column.setPreferredWidth(52);
                column.setMinWidth(52);
            } else {
                column.setPreferredWidth(80);
            }
        }

        for (JobDescription jobDescription : gui.getExperiment().getJobs()) {
            model.addRow(new JobRowObject(jobDescription, null));
        }

        gui.addExperimentWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                model.clear();
                for (JobDescription jobDescription : gui.getExperiment()
                        .getJobs()) {
                    model.addRow(new JobRowObject(jobDescription, null));
                }
                model.fireTableDataChanged();
            }

        });

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
            this.listener.editingCanceled(new ChangeEvent(object));
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
            this.listener.editingStopped(new ChangeEvent(object));
            return true;
        }

        public synchronized void actionPerformed(ActionEvent arg0) {
            ((JButton) arg0.getSource()).removeActionListener(this);
            stopCellEditing();
        }
    }

}

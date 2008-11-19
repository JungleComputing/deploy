package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JButton;
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

    public JobTablePanel(GUI gui, final MyTableModel model) {
        setLayout(new BorderLayout());
        JTable table = new JTable(model);
        model.setTable(table);

        table.setDefaultRenderer(Object.class, new MyTableRenderer(gui));
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(30);
        table.setDragEnabled(false);
        table.setIntercellSpacing(new Dimension(5, 5));

        TableColumn column = null;
        for (int i = 0; i < 5; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setCellEditor(new ButtonEditor());
                column.setPreferredWidth(30); // first column is smaller
            } else {
                column.setPreferredWidth(100);
            }
        }

        // table.setPreferredScrollableViewportSize(new Dimension(300, 70));
        // table.setFillsViewportHeight(true);

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
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

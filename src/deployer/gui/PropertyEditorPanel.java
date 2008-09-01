package deployer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import deployer.Deployer;
import deployer.PropertyCategory;
import deployer.PropertySet;
import deployer.PropertySetGroup;

public abstract class PropertyEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static int groupObjectID = 0;

    private static int objectID = 0;

    private Deployer deployer;

    public PropertyEditorPanel(Deployer deployer, String rootName) {
        super();
        setLayout(new BorderLayout());
        this.deployer = deployer;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootName);
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        JTree tree = new JTree(treeModel);
        tree.setCellEditor(new MyTreeCellEditor(tree,
                (DefaultTreeCellRenderer) tree.getCellRenderer()));
        tree.setEditable(true);
        tree.addMouseListener(new PropertyEditorMouseListener(tree));
        tree.setPreferredSize(new Dimension(300, 10));
        add(new JScrollPane(tree), BorderLayout.WEST);
        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.PAGE_AXIS));
        tree.addTreeSelectionListener(new PropertyTreeSelectionListener(
                editorPanel));
        add(editorPanel);
    }

    protected abstract PropertySetGroup load(Deployer deployer, String path)
            throws Exception;

    protected abstract PropertySet addGroupEntry(Deployer deployer,
            PropertySetGroup propertyGroupObject, int i) throws Exception;

    protected abstract PropertySetGroup addGroup(Deployer deployer, int i)
            throws Exception;

    protected abstract void removeGroup(Deployer deployer,
            PropertySetGroup group);

    protected abstract void removeGroupEntry(Deployer deployer,
            PropertySet entry);

    private static class MyTreeCellEditor extends DefaultTreeCellEditor {

        public MyTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
        }

        public boolean isCellEditable(EventObject object) {
            MouseEvent event = (MouseEvent) object;
            if (tree.getClosestPathForLocation(event.getX(), event.getY())
                    .getPathCount() == 1) {
                return false;
            } else {
                return true;
            }
        }

        public Object getCellEditorValue() {

            if (lastPath.getPathCount() == 2) {
                ((PropertySetGroup) ((DefaultMutableTreeNode) lastPath
                        .getLastPathComponent()).getUserObject())
                        .setName((String) realEditor.getCellEditorValue());
                return ((PropertySetGroup) ((DefaultMutableTreeNode) lastPath
                        .getLastPathComponent()).getUserObject());
            } else if (lastPath.getPathCount() == 3) {
                ((PropertySet) ((DefaultMutableTreeNode) lastPath
                        .getLastPathComponent()).getUserObject())
                        .setName((String) realEditor.getCellEditorValue());
                return ((PropertySet) ((DefaultMutableTreeNode) lastPath
                        .getLastPathComponent()).getUserObject());
            }
            return super.getCellEditorValue();
        }
    }

    private class PropertyEditorTableModelListener implements
            TableModelListener {
        private SortedMap<String, String> data;

        public PropertyEditorTableModelListener(SortedMap<String, String> data) {
            this.data = data;
        }

        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column != 1)
                return;
            TableModel model = (TableModel) e.getSource();
            Object value = model.getValueAt(row, 1);
            Object key = model.getValueAt(row, 0);
            data.put((String) key, (String) value);
        }
    }

    private class PropertyTreeSelectionListener implements
            TreeSelectionListener {

        JPanel parentPanel;

        public PropertyTreeSelectionListener(JPanel parentPanel) {
            this.parentPanel = parentPanel;
        }

        public void valueChanged(TreeSelectionEvent event) {
            if (event.getPath().getPathCount() < 2) {
                return;
            }
            // clean parentPanel
            parentPanel.removeAll();

            if (event.getPath().getPathCount() < 2) {
                return;
            }

            PropertySet propertySet = ((PropertySet) ((DefaultMutableTreeNode) event
                    .getPath().getLastPathComponent()).getUserObject());

            // draw tables
            for (PropertyCategory category : propertySet.getCategories()) {
                PropertyEditorTableModel model = new PropertyEditorTableModel(
                        new String[] { "Key", "Value" }, 0);
                for (String key : category.getData().keySet()) {
                    if (category.getData().get(key) == null
                            && propertySet.defaultValueFor(key, category
                                    .getName()) != null) {
                        model.addRow(new String[] {
                                key,
                                propertySet.defaultValueFor(key, category
                                        .getName())
                                        + " (default)" });
                        // default
                    } else {
                        model.addRow(new String[] { key,
                                category.getData().get(key) });
                    }
                }
                model
                        .addTableModelListener(new PropertyEditorTableModelListener(
                                category.getData()));
                JTable table = new JTable(model);

                JScrollPane pane = new JScrollPane(table);
                pane.setPreferredSize(new Dimension(
                        pane.getMaximumSize().width,
                        table.getPreferredSize().height + 40));
                pane.setMaximumSize(new Dimension(pane.getMaximumSize().width,
                        table.getPreferredSize().height + 40));
                pane.setBorder(BorderFactory.createTitledBorder(category
                        .getName()
                        + " properties"));
                parentPanel.add(pane);
                model.fireTableDataChanged();
            }
            parentPanel.getRootPane().repaint();
        }
    }

    private void updateTree(DefaultTreeModel treeModel, PropertySetGroup added) {
        MutableTreeNode root = (MutableTreeNode) treeModel.getRoot();
        if (added != null) {
            MutableTreeNode groupNode = new DefaultMutableTreeNode(added);
            treeModel.insertNodeInto(groupNode, root, root.getChildCount());
            for (PropertySet propertyObject : added.getPropertySets()) {
                MutableTreeNode newNode = new DefaultMutableTreeNode(
                        propertyObject);
                treeModel.insertNodeInto(newNode, groupNode, groupNode
                        .getChildCount());
            }
        }
    }

    private class PropertyEditorMouseListener implements MouseListener {

        private JTree tree;

        PropertyEditorMouseListener(JTree tree) {
            this.tree = tree;
        }

        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                TreePath currentSelection = tree.getSelectionPath();
                if (currentSelection != null) {

                    JPopupMenu menu = new JPopupMenu();
                    if (currentSelection.getPathCount() == 1) {
                        // root
                        JMenuItem newItem = new JMenuItem("add");
                        JMenuItem loadItem = new JMenuItem("load");
                        newItem.addActionListener(new PopupMenuActionListener(
                                tree));
                        loadItem.addActionListener(new PopupMenuActionListener(
                                tree));
                        menu.add(newItem);
                        menu.add(loadItem);
                    } else if (currentSelection.getPathCount() == 2) {
                        // container
                        JMenuItem addItem = new JMenuItem("add");
                        JMenuItem removeItem = new JMenuItem("remove");
                        JMenuItem saveItem = new JMenuItem("save");

                        addItem.addActionListener(new PopupMenuActionListener(
                                tree));
                        removeItem
                                .addActionListener(new PopupMenuActionListener(
                                        tree));
                        saveItem.addActionListener(new PopupMenuActionListener(
                                tree));
                        menu.add(addItem);
                        menu.add(removeItem);
                        menu.add(saveItem);
                    } else {
                        // leaf
                        JMenuItem removeItem = new JMenuItem("remove");
                        removeItem
                                .addActionListener(new PopupMenuActionListener(
                                        tree));
                        menu.add(removeItem);
                    }
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }

        private class PopupMenuActionListener implements ActionListener {

            JTree tree;

            PopupMenuActionListener(JTree tree) {
                this.tree = tree;
            }

            public void actionPerformed(ActionEvent e) {
                TreePath currentSelection = tree.getSelectionPath();
                if (currentSelection != null) {
                    JMenuItem item = (JMenuItem) e.getSource();
                    if (item.getText().equals("add")) {
                        Object newObject = null;
                        if (currentSelection.getPathCount() == 1) {
                            try {
                                newObject = addGroup(deployer, groupObjectID++);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        } else if (currentSelection.getPathCount() == 2) {
                            try {
                                newObject = addGroupEntry(
                                        deployer,
                                        (PropertySetGroup) ((DefaultMutableTreeNode) currentSelection
                                                .getPathComponent(1))
                                                .getUserObject(), objectID++);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            // ((ApplicationGroup) ((DefaultMutableTreeNode)
                            // currentSelection
                            // .getPathComponent(1)).getUserObject())
                            // .addApplication((Application) newObject);

                        }
                        if (newObject != null) {
                            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                                    newObject);

                            // It is key to invoke this on the TreeModel, and
                            // NOT
                            // DefaultMutableTreeNode
                            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (currentSelection
                                    .getLastPathComponent());
                            ((DefaultTreeModel) tree.getModel())
                                    .insertNodeInto(childNode, parent, parent
                                            .getChildCount());

                            // Make sure the user can see the lovely new node.
                            tree.scrollPathToVisible(new TreePath(childNode
                                    .getPath()));
                        }

                    } else if (item.getText().equals("remove")) {
                        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
                                .getLastPathComponent());
                        MutableTreeNode parent = (MutableTreeNode) (currentNode
                                .getParent());
                        if (parent != null) {
                            ((DefaultTreeModel) tree.getModel())
                                    .removeNodeFromParent(currentNode);
                            if (currentSelection.getPathCount() == 2) {
                                removeGroup(deployer,
                                        (PropertySetGroup) currentNode
                                                .getUserObject());
                                // remove group from deployer
                            } else {
                                removeGroupEntry(deployer,
                                        (PropertySet) currentNode
                                                .getUserObject());
                                // remove item from group
                            }
                            return;
                        }
                    } else if (item.getText().equals("load")) {
                        JFileChooser chooser = new JFileChooser(System
                                .getProperty("user.dir"));
                        chooser.setFileFilter(new FileFilter() {

                            public boolean accept(File f) {
                                return f.getName().endsWith(".properties")
                                        || f.isDirectory();
                            }

                            public String getDescription() {
                                return ".properties file";
                            }

                        });
                        int returnVal = chooser.showDialog(
                                PropertyEditorPanel.this, "Load");
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            PropertySetGroup added = null;
                            try {
                                added = load(deployer, chooser
                                        .getSelectedFile().getPath());
                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            updateTree((DefaultTreeModel) tree.getModel(),
                                    added);
                        }

                    } else if (item.getText().equals("save")) {
                        JFileChooser chooser = new JFileChooser(System
                                .getProperty("user.dir"));
                        chooser.setFileFilter(new FileFilter() {

                            public boolean accept(File f) {
                                return f.getName().endsWith(".properties")
                                        || f.isDirectory();
                            }

                            public String getDescription() {
                                return ".properties file";
                            }

                        });
                        int returnVal = chooser
                                .showSaveDialog(PropertyEditorPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                ((PropertySetGroup) ((DefaultMutableTreeNode) currentSelection
                                        .getPathComponent(1)).getUserObject())
                                        .save(chooser.getSelectedFile()
                                                .getPath()
                                                + (chooser.getSelectedFile()
                                                        .getPath().endsWith(
                                                                ".properties") ? ""
                                                        : ".properties"));
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                }

            }
        }

        public void mouseClicked(MouseEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void mouseEntered(MouseEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void mouseExited(MouseEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void mousePressed(MouseEvent arg0) {
            // TODO Auto-generated method stub

        }

        // public void mousePressed(MouseEvent e) {
        // Point pt = e.getPoint();
        // Object object = getObjectAt(pt); //Put your code to get the object
        // from tree here
        // if (object != null) {
        // if (e.isPopupTrigger()) {
        // setSelectionPath(getPathForLocation(pt.x, pt.y));
        // showMenu(object , e.getPoint()); //Have the tree display the pop up
        // menu here
        // }
        // }
        // }
    }

    private class PropertyEditorTableModel extends DefaultTableModel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public PropertyEditorTableModel(String[] strings, int i) {
            super(strings, i);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }
    }

}

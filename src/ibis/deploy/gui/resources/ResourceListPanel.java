package ibis.deploy.gui.resources;

import ibis.deploy.Resource;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.editor.EditorListener;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ResourceListPanel extends JPanel {

    private static final long serialVersionUID = -7171659896010561242L;

    private ResourceEditorPanel resourceEditorPanel;

    public ResourceListPanel(final GUI gui, final JPanel editPanel,
            final ResourceEditorPanel resourceEditorPanelRef) {

        this.resourceEditorPanel = resourceEditorPanelRef;

        setLayout(new BorderLayout());
        final DefaultListModel model = new DefaultListModel();
        final JList resourceList = new JList(model);
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final HashMap<Resource, JPanel> editResourcePanels = new HashMap<Resource, JPanel>();

        resourceList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                final Resource selectedResource = (Resource) resourceList
                        .getSelectedValue();
                editPanel.removeAll();
                editPanel.add(editResourcePanels.get(selectedResource),
                        BorderLayout.CENTER);
                editPanel.getRootPane().repaint();
            }
        });

        resourceEditorPanel.addEditorListener(new EditorListener() {

            public void edited(Object object) {
                resourceList.repaint();
            }

        });

        add(new ResourceListTopPanel(gui, resourceList, editResourcePanels,
                resourceEditorPanel), BorderLayout.NORTH);

        add(new JScrollPane(resourceList), BorderLayout.CENTER);

        init(gui, model, editResourcePanels);

        gui.addJungleWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                init(gui, model, editResourcePanels);
            }

        });

    }

    private void init(GUI gui, DefaultListModel model,
            Map<Resource, JPanel> editResourcePanels) {
        model.clear();

        for (Resource resource : gui.getJungle().getResources()) {
            model.addElement(resource);
        }

        // create a hash map with all the panels
        editResourcePanels.clear();
        for (Resource resource : gui.getJungle().getResources()) {
            editResourcePanels.put(resource, new ResourceEditorTabPanel(resource,
                    resourceEditorPanel, gui));
        }

    }
}

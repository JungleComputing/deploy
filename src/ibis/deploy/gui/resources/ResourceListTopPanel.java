package ibis.deploy.gui.resources;

import ibis.deploy.Resource;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.misc.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

public class ResourceListTopPanel extends JPanel {

	/**
     * 
     */
	private static final long serialVersionUID = 6183574615247336429L;

	private static long ID_COUNTER = 0;

	public ResourceListTopPanel(final GUI gui, final JList resourceList,
			final HashMap<Resource, JPanel> editResroucePanels,
			final ResourceEditorPanel resourceEditorPanel) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel buttonPanel = new JPanel();
		JButton addButton = Utils.createImageButton(
				"images/list-add-small.png", "Add resource", "Add");
		addButton.setPreferredSize(new Dimension(Utils.buttonWidth, addButton
				.getPreferredSize().height));
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				try {
					String resourceName = "New-Resource-" + ID_COUNTER;
					ID_COUNTER++;
					while (gui.getJungle().hasResource(resourceName)) {
						resourceName = "New-Resource-" + ID_COUNTER;
						ID_COUNTER++;
					}

					Resource newResource = new Resource(resourceName);
					gui.getJungle().addResource(newResource);
					((DefaultListModel) resourceList.getModel())
							.addElement(newResource);
					editResroucePanels.put(newResource,
							new ResourceEditorTabPanel(newResource,
									resourceEditorPanel, gui));
				} catch (Exception e) {
				}
				gui.fireJungleUpdated();
			}

		});
		buttonPanel.add(addButton);
		JButton removeButton = Utils.createImageButton(
				"images/list-remove-small.png", "Remove selected resource",
				"Remove");
		removeButton.setPreferredSize(new Dimension(Utils.buttonWidth,
				addButton.getPreferredSize().height));
		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				try {
					// make sure that there is something selected in the list
					if (resourceList.getSelectedIndex() >= 0) {
						Resource selectedResource = (Resource) ((DefaultListModel) resourceList
								.getModel()).get(resourceList.getSelectedIndex());
						(editResroucePanels.get(selectedResource)).getParent()
								.remove(editResroucePanels.get(selectedResource));
						editResroucePanels.remove(selectedResource);
						gui.getJungle().removeResource(selectedResource.getName());
						
						((DefaultListModel) resourceList.getModel())
								.removeElementAt(resourceList.getSelectedIndex());
						resourceEditorPanel.repaint();
						gui.fireJungleUpdated();
					}
				} catch (Exception e) {
					// ignore name is never null
				}
				gui.fireJungleUpdated();
			}

		});

		buttonPanel.add(removeButton);

		add(buttonPanel);
	}
}

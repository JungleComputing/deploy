package ibis.deploy.gui;

import ibis.deploy.gui.misc.Utils;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class DetachableTab {

	public DetachableTab(String title, String iconPath, JPanel contents,
			JTabbedPane tabPane) {

		int index = tabPane.getComponentCount();

		tabPane.add(contents);

		tabPane.setTabComponentAt(index, new DetachableTabComponent(tabPane, Utils
				.createImageIcon(iconPath, title), title));

	}

}

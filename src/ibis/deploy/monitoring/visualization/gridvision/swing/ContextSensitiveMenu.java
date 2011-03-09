package ibis.deploy.monitoring.visualization.gridvision.swing;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual;

public class ContextSensitiveMenu {	
	private JPopupMenu myMenu;
	private final ButtonGroup shapeGroup = new ButtonGroup();
	private final ButtonGroup collapseGroup = new ButtonGroup();
	
	public ContextSensitiveMenu(JGVisual caller) {		
		myMenu = new JPopupMenu();
			String[] collectionItems = {"Sphere","Cube","Cityscape"};
			GoggleAction al1 = new SetCollectionFormAction(caller, "");
		myMenu.add(makeRadioMenu("Collection Form", shapeGroup, collectionItems, al1));
			String[] collapseItems = {"Collapse","Unfold"};
			GoggleAction al2 = new SetCollapseAction(caller, "");
		myMenu.add(makeRadioMenu("Fold/unfold", collapseGroup, collapseItems, al2));
		
		
	}
	
	private JMenu makeRadioMenu(String name, ButtonGroup group, String[] labels, GoggleAction al) {
		JMenu result = new JMenu(name);
		
		for (String label : labels) {
			JRadioButtonMenuItem current = new JRadioButtonMenuItem(label);
			current.addActionListener(al.clone(label));
			result.add(current);
			group.add(current);
		}
		
		return result;
	}
	
	public JPopupMenu getMenu(JGVisual caller) {		
		return myMenu;
	}
}

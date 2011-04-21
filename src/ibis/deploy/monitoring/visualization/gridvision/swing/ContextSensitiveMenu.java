package ibis.deploy.monitoring.visualization.gridvision.swing;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import ibis.deploy.monitoring.visualization.gridvision.JGVisual;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.CollectionShape;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.State;

public class ContextSensitiveMenu extends JPopupMenu {
	private static final long serialVersionUID = -2817492958876235518L;
		
	public ContextSensitiveMenu(JGVisual caller) {
		String currentSelection = "";
								
			String[] collectionItems = {"Sphere","Cube","Cityscape"};
			CollectionShape cShape = caller.getCollectionShape();
			if (cShape == CollectionShape.CITYSCAPE) 	currentSelection = "Cityscape";
			else if (cShape == CollectionShape.SPHERE) 	currentSelection = "Sphere";
			else if (cShape == CollectionShape.CUBE) 	currentSelection = "Cube";
			
			ButtonGroup shapeGroup = new ButtonGroup();
			GoggleAction al1 = new SetCollectionFormAction(caller.getParent(), currentSelection);
		add(makeRadioMenu("Collection Form", shapeGroup, collectionItems, currentSelection, al1));
		
			String[] collapseItems = {"Collapse","Unfold"};
			State fstate = caller.getState();
			if (fstate == State.COLLAPSED) 		currentSelection = "Collapse";
			else if (fstate == State.UNFOLDED) 	currentSelection = "Unfold";
			
			ButtonGroup collapseGroup = new ButtonGroup();
			GoggleAction al2 = new SetCollapseAction(caller, currentSelection);
		add(makeRadioMenu("Fold/unfold", collapseGroup, collapseItems, currentSelection, al2));
	}
	
	private JMenu makeRadioMenu(String name, ButtonGroup group, String[] labels, String currentSelection, GoggleAction al) {
		JMenu result = new JMenu(name);
		
		for (String label : labels) {
			JRadioButtonMenuItem current = new JRadioButtonMenuItem(label);
			current.addActionListener(al.clone(label));
			result.add(current);
			group.add(current);
			if (currentSelection.compareTo(label) == 0) {
				group.setSelected(current.getModel(), true);
			} else {
				group.setSelected(current.getModel(), false);
			}
		}
		
		return result;
	}
}

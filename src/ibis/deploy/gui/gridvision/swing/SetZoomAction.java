package ibis.deploy.gui.gridvision.swing;

import ibis.deploy.gui.gridvision.GridVision;
import ibis.deploy.gui.gridvision.exceptions.ModeUnknownException;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetZoomAction extends AbstractAction {	
	private static final long serialVersionUID = 5697006330014878291L;
	
	GridVision perfvis;
	int myZoom;
	
	public SetZoomAction(GridVision perfvis, String label) {
		super(label);
		
		this.perfvis = perfvis;
		
		/*
		if (label.compareTo("Pools") == 0) 		myZoom = PerfVis.ZOOM_POOLS;
		else if (label.compareTo("Sites") == 0) myZoom = PerfVis.ZOOM_SITES;
		else if (label.compareTo("Nodes") == 0) myZoom = PerfVis.ZOOM_NODES;
		*/
	}

	public void actionPerformed(ActionEvent e) {
		/*
		try {
			perfvis.setZoom(myZoom);
		} catch (ModeUnknownException e1) {			
			e1.printStackTrace();
		}
		*/	
	}
}

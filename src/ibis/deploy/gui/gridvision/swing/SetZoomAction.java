package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SetZoomAction extends AbstractAction {	
	private static final long serialVersionUID = 5697006330014878291L;
	
	PerfVis perfvis;
	int myZoom;
	
	public SetZoomAction(PerfVis perfvis, String label) {
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

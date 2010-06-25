package ibis.deploy.gui.performance.swing;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.performance.PerformancePanel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class GridVisionAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private GUI gui;
    private PerformancePanel panel;

    public GridVisionAction(GUI gui, PerformancePanel panel) {
        super("GridVision");
        this.gui = gui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            gui.getRootPanel().toggleGridVisionPane(gui, panel);            
        } catch (Exception e) {            
            return;
        }        
    }

}

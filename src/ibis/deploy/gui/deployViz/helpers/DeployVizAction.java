package ibis.deploy.gui.deployViz.helpers;


import ibis.deploy.gui.GUI;
import ibis.deploy.gui.deployViz.DeployVizPanel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class DeployVizAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private GUI gui;
    private DeployVizPanel panel;

    public DeployVizAction(GUI gui, DeployVizPanel panel) {
        super("DeployViz");
        this.gui = gui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            gui.getRootPanel().toggleDeployVizPane(gui, panel);            
        } catch (Exception e) {            
            return;
        }        
    }

}

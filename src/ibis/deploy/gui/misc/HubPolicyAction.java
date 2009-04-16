package ibis.deploy.gui.misc;

import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class HubPolicyAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private boolean sharedHubs;

    private GUI gui;

    public HubPolicyAction(String label, boolean sharedHubs, GUI gui) {
        super(label);
        this.gui = gui;
        this.sharedHubs = sharedHubs;
    }

    public void actionPerformed(ActionEvent arg0) {
        gui.setSharedHubs(sharedHubs);
    }

}

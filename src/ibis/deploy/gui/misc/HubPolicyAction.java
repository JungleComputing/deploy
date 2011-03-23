package ibis.deploy.gui.misc;

import ibis.deploy.Deploy.HubPolicy;
import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class HubPolicyAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private HubPolicy policy;

    private GUI gui;

    public HubPolicyAction(String label, HubPolicy policy, GUI gui) {
        super(label);
        this.gui = gui;
        this.policy = policy;
    }

    public void actionPerformed(ActionEvent arg0) {
        gui.getDeploy().setHubPolicy(policy);
    }

}

package ibis.deploy.gui.misc;

import ibis.deploy.gui.GUI;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SmartSocketsVizAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JFrame frame;
    private GUI gui;

    public SmartSocketsVizAction(JFrame frame, GUI gui) {
        super("Smartsockets Visualization");
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent event) {
        
        
        JPanel smartSocketsPanel = new JPanel();

        try {
            DirectSocketAddress rootHub = DirectSocketAddress.getByAddress(gui
                    .getDeploy().getRootHubAddress());
            smartSocketsPanel = new SmartsocketsViz(Color.BLACK, Color.WHITE,false,
                    rootHub);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Cannot start visualization: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFrame window = new JFrame("SmartSockets Visualization");
        window.setLocationRelativeTo(frame);
        
        window.setContentPane(smartSocketsPanel);
        window.setPreferredSize(new Dimension(1024,768));
        window.pack();
        window.setVisible(true);
    }

}

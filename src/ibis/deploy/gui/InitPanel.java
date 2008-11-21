package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class InitPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -902160021134028192L;

    public InitPanel(final GUI gui, final InitializingPanel listener) {
        setLayout(new BorderLayout());
        final WorldMapPanel worldMap = new WorldMapPanel(gui);
        add(worldMap, BorderLayout.CENTER);
        final JButton initButton = new JButton("init");
        initButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    initButton.setEnabled(false);
                    listener.init();
                    gui.getDeploy().initialize(worldMap.getSelectedCluster(),
                            null, listener);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            e.getMessage(), "Initialize failed",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                }
            }
        });
        add(initButton, BorderLayout.SOUTH);
    }

}

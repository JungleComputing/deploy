package ibis.deploy.vizFramework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.deployViz.helpers.VizUtils;
import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;

import javax.media.opengl.awt.GLJPanel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class GlobePanel extends JPanel {
    private static final long serialVersionUID = 4754345291079348455L;

    GlobeVisualization globe;

    public GlobePanel(GUI gui) {
        setLayout(new BorderLayout(5, 5));

        JPanel leftPanel = new JPanel();
        BoxLayout verticalLayout = new BoxLayout(leftPanel,
                BoxLayout.PAGE_AXIS);
        leftPanel.setLayout(verticalLayout);

        globe = new GlobeVisualization(gui);
        
        leftPanel.add(new JLabel("Edge type:    "));
        
        // radio buttons for switching between layouts
        ButtonGroup radioGroup = new ButtonGroup();
        JRadioButton arcRadio = new JRadioButton("Arc");
        radioGroup.add(arcRadio);
        arcRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    globe.setFollowTerrain(false);
                } catch (ClassCastException exc) {
                    exc.printStackTrace();
                }
            }
        });
        arcRadio.setSelected(true);
        leftPanel.add(arcRadio);

        JRadioButton terrainRadio = new JRadioButton("Terrain");
        radioGroup.add(terrainRadio);
        terrainRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                globe.setFollowTerrain(true);
            }
        });
        leftPanel.add(terrainRadio);

        // Set up the window
        add(globe, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        globe.requestFocusInWindow();
    }

    public GlobeVisualization getGlobe() {
        return globe;
    }
}

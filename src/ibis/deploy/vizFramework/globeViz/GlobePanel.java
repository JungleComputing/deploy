package ibis.deploy.vizFramework.globeViz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.deploy.gui.GUI;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

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
        
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        //Change polyline visibility
        JCheckBox cb = new JCheckBox("Polylines visible");
        cb.setSelected(true);
        cb.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent event) {
                globe.setPolylinesEnabled(((JCheckBox)event.getSource()).isSelected());
            }
        });
        leftPanel.add(cb);
        
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        leftPanel.add(new JLabel("Display type:    "));
        
        // radio buttons for switching between layouts
        ButtonGroup particleButtonGroup = new ButtonGroup();

        JRadioButton showParticlesRadio = new JRadioButton("Particle encoding");
        particleButtonGroup.add(showParticlesRadio);
        showParticlesRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                globe.setShowParticles(true);
            }
        });
        showParticlesRadio.setSelected(true);
        leftPanel.add(showParticlesRadio);
        
        JRadioButton showArcsRadio = new JRadioButton("Edge weight encoding");
        particleButtonGroup.add(showArcsRadio);
        showArcsRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    globe.setShowParticles(false);
                } catch (ClassCastException exc) {
                    exc.printStackTrace();
                }
            }
        });
        leftPanel.add(showArcsRadio);    
        
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(Box.createVerticalGlue());

        // Set up the window
        add(globe, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        globe.requestFocusInWindow();
    }

    public GlobeVisualization getGlobe() {
        return globe;
    }
}

package ibis.deploy.vizFramework.globeViz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ibis.deploy.gui.GUI;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.impl.CollectorImpl;
import ibis.deploy.vizFramework.VisualizationPanel;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;
import ibis.deploy.vizFramework.persistence.XMLImporter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * @author Ana Vinatoru
 *
 */

public class GlobePanel extends JPanel {
    private static final long serialVersionUID = 4754345291079348455L;

    GlobeVisualization globe;

    public GlobePanel(GUI gui) {
        setLayout(new BorderLayout(5, 5));

        JPanel leftPanel = new JPanel();
        BoxLayout bLayout = new BoxLayout(leftPanel,
                BoxLayout.PAGE_AXIS);
        leftPanel.setLayout(bLayout);
        leftPanel.add(Box.createRigidArea(new Dimension(30, 15)));
       

        globe = new GlobeVisualization(gui);
        

        JLabel edgeTypeLabel = new JLabel("Connection type:    ");
        Font newLabelFont = new Font(edgeTypeLabel.getFont().getName(),Font.BOLD,edgeTypeLabel.getFont().getSize());
        edgeTypeLabel.setFont(newLabelFont);
        leftPanel.add(edgeTypeLabel);
        
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

        JRadioButton terrainRadio = new JRadioButton("Follow terrain");
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
        cb.setFont(newLabelFont);
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
        
        JLabel displayTypeLabel = new JLabel("Display type:    ");
        displayTypeLabel.setFont(newLabelFont);
        leftPanel.add(displayTypeLabel);
        
        // radio buttons for switching between layouts
        ButtonGroup particleButtonGroup = new ButtonGroup();

        JRadioButton showParticlesRadio = new JRadioButton("Historical (particles)");
        particleButtonGroup.add(showParticlesRadio);
        showParticlesRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                globe.setShowParticles(true);
            }
        });
        showParticlesRadio.setSelected(true);
        leftPanel.add(showParticlesRadio);
        
        JRadioButton showArcsRadio = new JRadioButton("Real-time (edges)");
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
        
        Component rigidArea = Box.createRigidArea(new Dimension(10, 350));
        leftPanel.add(rigidArea);

        // Set up the window
        add(globe, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        globe.requestFocusInWindow();
    }

    public GlobeVisualization getGlobe() {
        return globe;
    }
}

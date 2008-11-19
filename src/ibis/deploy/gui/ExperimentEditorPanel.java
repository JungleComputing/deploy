package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ExperimentEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -8580838957929000835L;

    private final static int SPACER = 5;

    public ExperimentEditorPanel(GUI gui, MyTableModel model) {
        // setMinimumSize(new Dimension(100, 100));
        setBorder(BorderFactory.createTitledBorder("Experiment Editor"));
        setLayout(new BorderLayout(5, 5));
        WorldMapPanel worldMapPanel = new WorldMapPanel(gui);
        add(worldMapPanel, BorderLayout.CENTER);
        add(new ExperimentEditorTextPanel(gui, model, worldMapPanel),
                BorderLayout.EAST);

        // setBorder(BorderFactory.createTitledBorder("Experiment Editor"));
        // SpringLayout layout = new SpringLayout();
        //
        // WorldMapPanel worldMapPanel = new WorldMapPanel(gui, true);
        // ApplicationSelectionPanel applicationSelectionPanel = new
        // ApplicationSelectionPanel(
        // gui);
        // ProcessCountPanel processCountPanel = new ProcessCountPanel(gui);
        // ResourceCountPanel resourceCountPanel = new ResourceCountPanel(gui);
        // RunButtonPanel runButtonPanel = new RunButtonPanel(gui, model);
        //
        // add(worldMapPanel);
        // add(applicationSelectionPanel);
        // add(processCountPanel);
        // add(resourceCountPanel);
        // add(runButtonPanel);
        //
        // setLayout(layout);
        //
        // // world map constraints
        // layout.putConstraint(SpringLayout.WEST, worldMapPanel, SPACER,
        // SpringLayout.WEST, this);
        // layout.putConstraint(SpringLayout.SOUTH, worldMapPanel, -SPACER,
        // SpringLayout.SOUTH, this);
        // layout.putConstraint(SpringLayout.EAST, worldMapPanel, -SPACER,
        // SpringLayout.WEST, resourceCountPanel);
        // layout.putConstraint(SpringLayout.NORTH, worldMapPanel, SPACER,
        // SpringLayout.SOUTH, applicationSelectionPanel);
        // layout.putConstraint(SpringLayout.NORTH, worldMapPanel, SPACER,
        // SpringLayout.SOUTH, processCountPanel);
        //
        // // application selection constraints
        // layout.putConstraint(SpringLayout.EAST, applicationSelectionPanel, 0,
        // SpringLayout.EAST, worldMapPanel);
        // layout.putConstraint(SpringLayout.VERTICAL_CENTER,
        // applicationSelectionPanel, 0, SpringLayout.VERTICAL_CENTER,
        // processCountPanel);
        //
        // // process count constraints
        // layout.putConstraint(SpringLayout.NORTH, processCountPanel, 5,
        // SpringLayout.NORTH, this);
        // layout.putConstraint(SpringLayout.WEST, processCountPanel, SPACER,
        // SpringLayout.EAST, applicationSelectionPanel);
        // layout.putConstraint(SpringLayout.EAST, processCountPanel, -SPACER,
        // SpringLayout.EAST, this);
        //
        // // resource count constraints
        // layout.putConstraint(SpringLayout.EAST, resourceCountPanel, -SPACER,
        // SpringLayout.EAST, this);
        // layout.putConstraint(SpringLayout.NORTH, resourceCountPanel, 0,
        // SpringLayout.NORTH, worldMapPanel);
        //
        // // run button panel constraints
        // layout.putConstraint(SpringLayout.NORTH, runButtonPanel, SPACER,
        // SpringLayout.SOUTH, resourceCountPanel);
        // layout.putConstraint(SpringLayout.EAST, runButtonPanel, -SPACER,
        // SpringLayout.EAST, this);
        // layout.putConstraint(SpringLayout.WEST, runButtonPanel, 0,
        // SpringLayout.WEST, resourceCountPanel);
        // layout.putConstraint(SpringLayout.SOUTH, runButtonPanel, -SPACER,
        // SpringLayout.SOUTH, this);

    }

}

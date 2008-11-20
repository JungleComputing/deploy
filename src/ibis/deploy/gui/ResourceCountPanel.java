package ibis.deploy.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ResourceCountPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6119634435992528982L;

    private final JSpinner resourceCountSpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

    private GUI gui;

    public ResourceCountPanel(final GUI gui, final WorldMapPanel worldMapPanel) {
        this.gui = gui;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(new JLabel("x"));

        resourceCountSpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                gui.getCurrentJobDescription().setResourceCount(
                        ((SpinnerNumberModel) resourceCountSpinner.getModel())
                                .getNumber().intValue());
                worldMapPanel
                        .setResourceCount(((SpinnerNumberModel) resourceCountSpinner
                                .getModel()).getNumber().intValue());

            }

        });
        resourceCountSpinner.setPreferredSize(new Dimension(50,
                resourceCountSpinner.getPreferredSize().height));
        add(resourceCountSpinner);
        gui.getCurrentJobDescription().setResourceCount(1);
    }

    protected void setResourceCount(int i) {
        ((SpinnerNumberModel) resourceCountSpinner.getModel())
                .setValue(new Integer(i));
        gui.getCurrentJobDescription().setResourceCount(
                ((SpinnerNumberModel) resourceCountSpinner.getModel())
                        .getNumber().intValue());
    }

}
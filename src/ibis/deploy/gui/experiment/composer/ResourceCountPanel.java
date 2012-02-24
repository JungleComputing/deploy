package ibis.deploy.gui.experiment.composer;

import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.worldmap.WorldMapPanel;

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

    private final JSpinner resourceCountSpinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));

    public ResourceCountPanel(final GUI gui, final WorldMapPanel worldMapPanel) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(new JLabel("x"));

        gui.addSubmitJobListener(new SubmitJobListener() {

            public void modify(JobDescription jobDescription) {
                jobDescription.setResourceCount(((SpinnerNumberModel) resourceCountSpinner.getModel()).getNumber()
                        .intValue());
            }

        });

        resourceCountSpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                worldMapPanel.setResourceCount(((SpinnerNumberModel) resourceCountSpinner.getModel()).getNumber()
                        .intValue());

            }

        });
        resourceCountSpinner.setPreferredSize(new Dimension(50, 25));
        add(resourceCountSpinner);
    }

    protected void setResourceCount(int i) {
        ((SpinnerNumberModel) resourceCountSpinner.getModel()).setValue(new Integer(i));
    }

}

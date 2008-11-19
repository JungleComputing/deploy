package ibis.deploy.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProcessCountPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1677946251801621514L;

    public ProcessCountPanel(final GUI gui) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(new JLabel("x"));
        final JSpinner processCountSpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        processCountSpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent arg0) {
                gui.getCurrentJobDescription().setProcessCount(
                        ((SpinnerNumberModel) processCountSpinner.getModel())
                                .getNumber().intValue());

            }

        });
        processCountSpinner.setPreferredSize(new Dimension(50,
                processCountSpinner.getPreferredSize().height));
        gui.getCurrentJobDescription().setProcessCount(1);
        add(processCountSpinner);

    }
}

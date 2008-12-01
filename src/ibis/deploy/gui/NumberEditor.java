package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumberEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 2216106418941074147L;

    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1,
            Integer.MAX_VALUE, 1));

    private boolean usingDefault;

    public NumberEditor(JPanel form, final String text, int value,
            int defaultValue) {
        // determine whether this text editor holds a default value
        usingDefault = (value <= 0 && defaultValue > 0);

        // set the text of the text field to the appropriate value
        spinner.setValue((usingDefault) ? defaultValue : Math.max(value, 1));

        // create the label
        final JLabel label = new JLabel(text, JLabel.TRAILING);
        if (usingDefault) {
            label.setText("<html><i>" + text + "</i></html>");
        }
        // add a background color, which can only been seen if the default is
        // used
        label.setBackground(Color.lightGray);
        label.setOpaque(usingDefault);

        if (usingDefault) {
            spinner.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent arg0) {
                    // no longer using the default
                    usingDefault = false;
                    // and change the background of this label
                    label.setOpaque(usingDefault);
                    label.setText(text);
                    label.repaint();
                    // and remove this action listener too
                    spinner.removeChangeListener(this);
                }

            });
        }

        JPanel container = new JPanel(new BorderLayout());
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(label, BorderLayout.WEST);
        label.setLabelFor(spinner);
        container.add(spinner);
        form.add(container);
    }

    public int getValue() {
        if (usingDefault) {
            return -1;
        } else {
            return ((SpinnerNumberModel) spinner.getModel()).getNumber()
                    .intValue();
        }
    }

}

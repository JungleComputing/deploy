package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumberEditor extends ChangeableField implements ChangeListener {

    private final JSpinner spinner;

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private int initialValue;

    /**
     * @param form
     *            - parent panel
     * @param text
     *            - label text
     * @param value
     *            - initial value for the spinner
     * @param startFromZero
     *            - if it is true, the spinner's start value is zero, otherwise,
     *            it is 1
     */
    public NumberEditor(JPanel tabPanel, JPanel form, final String text,
            int value, boolean startFromZero) {

        // initialize the spinner
        if (value > 0) {
            if (startFromZero) {
                spinner = new JSpinner(new SpinnerNumberModel(value, 0,
                        Integer.MAX_VALUE, 1));
            } else {
                spinner = new JSpinner(new SpinnerNumberModel(value, 1,
                        Integer.MAX_VALUE, 1));
            }
            initialValue = value;

        } else {
            if (startFromZero) {
                spinner = new JSpinner(new SpinnerNumberModel(0, 0,
                        Integer.MAX_VALUE, 1));
                initialValue = 0;
            } else {
                spinner = new JSpinner(new SpinnerNumberModel(1, 1,
                        Integer.MAX_VALUE, 1));
                initialValue = 1;
            }

        }

        this.tabPanel = tabPanel;
        spinner.addChangeListener(this);

        JPanel container = new JPanel(new BorderLayout());

        // make the container higher, so that all fields in the parent panel are
        // the same height
        container.setPreferredSize(new Dimension(
                label.getPreferredSize().width, Utils.defaultFieldHeight));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(spinner);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth, label
                .getPreferredSize().height));

        container.add(spinner, BorderLayout.CENTER);
        form.add(container);
    }

    /**
     * @return - the value stored by the spinner
     */
    public int getValue() {
        return ((SpinnerNumberModel) spinner.getModel()).getNumber().intValue();
    }

    /**
     * @param value
     *            - new value for the spinner
     */
    public void setValue(int value) {
        spinner.setValue(value);
    }

    public void refreshInitialValue() {
        initialValue = ((SpinnerNumberModel) spinner.getModel()).getNumber()
                .intValue();
    }

    @Override
    public boolean hasChanged() {
        return getValue() != initialValue;
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        informParent();
    }
}

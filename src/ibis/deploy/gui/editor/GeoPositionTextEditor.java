package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

public class GeoPositionTextEditor {

    private final JFormattedTextField textField;

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private final NumberFormatter formatter;

    public GeoPositionTextEditor(JPanel parent, final String text,
            Object value, boolean fitLabel) {
        JPanel container = new JPanel(new BorderLayout());
        if (fitLabel)
            container.setPreferredSize(new Dimension(
                    label.getPreferredSize().width, Utils.defaultFieldHeight));

        label.setText(text);
        if (fitLabel)
            label.setPreferredSize(new Dimension(Utils.defaultLabelWidth, label
                    .getPreferredSize().height));
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);

        // set the format for the textfields - they will only allow a maximum of
        // 4 decimal and a minimum of 1
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(1);

        textField = new JFormattedTextField(numberFormat);
        textField.setColumns(9);
        formatter = ((NumberFormatter) textField.getFormatter());
        formatter.setCommitsOnValidEdit(true);
        formatter.setAllowsInvalid(false);// invalid values - containing letters
        // and such - are automatically
        // discarded

        // set the text of the text field to the appropriate value
        if (value != null)
            textField.setValue(value);

        label.setLabelFor(textField);

        container.add(textField, BorderLayout.CENTER);
        parent.add(container);
    }

    /**
     * Adds a property change listener for "value" on the JFormattedTextField
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        textField.addPropertyChangeListener("value", listener);
    }

    /**
     * Sets the maximum bound for the text field
     * 
     * @param max
     *            - maximum value
     */
    public void setMaximum(double max) {
        formatter.setMaximum(new Double(max));
    }

    /**
     * Sets the minimum bound for the text field
     * 
     * @param min
     *            - minimum value
     */
    public void setMinimum(double min) {
        formatter.setMinimum(new Double(min));
    }

    /**
     * Sets a new value for the text field
     * 
     * @param value
     *            - new value
     */
    public void setValue(Object value) {
        textField.setValue(value);
    }

    /**
     * @return - the current value of the text field
     */
    public Object getValue() {
        return textField.getValue();
    }

    /**
     * @return - a reference to the editor's JFormattedTextField
     */
    public JFormattedTextField getTextField() {
        return textField;
    }
}

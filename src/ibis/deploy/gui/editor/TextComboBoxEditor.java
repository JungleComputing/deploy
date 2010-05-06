package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextComboBoxEditor extends ChangeableField implements
        ActionListener {

    private final JComboBox comboBox = new JComboBox();

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private JPanel parentPanel;

    private final String[] possibleValues;

    private String initialValue;

    private final String noSelectionText = "-";

    /**
     * @param form
     *            - parent panel
     * @param text
     *            - label text
     * @param value
     *            - selected value
     * @param possibleValues
     *            - list of possible values for the combobox
     */
    public TextComboBoxEditor(final JPanel tabPanel, JPanel form,
            final String text, String value, String[] possibleValues) {

        this.parentPanel = form;
        this.possibleValues = possibleValues;
        this.tabPanel = tabPanel;

        if (value != null) {
            initialValue = value;
        } else {
            initialValue = noSelectionText;
        }

        // initialize combobox
        comboBox.addItem(noSelectionText);
        for (String possibleValue : possibleValues) {
            comboBox.addItem(possibleValue);
            if (possibleValue.equalsIgnoreCase(value)) {
                comboBox.setSelectedItem(possibleValue);
            }

        }
        comboBox.addActionListener(this);

        JPanel container = new JPanel(new BorderLayout());

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(comboBox);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth, label
                .getPreferredSize().height));

        container.add(comboBox, BorderLayout.CENTER);
        form.add(container);
    }

    /**
     * @return - selected value in the combobox
     */
    public String getText() {
        if (comboBox.getSelectedIndex() > 0) {
            return comboBox.getSelectedItem().toString();
        }
        return null;
    }

    /**
     * Sets the selected item in the combobox to a new value
     * 
     * @param value
     *            - new selected value
     */
    public void setText(String value) {
        for (String possibleValue : possibleValues) {
            if (possibleValue.equalsIgnoreCase(value)) {
                comboBox.setSelectedItem(possibleValue);
            }
        }
        parentPanel.getRootPane().repaint();
    }

    @Override
    public void refreshInitialValue() {
        initialValue = getText() != null ? getText() : noSelectionText;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        informParent();
    }

    /**
     * @return - true if the selected item in the combobox is different from the
     *         initial value.
     */
    public boolean hasChanged() {
        return !comboBox.getSelectedItem().toString().equalsIgnoreCase(
                initialValue);
    }

}

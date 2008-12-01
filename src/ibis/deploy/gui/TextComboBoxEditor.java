package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextComboBoxEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 2216106418941074147L;

    private final JComboBox comboBox = new JComboBox();

    private boolean usingDefault;

    public TextComboBoxEditor(JPanel form, String text, Object toStringValue,
            Object defaultToStringValue, String[] possibleValues) {
        this(form, text, (toStringValue == null) ? (String) null
                : toStringValue.toString(),
                (defaultToStringValue == null) ? (String) null
                        : defaultToStringValue.toString(), possibleValues);
    }

    public TextComboBoxEditor(JPanel form, final String text, String value,
            String defaultValue, String[] possibleValues) {
        // determine whether this text editor holds a default value
        usingDefault = (value == null && defaultValue != null);

        //
        for (String possibleValue : possibleValues) {
            comboBox.addItem(possibleValue);
            if (usingDefault) {
                if (defaultValue.equalsIgnoreCase(possibleValue)) {
                    comboBox.setSelectedItem(possibleValue);
                }
            } else {
                if (value.equalsIgnoreCase(possibleValue)) {
                    comboBox.setSelectedItem(possibleValue);
                }
            }
        }

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
            comboBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    // no longer using the default
                    usingDefault = false;
                    // and change the background of this label
                    label.setOpaque(usingDefault);
                    label.setText(text);
                    label.repaint();
                    // and remove this action listener too
                    comboBox.removeActionListener(this);
                }

            });
        }

        JPanel container = new JPanel(new BorderLayout());
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(label, BorderLayout.WEST);
        label.setLabelFor(comboBox);
        container.add(comboBox);
        form.add(container);
    }

    public String getText() {
        if (usingDefault) {
            return null;
        } else {
            return comboBox.getSelectedItem().toString();
        }
    }

}

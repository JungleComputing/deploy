package ibis.deploy.gui.editor;

import ibis.deploy.gui.GUIUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextComboBoxEditor {

    private final JComboBox comboBox = new JComboBox();

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final String defaultValue;

    private final JButton openButton = GUIUtils.createImageButton(
            "/images/document-open.png", "select a file", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    public TextComboBoxEditor(JPanel form, final String text, String value,
            String defaultValue, String[] possibleValues) {
        this.defaultValue = defaultValue;

        // determine whether this text editor holds a default value
        final boolean useDefault = (value == null && defaultValue != null);

        // set the check box
        useDefaultCheckBox
                .setToolTipText("<html>enable for <code><i>default</i></code> value</html>");
        // if the value is 'nullified' or 'denullified' enable or disable the
        // editing components and invoke the edited method
        useDefaultCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUseDefault(!useDefaultCheckBox.isSelected());
            }
        });

        // initialize the components in enabled or disabled state
        setUseDefault(useDefault);

        for (String possibleValue : possibleValues) {
            comboBox.addItem(possibleValue);
            if (useDefault) {
                if (possibleValue.equalsIgnoreCase(defaultValue)) {
                    comboBox.setSelectedItem(possibleValue);
                }
            } else {
                if (possibleValue.equalsIgnoreCase(value)) {
                    comboBox.setSelectedItem(possibleValue);
                }
            }

        }

        JPanel container = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(useDefaultCheckBox, BorderLayout.WEST);
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(comboBox);
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(comboBox, BorderLayout.CENTER);
        form.add(container);
    }

    public String getText() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            return comboBox.getSelectedItem().toString();
        }
    }

    private void setUseDefault(boolean useDefault) {
        useDefaultCheckBox.setSelected(!useDefault);

        if (useDefault) {
            comboBox.setSelectedItem(defaultValue);
        }
        label.setEnabled(!useDefault);
        comboBox.setEnabled(!useDefault);
        openButton.setEnabled(!useDefault);
    }

}

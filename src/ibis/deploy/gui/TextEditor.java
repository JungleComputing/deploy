package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextEditor {

    private final JTextField textField = new JTextField();

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final String defaultValue;

    private final JButton openButton = GUIUtils.createImageButton(
            "/images/document-open.png", "select a file", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    public TextEditor(JPanel form, final String text, Object value,
            Object defaultValue) {
        this(form, text, (value == null) ? null : value.toString(),
                (defaultValue == null) ? null : defaultValue.toString());
    }

    public TextEditor(JPanel form, final String text, String value,
            String defaultValue) {
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

        // set the text of the text field to the appropriate value
        if (value != null && !useDefault) {
            textField.setText(value);
        }

        JPanel container = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(useDefaultCheckBox, BorderLayout.WEST);
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(textField);
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(textField, BorderLayout.CENTER);
        form.add(container);
    }

    public String getText() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            return textField.getText();
        }
    }

    private void setUseDefault(boolean useDefault) {
        useDefaultCheckBox.setSelected(!useDefault);

        if (useDefault) {
            textField.setText(defaultValue);
        }
        label.setEnabled(!useDefault);
        textField.setEnabled(!useDefault);
        openButton.setEnabled(!useDefault);
    }

}

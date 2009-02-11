package ibis.deploy.gui.editor;

import ibis.deploy.gui.GUIUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class TextMapArrayEditor {

    private final List<JTextField> keyTextFields = new ArrayList<JTextField>();

    private final List<JTextField> valueTextFields = new ArrayList<JTextField>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = GUIUtils.createImageButton(
            "/images/list-add-small.png", "add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private final Map<String, String> defaultValues;

    private JPanel parent;

    public TextMapArrayEditor(final JPanel form, final String text,
            Map<String, String> values, Map<String, String> defaultValues) {
        this.parent = form;
        this.defaultValues = defaultValues;

        // determine whether this text editor holds a default value
        boolean useDefault = values == null;

        // set the check box
        useDefaultCheckBox
                .setToolTipText("<html>check this box to overwrite the <code><i>default</i></code> value(s)</html>");
        // if the value is 'nullified' or 'denullified' enable or disable the
        // editing components and invoke the edited method
        useDefaultCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUseDefault(!useDefaultCheckBox.isSelected());
            }
        });

        // initialize the components in enabled or disabled state
        setUseDefault(useDefault);

        // set the text of the text fields to the appropriate values
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        if (!useDefault) {
            if (values != null) {
                for (String key : values.keySet()) {
                    addField(key, values.get(key), arrayPanel);
                }
            }
        }

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, null, arrayPanel);
                setUseDefault(false);
                arrayPanel.getRootPane().repaint();
            }

        });

        addPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(addPanel);

        JPanel container = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(useDefaultCheckBox, BorderLayout.WEST);
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(arrayPanel);
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(arrayPanel, BorderLayout.CENTER);
        form.add(container);
    }

    private void addField(String key, String value, final JPanel panel) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField keyTextField = new JTextField(key);
        keyTextFields.add(keyTextField);
        final JTextField valueTextField = new JTextField(value);
        valueTextFields.add(valueTextField);

        SpringLayout layout = new SpringLayout();
        JPanel internalPanel = new JPanel(layout);

        internalPanel.add(keyTextField);
        JLabel label = new JLabel("=");
        internalPanel.add(label);
        internalPanel.add(valueTextField);

        layout.putConstraint(SpringLayout.WEST, keyTextField, 0,
                SpringLayout.WEST, internalPanel);
        layout.putConstraint(SpringLayout.SOUTH, keyTextField, 0,
                SpringLayout.SOUTH, internalPanel);
        layout.putConstraint(SpringLayout.NORTH, keyTextField, 0,
                SpringLayout.NORTH, internalPanel);
        layout.putConstraint(SpringLayout.EAST, keyTextField, -10,
                SpringLayout.HORIZONTAL_CENTER, internalPanel);

        layout.putConstraint(SpringLayout.EAST, valueTextField, 0,
                SpringLayout.EAST, internalPanel);
        layout.putConstraint(SpringLayout.WEST, valueTextField, 10,
                SpringLayout.HORIZONTAL_CENTER, internalPanel);
        layout.putConstraint(SpringLayout.SOUTH, valueTextField, 0,
                SpringLayout.SOUTH, internalPanel);
        layout.putConstraint(SpringLayout.NORTH, valueTextField, 0,
                SpringLayout.NORTH, internalPanel);

        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.EAST,
                keyTextField);
        layout.putConstraint(SpringLayout.EAST, label, 5, SpringLayout.WEST,
                valueTextField);
        layout.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.SOUTH,
                internalPanel);
        layout.putConstraint(SpringLayout.NORTH, label, 0, SpringLayout.NORTH,
                internalPanel);

        arrayItemPanel.add(internalPanel, BorderLayout.CENTER);

        final JButton removeButton = GUIUtils.createImageButton(
                "/images/list-remove-small.png", "remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                keyTextFields.remove(keyTextField);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.getRootPane().repaint();

            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount() - 1);
    }

    public Map<String, String> getTextMap() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            if (keyTextFields.size() > 0) {
                Map<String, String> result = new HashMap<String, String>();
                for (int i = 0; i < keyTextFields.size(); i++) {
                    result.put(keyTextFields.get(i).getText(), valueTextFields
                            .get(i).getText());
                }
                return result;
            } else {
                return null;
            }
        }
    }

    private void setUseDefault(boolean useDefault) {
        useDefaultCheckBox.setSelected(!useDefault);

        // if default is true, remove everything from the arraypanel and add the
        // defaults
        if (useDefault) {
            arrayPanel.removeAll();
            arrayPanel.add(addPanel);

            if (defaultValues != null) {
                for (String key : defaultValues.keySet()) {
                    addField(key, defaultValues.get(key), arrayPanel);
                }
            }
            arrayPanel.repaint();
        }
        for (JTextField textField : keyTextFields) {
            textField.setEnabled(!useDefault);
        }
        for (JButton button : removeButtons) {
            button.setEnabled(!useDefault);
        }
        addButton.setEnabled(!useDefault);
        label.setEnabled(!useDefault);
        if (parent.getRootPane() != null) {
            parent.getRootPane().repaint();
        }
    }
}

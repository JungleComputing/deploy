package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextArrayComboBoxEditor {

    private final List<JComboBox> comboBoxes = new ArrayList<JComboBox>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = GUIUtils.createImageButton(
            "images/list-add-small.png", "add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private final String[] defaultValues;

    private JPanel parent;

    public TextArrayComboBoxEditor(final JPanel form, final String text,
            String[] values, String[] defaultValues,
            final String[] possibleValues) {
        this.parent = form;
        this.defaultValues = defaultValues;

        // determine whether this text editor holds a default value
        boolean useDefault = (values == null && defaultValues != null);

        // set the check box
        useDefaultCheckBox
                .setToolTipText("<html>enable for <code><i>default</i></code> value</html>");
        // if the value is 'nullified' or 'denullified' enable or disable the
        // editing components and invoke the edited method
        useDefaultCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUseDefault(!useDefaultCheckBox.isSelected(), possibleValues);
            }
        });

        // initialize the components in enabled or disabled state
        setUseDefault(useDefault, possibleValues);

        // set the text of the text fields to the appropriate values
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        if (!useDefault) {
            if (values != null) {
                for (String value : values) {
                    addField(value, arrayPanel, possibleValues);
                }
            }
        }

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, arrayPanel, possibleValues);
                setUseDefault(false, possibleValues);
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

    private void addField(String value, final JPanel panel,
            String[] possibleValues) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JComboBox comboBox = new JComboBox(possibleValues);
        for (String possibleValue : possibleValues) {
            if (possibleValue.equalsIgnoreCase(value)) {
                comboBox.setSelectedItem(possibleValue);
            }
        }

        comboBoxes.add(comboBox);
        arrayItemPanel.add(comboBox, BorderLayout.CENTER);

        final JButton removeButton = GUIUtils.createImageButton(
                "images/list-remove-small.png", "remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                comboBoxes.remove(comboBox);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.getRootPane().repaint();

            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount() - 1);
    }

    public String[] getTextArray() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            if (comboBoxes.size() > 0) {
                String[] result = new String[comboBoxes.size()];
                int i = 0;
                for (JComboBox comboBox : comboBoxes) {
                    result[i] = comboBox.getSelectedItem().toString();
                    i++;
                }
                return result;
            } else {
                return null;
            }
        }
    }

    private void setUseDefault(boolean useDefault, String[] possibleValues) {
        useDefaultCheckBox.setSelected(!useDefault);
        // if default is true, remove everything from the arraypanel and add the
        // defaults
        if (useDefault) {
            arrayPanel.removeAll();
            arrayPanel.add(addPanel);

            if (defaultValues != null) {
                for (String value : defaultValues) {
                    addField(value, arrayPanel, possibleValues);
                }
            }
            arrayPanel.repaint();
        }
        for (JComboBox comboBox : comboBoxes) {
            comboBox.setEnabled(!useDefault);
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

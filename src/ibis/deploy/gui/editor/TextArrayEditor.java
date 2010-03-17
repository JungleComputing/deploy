package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextArrayEditor {

    private final List<JTextField> textFields = new ArrayList<JTextField>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = Utils.createImageButton(
            "/images/list-add-small.png", "add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private final String[] defaultValues;

    private JPanel parent;

    public TextArrayEditor(final JPanel form, final String text,
            String[] values, String[] defaultValues) {
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
                for (String value : values) {
                    addField(value, arrayPanel);
                }
            }
        }

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, arrayPanel);
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
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth,
                label.getPreferredSize().height));
        container.add(arrayPanel, BorderLayout.CENTER);
        form.add(container);
    }

    private void addField(String value, final JPanel panel) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField textField = new JTextField(value);
        textFields.add(textField);
        arrayItemPanel.add(textField, BorderLayout.CENTER);

        final JButton removeButton = Utils.createImageButton(
                "/images/list-remove-small.png", "remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                textFields.remove(textField);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.getRootPane().repaint();

            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount());
    }

    public String[] getTextArray() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            if (textFields.size() > 0) {
                String[] result = new String[textFields.size()];
                int i = 0;
                for (JTextField textField : textFields) {
                    result[i] = textField.getText();
                    i++;
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
                for (String value : defaultValues) {
                    addField(value, arrayPanel);
                }
            }
            arrayPanel.repaint();
        }
        for (JTextField textField : textFields) {
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

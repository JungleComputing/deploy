package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextArrayComboBoxEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 7635144209006171177L;

    private final List<JComboBox> comboBoxes = new ArrayList<JComboBox>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private boolean usingDefault;

    public TextArrayComboBoxEditor(final JPanel form, final String text,
            String[] values, String[] defaultValues,
            final String[] possibleValues) {
        // determine whether this text editor holds a default value
        usingDefault = (values == null && defaultValues != null);

        // create the label
        final JLabel label = new JLabel(text, JLabel.TRAILING);
        if (usingDefault) {
            label.setText("<html><i>" + text + "</i></html>");
        }
        // add a background color, which can only been seen if the default is
        // used
        label.setBackground(Color.lightGray);
        label.setOpaque(usingDefault);

        final ActionListener textFieldActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (!usingDefault) {
                    return;
                }
                // no longer using the default
                usingDefault = false;
                // and change the background of this label
                label.setOpaque(usingDefault);
                label.setText(text);
                label.repaint();
                // and remove all action listeners too
                for (JComboBox comboBox : comboBoxes) {
                    comboBox.removeActionListener(this);
                }
            }

        };

        // set the text of the text fields to the appropriate values
        final JPanel arrayPanel = new JPanel();
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        if (usingDefault) {
            if (defaultValues != null) {
                for (String value : defaultValues) {
                    addField(value, arrayPanel, textFieldActionListener,
                            usingDefault, possibleValues);
                }
            }
        } else {
            if (values != null) {
                for (String value : values) {
                    addField(value, arrayPanel, textFieldActionListener,
                            usingDefault, possibleValues);
                }
            }
        }

        JPanel container = new JPanel(new BorderLayout());

        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));

        container.add(label, BorderLayout.WEST);
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        JButton addButton = GUIUtils.createImageButton(
                "images/list-add-small.png", "add a new item", null);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, arrayPanel, textFieldActionListener,
                        usingDefault, possibleValues);
                form.getRootPane().repaint();
            }

        });
        addButton.addActionListener(textFieldActionListener);

        JPanel newItemPanel = new JPanel(new BorderLayout());
        newItemPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(newItemPanel);
        label.setLabelFor(arrayPanel);
        container.add(arrayPanel);
        form.add(container, BorderLayout.CENTER);
    }

    private void addField(String value, final JPanel panel,
            ActionListener listener, boolean usingDefault,
            String[] possibleValues) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JComboBox comboBox = new JComboBox(possibleValues);
        for (String possibleValue : possibleValues) {
            if (value.equalsIgnoreCase(possibleValue)) {
                comboBox.setSelectedItem(possibleValue);
            }
        }

        if (usingDefault) {
            comboBox.addActionListener(listener);
        }
        comboBox.setPreferredSize(new Dimension(Integer.MAX_VALUE, 12));
        comboBoxes.add(comboBox);
        arrayItemPanel.add(comboBox, BorderLayout.CENTER);

        final JButton removeButton = GUIUtils.createImageButton(
                "images/list-remove-small.png", "remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                comboBoxes.remove(comboBox);
                arrayItemPanel.remove(comboBox);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.getRootPane().repaint();

            }
        });
        if (usingDefault) {
            removeButton.addActionListener(listener);
        }
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount() - 1);
    }

    public String[] getTextArray() {
        if (usingDefault) {
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
}

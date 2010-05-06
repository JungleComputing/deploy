package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class TextMapArrayEditor extends ChangeableField implements KeyListener {

    private final List<JTextField> keyTextFields = new ArrayList<JTextField>();

    private final List<JTextField> valueTextFields = new ArrayList<JTextField>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = Utils.createImageButton(
            "images/list-add-small.png", "Add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private JPanel parentPanel;

    private Map<String, String> initialValues;

    public TextMapArrayEditor(final JPanel tabPanel, final JPanel form,
            final String text, Map<String, String> values) {
        this.parentPanel = form;
        this.tabPanel = tabPanel;

        if (values != null) {
            initialValues = values;
        } else {
            initialValues = new HashMap<String, String>();
        }

        // set the text of the text fields to the appropriate values
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, null, arrayPanel);
                arrayPanel.getRootPane().repaint();
                informParent();
            }

        });

        addPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(addPanel);

        if (values != null) {
            for (String key : values.keySet()) {
                addField(key, values.get(key), arrayPanel);
            }
        }

        JPanel container = new JPanel(new BorderLayout());

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(label);
        labelPanel.add(Box.createVerticalGlue());
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label.setLabelFor(arrayPanel);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth, label
                .getPreferredSize().height));

        container.add(arrayPanel, BorderLayout.CENTER);
        form.add(container);
    }

    private void addField(String key, String value, final JPanel panel) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField keyTextField = new JTextField(key);
        keyTextFields.add(keyTextField);
        keyTextField.addKeyListener(this);
        final JTextField valueTextField = new JTextField(value);
        valueTextFields.add(valueTextField);
        valueTextField.addKeyListener(this);

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

        final Component rigidArea = Box.createRigidArea(new Dimension(0,
                Utils.gapHeight));

        final JButton removeButton = Utils.createImageButton(
                "images/list-remove-small.png", "Remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                keyTextFields.remove(keyTextField);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.remove(rigidArea);
                panel.getRootPane().repaint();
                informParent();
            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount() - 1);
        panel.add(rigidArea, panel.getComponentCount() - 1);
    }

    public Map<String, String> getTextMap() {
        if (keyTextFields.size() > 0) {
            Map<String, String> result = new HashMap<String, String>();
            for (int i = 0; i < keyTextFields.size(); i++) {
                result.put(keyTextFields.get(i).getText(), valueTextFields.get(
                        i).getText());
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Adds a field for each of the new values
     * @param values
     *            - new list of values
     */
    public void setTextMap(Map<String, String> values) {
        clearAll();

        if (values != null) {
            for (String key : values.keySet()) {
                addField(key, values.get(key), arrayPanel);
            }
        }
        parentPanel.getRootPane().repaint();
    }

    private void clearAll() {
        keyTextFields.clear();
        valueTextFields.clear();
        removeButtons.clear();
        arrayPanel.removeAll();

        arrayPanel.add(addPanel);
    }

    @Override
    public void refreshInitialValue() {
        initialValues = getTextMap();
        if (initialValues == null) {
            initialValues = new HashMap<String, String>();
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        informParent();
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    /**
     * @return - true if the values in the text fields are different from the
     *         initial values. Also returns true if the number of text fields
     *         has changed.
     */
    public boolean hasChanged() {
        if (keyTextFields.size() != initialValues.size()) {
            return true;
        }

        Iterator<JTextField> keyIterator = keyTextFields.iterator();
        Iterator<JTextField> valueIterator = valueTextFields.iterator();

        JTextField keyTF, valueTF;

        for (String key : initialValues.keySet()) {
            keyTF = keyIterator.next();
            valueTF = valueIterator.next();

            if (!keyTF.getText().equalsIgnoreCase(key)
                    || !valueTF.getText().equalsIgnoreCase(
                            initialValues.get(key))) {
                return true;
            }
        }

        return false;
    }
}

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
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextArrayEditor extends ChangeableField implements KeyListener {

    private final List<JTextField> textFields = new ArrayList<JTextField>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = Utils.createImageButton(
            "images/list-add-small.png", "Add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private JPanel parentPanel;

    private String[] initialValues;

    public TextArrayEditor(final JPanel tabPanel, final JPanel form,
            final String text, String[] values) {
        this.parentPanel = form;
        this.tabPanel = tabPanel;

        if (values != null) {
            this.initialValues = values;
        } else {
            this.initialValues = new String[0];
        }

        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, arrayPanel);
                arrayPanel.getRootPane().repaint();
                informParent();
            }

        });

        addPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(addPanel);

        if (values != null) {
            for (String value : values) {
                addField(value, arrayPanel);
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

    private void addField(String value, final JPanel panel) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField textField = new JTextField(value);
        textFields.add(textField);
        arrayItemPanel.add(textField, BorderLayout.CENTER);
        textField.addKeyListener(this);

        final Component rigidArea = Box.createRigidArea(new Dimension(0,
                Utils.gapHeight));
        panel.add(rigidArea);

        final JButton removeButton = Utils.createImageButton(
                "images/list-remove-small.png", "Remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                textFields.remove(textField);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.remove(rigidArea);
                panel.getRootPane().repaint();
                informParent();
            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel);
    }

    public String[] getTextArray() {
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

    /**
     * adds a field for each of the new values
     * 
     * @param values
     *            - new list of values
     */
    public void setTextArray(String[] values) {
        clearAll();

        if (values != null) {
            for (String value : values) {
                addField(value, arrayPanel);
            }
        }
        parentPanel.getRootPane().repaint();
    }

    private void clearAll() {
        textFields.clear();
        removeButtons.clear();
        arrayPanel.removeAll();

        arrayPanel.add(addPanel);
    }

    @Override
    public void refreshInitialValue() {
        initialValues = getTextArray();
        if (initialValues == null) {
            initialValues = new String[0];
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
        if (textFields.size() != initialValues.length) {
            return true;
        }

        int i = 0;
        for (JTextField textField : textFields) {
            if (!textField.getText().equalsIgnoreCase(initialValues[i])) {
                return true;
            }
            i++;
        }
        return false;
    }
}

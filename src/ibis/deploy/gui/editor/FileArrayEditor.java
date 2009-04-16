package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileArrayEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 7635144209006171177L;

    private final List<JFileChooser> fileChoosers = new ArrayList<JFileChooser>();

    private final List<JTextField> textFields = new ArrayList<JTextField>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final List<JButton> openButtons = new ArrayList<JButton>();

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = Utils.createImageButton(
            "/images/list-add-small.png", "add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private final File[] defaultValues;

    private JPanel parent;

    public FileArrayEditor(final JPanel form, final String text, File[] values,
            File[] defaultValues) {
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
                for (File value : values) {
                    addField(value, arrayPanel);
                }
            }
        }

        JPanel container = new JPanel(new BorderLayout());

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(new File("."), arrayPanel);
                setUseDefault(false);
                arrayPanel.getRootPane().repaint();
            }

        });

        addPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(addPanel);
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

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

    private void addField(File value, final JPanel panel) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField textField = new JTextField(value.getPath());
        textFields.add(textField);
        arrayItemPanel.add(textField, BorderLayout.CENTER);

        final JFileChooser fileChooser = new JFileChooser(value);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChoosers.add(fileChooser);
        final JButton openButton = Utils.createImageButton(
                "/images/document-open.png", "select a file", null);
        openButtons.add(openButton);

        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getPath());
                }
            }

        });

        arrayItemPanel.add(openButton, BorderLayout.EAST);

        final JButton removeButton = Utils.createImageButton(
                "/images/list-remove-small.png", "remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                fileChoosers.remove(fileChooser);
                textFields.remove(textField);
                arrayItemPanel.remove(openButton);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.getRootPane().repaint();

            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount() - 1);
    }

    public File[] getFileArray() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            if (textFields.size() > 0) {
                File[] result = new File[textFields.size()];
                int i = 0;
                for (JTextField textField : textFields) {
                    result[i] = new File(textField.getText());
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
                for (File value : defaultValues) {
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
        for (JButton button : openButtons) {
            button.setEnabled(!useDefault);
        }
        addButton.setEnabled(!useDefault);
        label.setEnabled(!useDefault);
        if (parent.getRootPane() != null) {
            parent.getRootPane().repaint();
        }
    }
}

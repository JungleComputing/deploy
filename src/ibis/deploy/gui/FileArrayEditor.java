package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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

    private boolean usingDefault;

    public FileArrayEditor(final JPanel form, final String text, File[] values,
            File[] defaultValues) {
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
                for (JTextField textField : textFields) {
                    textField.removeActionListener(this);
                }
            }

        };

        // set the text of the text fields to the appropriate values
        final JPanel arrayPanel = new JPanel();
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        if (usingDefault) {
            if (defaultValues != null) {
                for (File value : defaultValues) {
                    addField(value, arrayPanel, textFieldActionListener,
                            usingDefault);
                }
            }
        } else {
            if (values != null) {
                for (File value : values) {
                    addField(value, arrayPanel, textFieldActionListener,
                            usingDefault);
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
                addField(new File("."), arrayPanel, textFieldActionListener,
                        usingDefault);
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

    private void addField(File value, final JPanel panel,
            final ActionListener listener, boolean usingDefaults) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField textField = new JTextField(value.getPath());
        if (usingDefaults) {
            textField.addActionListener(listener);
        }
        textFields.add(textField);
        arrayItemPanel.add(textField, BorderLayout.CENTER);

        final JFileChooser fileChooser = new JFileChooser(value);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChoosers.add(fileChooser);
        final JButton openButton = GUIUtils.createImageButton(
                "images/document-open.png", "select a file", null);
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getPath());
                    listener.actionPerformed(null);
                }
            }

        });
        arrayItemPanel.add(openButton, BorderLayout.EAST);

        final JButton removeButton = GUIUtils.createImageButton(
                "images/list-remove-small.png", "remove item", null);
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
        if (usingDefaults) {
            removeButton.addActionListener(listener);
        }
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel, panel.getComponentCount() - 1);
    }

    public File[] getFileArray() {
        if (usingDefault) {
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
}

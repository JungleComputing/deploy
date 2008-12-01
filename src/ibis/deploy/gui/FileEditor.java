package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 2216106418941074147L;

    private final JTextField textField = new JTextField();

    private boolean usingDefault;

    public FileEditor(JPanel form, final String text, File value,
            File defaultValue) {
        // determine whether this text editor holds a default value
        usingDefault = (value == null && defaultValue != null);

        // set the text of the text field to the appropriate value
        if (value != null || usingDefault) {
            textField.setText((usingDefault) ? defaultValue.getPath() : value
                    .getPath());
        }

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
                // no longer using the default
                usingDefault = false;
                // and change the background of this label
                label.setOpaque(usingDefault);
                label.setText(text);
                label.repaint();
                // and remove this action listener too
                textField.removeActionListener(this);
            }

        };

        if (usingDefault) {
            textField.addActionListener(textFieldActionListener);
        }
        JPanel container = new JPanel(new BorderLayout());

        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(label, BorderLayout.WEST);
        label.setLabelFor(textField);
        final JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(textField, BorderLayout.CENTER);

        final JFileChooser fileChooser = new JFileChooser(value);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final JButton openButton = GUIUtils.createImageButton(
                "images/document-open.png", "select a file", null);
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(filePanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getPath());
                    textFieldActionListener.actionPerformed(null);
                }
            }

        });
        filePanel.add(openButton, BorderLayout.EAST);

        container.add(filePanel);
        container.setMinimumSize(container.getPreferredSize());
        form.add(container);
    }

    public File getFile() {
        if (usingDefault) {
            return null;
        } else {
            return new File(textField.getText());
        }
    }

}

package ibis.deploy.gui.editor;

import ibis.deploy.gui.GUIUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final File defaultValue;

    private final JButton openButton = GUIUtils.createImageButton(
            "/images/document-open.png", "select a file", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    public FileEditor(JPanel form, final String text, File value,
            File defaultValue) {
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
            textField.setText(value.getPath());
        }

        JPanel container = new JPanel(new BorderLayout());

        container.add(label, BorderLayout.WEST);
        final JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(textField, BorderLayout.CENTER);

        final JFileChooser fileChooser = new JFileChooser(value);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(filePanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getPath());
                }
            }

        });
        filePanel.add(openButton, BorderLayout.EAST);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(useDefaultCheckBox, BorderLayout.WEST);
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(filePanel);
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(filePanel, BorderLayout.CENTER);
        form.add(container);
    }

    public File getFile() {
        if (!useDefaultCheckBox.isSelected()) {
            return null;
        } else {
            return new File(textField.getText());
        }
    }

    private void setUseDefault(boolean useDefault) {
        useDefaultCheckBox.setSelected(!useDefault);

        if (useDefault) {
            if (defaultValue != null) {
                textField.setText(defaultValue.getPath());
            } else {
                textField.setText(null);
            }
        }
        label.setEnabled(!useDefault);
        textField.setEnabled(!useDefault);
        openButton.setEnabled(!useDefault);
    }

}

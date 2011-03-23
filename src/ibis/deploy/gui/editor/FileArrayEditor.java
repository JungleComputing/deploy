package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileArrayEditor extends ChangeableField implements KeyListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7635144209006171177L;

    private final List<JFileChooser> fileChoosers = new ArrayList<JFileChooser>();

    private final List<JTextField> textFields = new ArrayList<JTextField>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final List<JButton> openButtons = new ArrayList<JButton>();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = Utils.createImageButton(
            "images/list-add-small.png", "Add a new item", null);

    private final JLabel label;

    private JPanel parentPanel;

    private File[] initialValues;

    /**
     * @param form
     *            - parent panel
     * @param text
     *            - label text
     * @param values
     *            - initial values for file paths
     */
    public FileArrayEditor(final JPanel tabPanel, final JPanel form,
            final String text, File[] values) {

        this.parentPanel = form;
        this.tabPanel = tabPanel;

        if (values != null) {
            this.initialValues = values;
        } else {
            initialValues = new File[0];
        }

        JPanel container = new JPanel(new BorderLayout());

        addPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(addPanel);
        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(new File("."), arrayPanel);
                arrayPanel.getRootPane().repaint();

                informParent();
            }

        });

        // add the files in the list to the panel
        if (values != null) {
            for (File value : values) {
                addField(value, arrayPanel);
            }
        }

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        label = new JLabel(text, JLabel.TRAILING);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label.setLabelFor(arrayPanel);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth, label
                .getPreferredSize().height));
        labelPanel.add(label);
        labelPanel.add(Box.createVerticalGlue());

        container.add(labelPanel, BorderLayout.WEST);
        container.add(arrayPanel, BorderLayout.CENTER);
        form.add(container);
    }

    /**
     * @param value
     *            - path of the file
     * @param panel
     *            - parent panel
     */
    private void addField(File value, final JPanel panel) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JTextField textField = new JTextField(value.getPath());
        textFields.add(textField);
        arrayItemPanel.add(textField, BorderLayout.CENTER);
        textField.addKeyListener(this);

        final JFileChooser fileChooser = new JFileChooser(value);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChoosers.add(fileChooser);
        final JButton openButton = Utils.createImageButton(
                "images/document-open.png", "Select a file", null);
        openButtons.add(openButton);

        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getPath());
                    informParent();
                }
            }

        });

        arrayItemPanel.add(openButton, BorderLayout.EAST);

        final Component rigidArea = Box.createRigidArea(new Dimension(0,
                Utils.gapHeight));

        final JButton removeButton = Utils.createImageButton(
                "images/list-remove-small.png", "Remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                fileChoosers.remove(fileChooser);
                textFields.remove(textField);
                arrayItemPanel.remove(openButton);
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

    /**
     * @return - array containing the files in the editor
     */
    public File[] getFileArray() {
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

    /**
     * resets the files in the editor to the given values
     * 
     * @param values
     *            - list of new file paths
     */
    public void setFileArray(File[] values) {
        clearAll();

        // add the files in the list to the panel
        if (values != null) {
            for (File value : values) {
                addField(value, arrayPanel);
            }
        }
        parentPanel.getRootPane().repaint();
    }

    private void clearAll() {
        fileChoosers.clear();
        textFields.clear();
        removeButtons.clear();
        openButtons.clear();
        arrayPanel.removeAll();

        arrayPanel.add(addPanel);
    }

    public void refreshInitialValue() {
        initialValues = getFileArray();
        if (initialValues == null) {
            initialValues = new File[0];
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

    @Override
    /**
     * @return - true if any of the text fields contain a different value than
     * their initial value. Also return true if the number of fields has changed
     */
    public boolean hasChanged() {
        if (textFields.size() != initialValues.length) {
            return true;
        }

        int i = 0;
        for (JTextField tf : textFields) {
            if (!tf.getText().equalsIgnoreCase(initialValues[i].getPath())) {
                return true;
            }
            i++;
        }

        return false;
    }
}

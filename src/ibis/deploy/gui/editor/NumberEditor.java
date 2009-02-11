package ibis.deploy.gui.editor;

import ibis.deploy.gui.GUIUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class NumberEditor {

    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1,
            Integer.MAX_VALUE, 1));

    private final JCheckBox useDefaultCheckBox = new JCheckBox();

    private final int defaultValue;

    private final JButton openButton = GUIUtils.createImageButton(
            "/images/document-open.png", "select a file", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    public NumberEditor(JPanel form, final String text, int value,
            int defaultValue) {
        this.defaultValue = defaultValue;

        // determine whether this text editor holds a default value
        final boolean useDefault = value <= 0;

        // set the check box
        useDefaultCheckBox
                .setToolTipText("<html>check this box to overwrite the <code><i>default</i></code> value</html>");
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
        if (value > 0 && !useDefault) {
            spinner.setValue(value);
        }

        JPanel container = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(useDefaultCheckBox, BorderLayout.WEST);
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(spinner);
        label.setPreferredSize(new Dimension(150,
                label.getPreferredSize().height));
        container.add(spinner, BorderLayout.CENTER);
        form.add(container);
    }

    public int getValue() {
        if (!useDefaultCheckBox.isSelected()) {
            return -1;
        } else {
            return ((SpinnerNumberModel) spinner.getModel()).getNumber()
                    .intValue();
        }
    }

    private void setUseDefault(boolean useDefault) {
        useDefaultCheckBox.setSelected(!useDefault);

        if (useDefault) {
            spinner.setValue(defaultValue);
        }
        label.setEnabled(!useDefault);
        spinner.setEnabled(!useDefault);
        openButton.setEnabled(!useDefault);
    }

}

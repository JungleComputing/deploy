package deployer.ibis.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import deployer.gui.SelectionComponent;
import deployer.ibis.IbisPool;

public class IbisPoolSelectionComponent implements SelectionComponent {

    private int poolID;

    private SortedSet<IbisPool> ibisPools = new TreeSet<IbisPool>();

    private JPanel panel;

    private JRadioButton existingPoolRadioButton;

    private JComboBox existingPoolComboBox;

    private JTextField newPoolTextField;

    private JCheckBox newPoolCheckBox;

    private JSpinner newPoolSpinner;

    public IbisPoolSelectionComponent() {
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
        panel.add(boxPanel);
        panel.setBorder(BorderFactory.createTitledBorder("select ibis pool"));

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel,
                BoxLayout.PAGE_AXIS));

        existingPoolRadioButton = new JRadioButton("existing: ", false);
        JRadioButton newPoolRadioButton = new JRadioButton("new: ", true);
        // existingPoolPanel.add(existingPoolRadioButton);
        checkBoxPanel.add(existingPoolRadioButton);
        checkBoxPanel.add(newPoolRadioButton);

        boxPanel.add(checkBoxPanel);

        JPanel detailsPanel = new JPanel();
        detailsPanel
                .setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
        JPanel existingPoolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                0, 0));
        existingPoolComboBox = new JComboBox();
        existingPoolComboBox.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) existingPoolComboBox
                        .getPreferredSize().getHeight()));

        existingPoolPanel.add(existingPoolComboBox);
        detailsPanel.add(existingPoolPanel);

        JPanel newPoolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        newPoolTextField = new JTextField("pool-" + poolID);
        newPoolTextField.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) newPoolTextField
                        .getPreferredSize().getHeight()));
        newPoolPanel.add(newPoolTextField);
        newPoolCheckBox = new JCheckBox("Closed World.");
        newPoolPanel.add(newPoolCheckBox);
        JLabel poolSizeLabel = new JLabel(" Pool size: ");
        poolSizeLabel.setEnabled(false);
        newPoolPanel.add(poolSizeLabel);
        newPoolSpinner = new JSpinner(new SpinnerNumberModel(1, 1,
                Integer.MAX_VALUE, 1));
        newPoolPanel.add(newPoolSpinner);
        newPoolSpinner.setEnabled(false);
        newPoolSpinner.setPreferredSize(new Dimension(
                (int) DEFAULT_COMPONENT_WIDTH / 2, (int) newPoolSpinner
                        .getPreferredSize().getHeight()));
        newPoolCheckBox.addActionListener(new CheckBoxActionListener(
                newPoolSpinner, poolSizeLabel));
        ButtonGroup poolRadioButtons = new ButtonGroup();
        poolRadioButtons.add(existingPoolRadioButton);
        poolRadioButtons.add(newPoolRadioButton);
        detailsPanel.add(newPoolPanel);
        boxPanel.add(detailsPanel);
        boxPanel.add(Box.createHorizontalGlue());
    }

    public void update() {
    }

    public JPanel getPanel() {
        return panel;
    }

    public Object[] getValues() {
        if (existingPoolRadioButton.isSelected()) {
            return new Object[] { existingPoolComboBox.getSelectedItem() };
        } else {
            String poolName = newPoolTextField.getText();
            System.out.println("current: " + poolName);
            System.out.println("compare: " + "pool-" + poolID);
            if (poolName.equals("pool-" + poolID)) {
                poolID++;
                newPoolTextField.setText("pool-" + poolID);
                newPoolTextField.repaint();
            }
            boolean closedWorld = newPoolCheckBox.isSelected();
            int poolSize = ((SpinnerNumberModel) newPoolSpinner.getModel())
                    .getNumber().intValue();
            IbisPool newIbisPool = new IbisPool(poolName, closedWorld, poolSize);
            ibisPools.add(newIbisPool);
            existingPoolComboBox.removeAllItems();
            for (IbisPool ibisPool : ibisPools) {
                existingPoolComboBox.addItem(ibisPool);
            }
            return new Object[] { newIbisPool };
        }
    }

    private class CheckBoxActionListener implements ActionListener {

        private JSpinner spinner;

        private JLabel label;

        public CheckBoxActionListener(JSpinner spinner, JLabel label) {
            this.spinner = spinner;
            this.label = label;
        }

        public void actionPerformed(ActionEvent e) {
            spinner.setEnabled((((JCheckBox) e.getSource()).isSelected()));
            label.setEnabled((((JCheckBox) e.getSource()).isSelected()));
        }

    }
}

package deployer.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import deployer.Application;
import deployer.ApplicationGroup;
import deployer.Deployer;

public class ApplicationSelectionComponent implements SelectionComponent {

    private JPanel panel;

    private JComboBox applicationComboBox;

    private JComboBox applicationGroupComboBox;

    private JSpinner processCount;

    private Deployer deployer;

    public ApplicationSelectionComponent(Deployer deployer) {
        this.deployer = deployer;
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        panel.setBorder(BorderFactory.createTitledBorder("select application"));

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 3, 3));
        gridPanel.add(new JLabel("application group: ", JLabel.RIGHT));
        applicationGroupComboBox = new JComboBox();
        applicationGroupComboBox.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) applicationGroupComboBox
                        .getPreferredSize().getHeight()));

        for (ApplicationGroup applicationGroup : deployer
                .getApplicationGroups()) {
            applicationGroupComboBox.addItem(applicationGroup);
        }
        gridPanel.add(applicationGroupComboBox);
        gridPanel.add(new JLabel("application: ", JLabel.RIGHT));
        applicationComboBox = new JComboBox();
        applicationComboBox.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) applicationComboBox
                        .getPreferredSize().getHeight()));

        applicationGroupComboBox
                .addActionListener(new ApplicationGroupComboBoxActionListener(
                        applicationGroupComboBox, applicationComboBox));
        gridPanel.add(applicationComboBox);
        gridPanel.add(new JLabel("process count: ", JLabel.RIGHT));
        processCount = new JSpinner(new SpinnerNumberModel(1, 1,
                Integer.MAX_VALUE, 1));
        processCount.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH / 2, (int) processCount
                        .getPreferredSize().getHeight()));

        gridPanel.add(processCount);

        panel.add(gridPanel);
        panel.add(Box.createHorizontalGlue());
    }

    public void update() {
        applicationGroupComboBox.removeAllItems();
        for (ApplicationGroup applicationGroup : deployer
                .getApplicationGroups()) {
            applicationGroupComboBox.addItem(applicationGroup);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public Object[] getValues() {
        // Application, process count
        return new Object[] {
                applicationComboBox.getSelectedItem(),
                ((SpinnerNumberModel) processCount.getModel()).getNumber()
                        .intValue() };
    }

    private class ApplicationGroupComboBoxActionListener implements
            ActionListener {

        private JComboBox applicationComboBox;

        private JComboBox applicationGroupComboBox;

        ApplicationGroupComboBoxActionListener(
                JComboBox applicationGroupComboBox,
                JComboBox applicationComboBox) {
            this.applicationGroupComboBox = applicationGroupComboBox;
            this.applicationComboBox = applicationComboBox;
            if (applicationGroupComboBox.getSelectedItem() != null) {
                for (Application application : ((ApplicationGroup) applicationGroupComboBox
                        .getSelectedItem()).getApplications()) {
                    applicationComboBox.addItem(application);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (applicationGroupComboBox.getSelectedItem() == null) {
                return;
            }
            applicationComboBox.removeAllItems();
            for (Application application : ((ApplicationGroup) applicationGroupComboBox
                    .getSelectedItem()).getApplications()) {
                applicationComboBox.addItem(application);
            }
        }
    }

}

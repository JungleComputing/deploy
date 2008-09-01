package deployer.ibis.gui;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.util.MalformedAddressException;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import deployer.gui.SelectionComponent;
import deployer.ibis.Server;

public class IbisHubSelectionComponent implements SelectionComponent {

    private JPanel panel;

    private List<Server> ibisServers = new ArrayList<Server>();

    private List<Server> ibisHubs = new ArrayList<Server>();

    private JRadioButton newServerNewHubRadioButton;

    private JRadioButton existingServerNewHubRadioButton;

    private JRadioButton existingServerExistingHubRadioButton;

    private JComboBox existingServerComboBox;

    private JComboBox existingServerComboBox2;

    private JComboBox existingHubComboBox;

    private JComboBox startVizComboBox;

    private JTextField newServerTextField;

    private JTextField newHubTextField;

    private JButton startVizButton;

    public IbisHubSelectionComponent() {
        panel = new JPanel(new BorderLayout());
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(flowPanel, BorderLayout.NORTH);

        JPanel boxPanel = new JPanel();
        flowPanel.add(boxPanel);
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory
                .createTitledBorder("select ibis server & hub"));

        newServerNewHubRadioButton = new JRadioButton(
                "new server and new hub: ", true);
        existingServerNewHubRadioButton = new JRadioButton("existing server: ");
        existingServerExistingHubRadioButton = new JRadioButton(
                "existing server: ");
        JPanel newServerNewHubPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, 0));
        newServerNewHubPanel.add(newServerNewHubRadioButton);
        newServerTextField = new JTextField();
        newServerTextField.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) newServerTextField
                        .getPreferredSize().getHeight()));
        newServerNewHubPanel.add(newServerTextField);
        boxPanel.add(newServerNewHubPanel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 3)));

        JPanel existingServerNewHubPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, 0));
        existingServerNewHubPanel.add(existingServerNewHubRadioButton);
        existingServerComboBox = new JComboBox();
        existingServerComboBox.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) existingServerComboBox
                        .getPreferredSize().getHeight()));
        existingServerNewHubPanel.add(existingServerComboBox);
        existingServerNewHubPanel.add(new JLabel(" and new hub: "));
        newHubTextField = new JTextField();
        newHubTextField.setPreferredSize(new Dimension(DEFAULT_COMPONENT_WIDTH,
                (int) newHubTextField.getPreferredSize().getHeight()));
        existingServerNewHubPanel.add(newHubTextField);
        boxPanel.add(existingServerNewHubPanel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 3)));

        JPanel existingServerExistingHubPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, 0));
        existingServerExistingHubPanel
                .add(existingServerExistingHubRadioButton);
        existingServerComboBox2 = new JComboBox();
        existingServerComboBox2.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) existingServerComboBox2
                        .getPreferredSize().getHeight()));
        existingServerExistingHubPanel.add(existingServerComboBox2);
        existingServerExistingHubPanel.add(new JLabel(" and existing hub: "));
        existingHubComboBox = new JComboBox();
        existingHubComboBox.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) existingHubComboBox
                        .getPreferredSize().getHeight()));
        existingServerExistingHubPanel.add(existingHubComboBox);
        boxPanel.add(existingServerExistingHubPanel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 3)));

        JPanel startVizPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        startVizPanel.add(new JLabel("start visualization for: "));
        startVizComboBox = new JComboBox();
        startVizComboBox.setPreferredSize(new Dimension(
                DEFAULT_COMPONENT_WIDTH, (int) startVizComboBox
                        .getPreferredSize().getHeight()));
        startVizPanel.add(startVizComboBox);
        startVizButton = new JButton("visualize");
        startVizButton.addActionListener(new StartVizActionListener(panel));
        startVizPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        startVizPanel.add(startVizButton);
        boxPanel.add(startVizPanel);
        boxPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(newServerNewHubRadioButton);
        buttonGroup.add(existingServerExistingHubRadioButton);
        buttonGroup.add(existingServerNewHubRadioButton);
    }

    public void update() {

    }

    public JPanel getPanel() {
        return panel;
    }

    public Object[] getValues() throws Exception {
        if (newServerNewHubRadioButton.isSelected()) {
            return new Object[] { null, null, newServerTextField.getText() };
        } else if (existingServerNewHubRadioButton.isSelected()) {
            return new Object[] {
                    ((Server) existingServerComboBox.getSelectedItem())
                            .getServerClient().getLocalAddress(), null,
                    newHubTextField.getText() };
        } else {
            return new Object[] {
                    ((Server) existingServerComboBox2.getSelectedItem())
                            .getServerClient().getLocalAddress(),
                    ((Server) existingHubComboBox.getSelectedItem())
                            .getServerClient().getLocalAddress(), null };
        }
    }

    public void addServer(Server ibisServer) {
        ibisServers.add(ibisServer);
        existingServerComboBox.addItem(ibisServer);
        existingServerComboBox2.addItem(ibisServer);
        startVizComboBox.addItem(ibisServer);
    }

    public void addHub(Server ibisHub) {
        ibisHubs.add(ibisHub);
        existingHubComboBox.addItem(ibisHub);
    }

    private class StartVizActionListener implements ActionListener {

        private JPanel panel;

        private SmartsocketsViz viz;

        public StartVizActionListener(JPanel panel) {
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent e) {
            List<DirectSocketAddress> serverList = new ArrayList<DirectSocketAddress>();
            try {
                serverList.add(DirectSocketAddress
                        .getByAddress(((Server) startVizComboBox
                                .getSelectedItem()).getServerClient()
                                .getLocalAddress()));
            } catch (UnknownHostException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (MalformedAddressException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (viz != null) {
                panel.remove(viz);
            }
            viz = new SmartsocketsViz(serverList);
            panel.add(viz, BorderLayout.CENTER);
            panel.getRootPane().repaint();
        }
    }

}

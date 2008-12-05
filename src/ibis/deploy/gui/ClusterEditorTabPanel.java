package ibis.deploy.gui;

import ibis.deploy.Cluster;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.gridlab.gat.AdaptorInfo;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;

public class ClusterEditorTabPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1085273687721913236L;

    public ClusterEditorTabPanel(final Cluster source,
            final ClusterEditorPanel clusterEditorPanel, final GUI gui) {
        this(source, clusterEditorPanel, gui, false);
    }

    public ClusterEditorTabPanel(final Cluster source,
            final ClusterEditorPanel clusterEditorPanel, final GUI gui,
            final boolean defaultsEditor) {
        setLayout(new BorderLayout());

        Cluster defaults = (source.getGrid() == null) ? null : source.getGrid()
                .getDefaults();

        JPanel container = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.PAGE_AXIS));
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextEditor nameEditor = (defaultsEditor) ? null : new TextEditor(
                formPanel, "Name: ", source.getName(), "");
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final NumberEditor nodesEditor = new NumberEditor(formPanel, "Nodes: ",
                source.getNodes(), defaults == null ? 1 : defaults.getNodes());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final NumberEditor coresEditor = new NumberEditor(formPanel, "Cores: ",
                source.getCores(), defaults == null ? 1 : defaults
                        .getCores());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextEditor jobURIEditor = new TextEditor(formPanel, "Job URI: ",
                source.getJobURI(), defaults == null ? null : defaults
                        .getJobURI());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        AdaptorInfo[] jobAdaptorInfos = new AdaptorInfo[0];
        try {
            jobAdaptorInfos = GAT.getAdaptorInfos("ResourceBroker");
        } catch (GATInvocationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String[] jobAdaptors = new String[jobAdaptorInfos.length];
        for (int i = 0; i < jobAdaptorInfos.length; i++) {
            jobAdaptors[i] = jobAdaptorInfos[i].getShortName().replace(
                    "ResourceBrokerAdaptor", "");
        }
        final TextComboBoxEditor jobAdaptorEditor = new TextComboBoxEditor(
                formPanel, "Job Adaptor: ", source.getJobAdaptor(),
                defaults == null ? null : defaults.getJobAdaptor(), jobAdaptors);
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        AdaptorInfo[] fileAdaptorInfos = new AdaptorInfo[0];
        try {
            fileAdaptorInfos = GAT.getAdaptorInfos("File");
        } catch (GATInvocationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String[] fileAdaptors = new String[fileAdaptorInfos.length];
        for (int i = 0; i < fileAdaptorInfos.length; i++) {
            fileAdaptors[i] = fileAdaptorInfos[i].getShortName().replace(
                    "FileAdaptor", "");
        }
        final TextArrayComboBoxEditor fileAdaptorsEditor = new TextArrayComboBoxEditor(
                formPanel, "File Adaptors: ", source.getFileAdaptors(),
                defaults == null ? null : defaults.getFileAdaptors(),
                fileAdaptors);

        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextEditor userNameEditor = new TextEditor(formPanel,
                "User Name: ", source.getUserName(), defaults == null ? null
                        : defaults.getUserName());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextEditor javaPathEditor = new TextEditor(formPanel,
                "Java Path: ", source.getJavaPath(), defaults == null ? null
                        : defaults.getJavaPath());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final FileEditor cacheDirEditor = new FileEditor(formPanel,
                "Cache Directory: ", source.getCacheDir(),
                defaults == null ? null : defaults.getCacheDir());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final FileEditor jobWrapperScriptEditor = new FileEditor(formPanel,
                "Job Wrapper Script: ", source.getJobWrapperScript(),
                defaults == null ? null : defaults.getJobWrapperScript());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextComboBoxEditor serverAdaptorEditor = new TextComboBoxEditor(
                formPanel, "Server Adaptor: ", source.getServerAdaptor(),
                defaults == null ? null : defaults.getServerAdaptor(),
                jobAdaptors);
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        final TextEditor serverURIEditor = new TextEditor(formPanel,
                "Server URI: ", source.getServerURI(), defaults == null ? null
                        : defaults.getServerURI());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final FileArrayEditor serverOutputFilesEditor = new FileArrayEditor(
                formPanel, "Server Output Files: ", source
                        .getServerOutputFiles(), defaults == null ? null
                        : defaults.getServerOutputFiles());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final MapEditor geoPositionEditor = new MapEditor(formPanel,
                "Geo Position: ", source.getLatitude(), source.getLongitude());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        container.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(container,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(new JLabel("check to overwrite the default values"),
                BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        JButton applyButton = new JButton("Apply");

        add(applyButton, BorderLayout.SOUTH);

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (!defaultsEditor) {
                    try {
                        source.setName(nameEditor.getText());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(getRootPane(), e
                                .getMessage(), "Unable to apply changes",
                                JOptionPane.PLAIN_MESSAGE);
                        e.printStackTrace(System.err);
                        return;
                    }
                }
                source.setUserName(userNameEditor.getText());
                if (jobURIEditor.getText() != null) {
                    try {
                        source.setJobURI(new URI(jobURIEditor.getText()));
                    } catch (URISyntaxException e) {
                        JOptionPane.showMessageDialog(getRootPane(), e
                                .getMessage(), "Failed to set new Job URI",
                                JOptionPane.PLAIN_MESSAGE);
                        e.printStackTrace();
                    }
                }
                source.setJobAdaptor(jobAdaptorEditor.getText());
                source.setJavaPath(javaPathEditor.getText());
                source.setFileAdaptors(fileAdaptorsEditor.getTextArray());
                source.setServerAdaptor(serverAdaptorEditor.getText());
                source.setCacheDir(cacheDirEditor.getFile());
                if (nodesEditor.getValue() > 0) {
                    source.setNodes(nodesEditor.getValue());
                }
                if (coresEditor.getValue() > 0) {
                    source.setCores(coresEditor.getValue());
                }
                source.setJobWrapperScript(jobWrapperScriptEditor.getFile());
                if (serverURIEditor.getText() != null) {
                    try {
                        source.setServerURI(new URI(serverURIEditor.getText()));
                    } catch (URISyntaxException e) {
                        JOptionPane.showMessageDialog(getRootPane(), e
                                .getMessage(), "Failed to set new Server URI",
                                JOptionPane.PLAIN_MESSAGE);
                        e.printStackTrace();
                    }
                }
                source.setLatitude(geoPositionEditor.getLatitude());
                source.setLongitude(geoPositionEditor.getLongitude());
                source.setOutputFiles(serverOutputFilesEditor.getFileArray());
                clusterEditorPanel.fireClusterEdited(source);
                gui.fireGridUpdated();
            }

        });
    }

}

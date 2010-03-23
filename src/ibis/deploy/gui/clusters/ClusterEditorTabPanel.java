package ibis.deploy.gui.clusters;

import ibis.deploy.Cluster;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.editor.ChangeableField;
import ibis.deploy.gui.editor.FileArrayEditor;
import ibis.deploy.gui.editor.FileEditor;
import ibis.deploy.gui.editor.MapEditor;
import ibis.deploy.gui.editor.NumberEditor;
import ibis.deploy.gui.editor.Spacer;
import ibis.deploy.gui.editor.TextArrayComboBoxEditor;
import ibis.deploy.gui.editor.TextComboBoxEditor;
import ibis.deploy.gui.editor.TextEditor;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.gridlab.gat.AdaptorInfo;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;

public class ClusterEditorTabPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1085273687721913236L;
    
    private final ClusterEditorPanel clusterEditorPanel;
    
    private ArrayList<ChangeableField> fields = new ArrayList<ChangeableField>();
    
    private JButton discardButton = null;
    private JButton applyButton = null;

    public ClusterEditorTabPanel(final Cluster source,
            final ClusterEditorPanel clusterEditorPanelRef, final GUI gui) {
        clusterEditorPanel = clusterEditorPanelRef;
    	
        setLayout(new BorderLayout());    	

//        Cluster defaults = null;
//        if (source.getGrid() != null) {
//            if (source.getName().equals("local")) {
//                defaults = source.getGrid().getLocalDefaults();
//            } else {
//                defaults = source.getGrid().getDefaults();
//            }
//        }

        JPanel container = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.PAGE_AXIS));
        formPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        formPanel.add(new Spacer("General settings"));
        
        final TextEditor nameEditor = new TextEditor(this, formPanel, "Name: ", 
        		source.getName()); 
    	fields.add(nameEditor);
    	formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
     
        final NumberEditor nodesEditor = new NumberEditor(this, formPanel, "Nodes: ",
                source.getNodes());
        fields.add(nodesEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final NumberEditor coresEditor = new NumberEditor(this, formPanel, "Cores: ",
                source.getCores());
        fields.add(coresEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final TextEditor userNameEditor = new TextEditor(this, formPanel,
                "User Name: ", source.getUserName());
        fields.add(userNameEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final TextEditor javaPathEditor = new TextEditor(this, formPanel,
                "Java Path: ", source.getJavaPath());
        fields.add(javaPathEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final FileEditor cacheDirEditor = new FileEditor(this, formPanel,
                "Cache Directory: ", source.getCacheDir());
        fields.add(cacheDirEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

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
                this, formPanel, "File Adaptors: ", source.getFileAdaptors(), fileAdaptors);
        fields.add(fileAdaptorsEditor);        
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        formPanel.add(new Spacer("Jobs"));

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
                this, formPanel, "Job Adaptor: ", source.getJobAdaptor(),jobAdaptors);
        fields.add(jobAdaptorEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final TextEditor jobURIEditor = new TextEditor(this, formPanel, "Job URI: ",
                source.getJobURI());
        fields.add(jobURIEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final FileEditor jobWrapperScriptEditor = new FileEditor(this, formPanel,
                "Job Wrapper Script: ", source.getJobWrapperScript());
        fields.add(jobWrapperScriptEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        formPanel.add(new Spacer("Server"));
        
        final TextComboBoxEditor serverAdaptorEditor = new TextComboBoxEditor(
                this, formPanel, "Server Adaptor: ", source.getServerAdaptor(),
                jobAdaptors);
        fields.add(serverAdaptorEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        final TextEditor serverURIEditor = new TextEditor(this, formPanel,
                "Server URI: ", source.getServerURI());
        fields.add(serverURIEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final FileArrayEditor serverOutputFilesEditor = new FileArrayEditor(
                this, formPanel, "Server Output Files: ", source
                        .getServerOutputFiles());
        fields.add(serverOutputFilesEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        formPanel.add(new Spacer("Geo position"));
 
        final MapEditor geoPositionEditor = new MapEditor(this, formPanel,
                "Geo Position: ", source.getLatitude(), source.getLongitude());
        fields.add(geoPositionEditor);

        container.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(container,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(buttonPanel, BorderLayout.SOUTH);
        
        //button that allows the user to apply the changes in the editors to the source
        applyButton = new JButton("Apply changes");        
        buttonPanel.add(applyButton);
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                
//            	System.out.println(source.toPrintString());
            	
                try 
                {
                	if(nameEditor.getText().length() > 0)
                		source.setName(nameEditor.getText());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getRootPane(), e
                            .getMessage(), "Unable to apply changes",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                    return;
                }
                
                if (nodesEditor.getValue() > 0) {
                    source.setNodes(nodesEditor.getValue());
                }
                
                if (coresEditor.getValue() > 0) {
                    source.setCores(coresEditor.getValue());
                }
                
                if(userNameEditor.getText().length() > 0)
                	source.setUserName(userNameEditor.getText());
                else
                	source.setUserName(null);
                
                if(javaPathEditor.getText().length() > 0)
                	source.setJavaPath(javaPathEditor.getText());
                else
                	source.setJavaPath(null);
                
            	source.setCacheDir(cacheDirEditor.getFile());
                
                source.setFileAdaptors(fileAdaptorsEditor.getTextArray());
                
                source.setJobAdaptor(jobAdaptorEditor.getText());
                
                if (jobURIEditor.getText() != null) 
                {
                    try 
                    {
                    	if(jobURIEditor.getText().length() > 0)
                    		source.setJobURI(new URI(jobURIEditor.getText()));
                    	else
                    		source.setJobURI(null);
                    } catch (URISyntaxException e) {
                        JOptionPane.showMessageDialog(getRootPane(), e
                                .getMessage(), "Failed to set new Job URI",
                                JOptionPane.PLAIN_MESSAGE);
                        e.printStackTrace();
                    }
                }
                
                source.setJobWrapperScript(jobWrapperScriptEditor.getFile());
                
                source.setServerAdaptor(serverAdaptorEditor.getText());
                
                if (serverURIEditor.getText() != null) {
                    try {
                        if(serverURIEditor.getText().length() > 0)
                        	source.setServerURI(new URI(serverURIEditor.getText()));
                        else
                        	source.setServerURI(null);
                    } catch (URISyntaxException e) {
                        JOptionPane.showMessageDialog(getRootPane(), e
                                .getMessage(), "Failed to set new Server URI",
                                JOptionPane.PLAIN_MESSAGE);
                        e.printStackTrace();
                    }
                }
                
                source.setOutputFiles(serverOutputFilesEditor.getFileArray());
                
                source.setLatitude(geoPositionEditor.getLatitude());
                
                source.setLongitude(geoPositionEditor.getLongitude());
                
                //inform all the fields to refresh their initial values, as the
                //changes have been applied
                for(ChangeableField field:fields)
                {
                	field.refreshInitialValue();
                }
                
//                System.out.println(source.toPrintString());
                
                //disable buttons
                applyButton.setEnabled(false);
                discardButton.setEnabled(false);
                
                clusterEditorPanel.fireClusterEdited(source);
                gui.fireGridUpdated();
            }

        });
        
        //button that allows to reset the fields to the initial source values
        discardButton = new JButton("Discard changes");        
        buttonPanel.add(discardButton);
        discardButton.setEnabled(false);
        discardButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				
				nameEditor.setText(source.getName());
                
				nodesEditor.setValue(source.getNodes());
				
				coresEditor.setValue(source.getCores());
                
				userNameEditor.setText(source.getUserName());
				
                javaPathEditor.setText(source.getJavaPath());
                
                if(source.getCacheDir() != null)
                	cacheDirEditor.setFile(source.getCacheDir().toString());
                else
                	cacheDirEditor.setFile(null);
                
                fileAdaptorsEditor.setFileArray(source.getFileAdaptors());
                
                jobAdaptorEditor.setText(source.getJobAdaptor());
            
                if(source.getJobURI() != null)
                	jobURIEditor.setText(source.getJobURI().toString());
                else
                	jobURIEditor.setText(null);
                                
                if(source.getJobWrapperScript() != null)
                	jobWrapperScriptEditor.setFile(source.getJobWrapperScript().toString());
                else
                	jobWrapperScriptEditor.setFile(null);
                
                serverAdaptorEditor.setText(source.getServerAdaptor());
                
                if(source.getServerURI() != null)
                	serverURIEditor.setText(source.getServerURI().toString());
                else
                	serverURIEditor.setText(null);
                
                serverOutputFilesEditor.setFileArray(source.getServerOutputFiles());
                
                geoPositionEditor.setLatitude(source.getLatitude());
                
                geoPositionEditor.setLongitude(source.getLongitude());
                
                //disable buttons
                applyButton.setEnabled(false);
                discardButton.setEnabled(false);
			}
		});
    }

    /**
     * Checks if in any of the fields the value is different from the one in the source cluster.
     * According to that, the apply and discard buttons are enabled / disabled
     */
    public void checkForChanges()
    {
    	boolean hasChanged = false;
    	for(ChangeableField field:fields)
    	{
    		hasChanged = hasChanged || field.hasChanged();
    		if(hasChanged)
    			break;
    	}
    	
    	applyButton.setEnabled(hasChanged);
    	discardButton.setEnabled(hasChanged);
    }
}

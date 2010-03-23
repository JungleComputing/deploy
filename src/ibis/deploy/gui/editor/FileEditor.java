package ibis.deploy.gui.editor;

import ibis.deploy.gui.clusters.ClusterEditorTabPanel;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileEditor implements KeyListener, ChangeableField
{
    /**
     * 
     */
    private static final long serialVersionUID = 2216106418941074147L;

    private final JTextField textField = new JTextField();

    private final JButton openButton = Utils.createImageButton(
            "images/document-open.png", "Select a file", null);

    private String initialFile; 

    private final JPanel tabPanel;
    
    private final JLabel label = new JLabel("", JLabel.TRAILING);

    /**
     * @param form - parent JPanel
     * @param text - text to be displayed in the label
     * @param value - initial text for the textField
     */
    public FileEditor(final JPanel tabPanel, JPanel form, final String text, File value) 
    {

        if (value != null) 
        {
        	textField.setText(value.getPath());
        	initialFile = value.getPath();
        }
        else
        	initialFile = "";
        
        this.tabPanel = tabPanel;
        textField.addKeyListener(this);

        JPanel container = new JPanel(new BorderLayout());

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth,
                label.getPreferredSize().height));
        
        final JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(textField, BorderLayout.CENTER);

        final JFileChooser fileChooser = new JFileChooser(value);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(filePanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getPath());
                    if(tabPanel instanceof ClusterEditorTabPanel)
            			((ClusterEditorTabPanel) tabPanel).checkForChanges();
                }
            }

        });
        
        label.setLabelFor(filePanel);
        filePanel.add(openButton, BorderLayout.EAST);
        container.add(filePanel, BorderLayout.CENTER);
        form.add(container);
    }

    /**
     * @return - new File created using the path in the textField
     */
    public File getFile() 
    {
        if(textField.getText().trim().length() > 0)	
        	return new File(textField.getText());
        else 
        	return null;
    }
    
    /**
     * Sets the text of the textField to the given value
     * @param fileName - new filename to be displayed in the textField
     */
    public void setFile(String fileName)
    {
    	textField.setText(fileName);
    }
    
    @Override
    public void refreshInitialValue()
    {
    	initialFile = textField.getText();
    	if(initialFile == null)
    		initialFile = "";
    }
    
    @Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(tabPanel instanceof ClusterEditorTabPanel)
			((ClusterEditorTabPanel) tabPanel).checkForChanges();
		
		//need to take care, checks are performed also when the file chooser is used
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}

	/**
	 * @return - true if the text field contains a different value than
	 * the initial value
	 */
	@Override
	public boolean hasChanged() 
	{
		return !textField.getText().equals(initialFile);
	}
}

package ibis.deploy.gui.editor;

import ibis.deploy.gui.clusters.ClusterEditorTabPanel;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextEditor implements KeyListener, ChangeableField
{

    private final JTextField textField = new JTextField();
    
    private final JPanel tabPanel;
    
    private String initialText;

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    /**
     * @param form - parent JPanel
     * @param text - label text
     * @param value - initial text value for the editor
     */
    public TextEditor(JPanel tabPanel, JPanel form, final String text, Object value) 
    {
        this(tabPanel, form, text, (value == null) ? null : value.toString());
    }

    /**
     * @param form - parent JPanel
     * @param text - label text
     * @param value - initial text value for the editor
     */
    public TextEditor(JPanel tabPanel, JPanel form, final String text, String value) 
    {

        this.tabPanel = tabPanel;  
        
    	if (value != null) {
            textField.setText(value);
            this.initialText = value;
        }
    	else
    		initialText = "";
    	
    	textField.addKeyListener(this);

        JPanel container = new JPanel(new BorderLayout());
        
        //make the container higher, so that all fields in the parent panel are the same height
        container.setPreferredSize(new Dimension(label.getPreferredSize().width, 
        		Utils.defaultFieldHeight));
        
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(label, BorderLayout.EAST);
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setLabelFor(textField);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth,
                label.getPreferredSize().height));
        container.add(textField, BorderLayout.CENTER);
        form.add(container);
    }

    /**
     * @return - text contained by the text field
     */
    public String getText() 
    {
    	return textField.getText().trim();
    }
    
    /**
     * @param text - new value for the text field
     */
    public void setText(String text)
    {
    	textField.setText(text);
    }
    
    @Override
    public void refreshInitialValue()
    {
    	initialText = textField.getText();
    	if(initialText == null)
    		initialText = "";
    }
    
    @Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(tabPanel instanceof ClusterEditorTabPanel)
			((ClusterEditorTabPanel) tabPanel).checkForChanges();
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
		return !textField.getText().equals(initialText);
	}
}

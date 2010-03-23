package ibis.deploy.gui.editor;

import ibis.deploy.gui.clusters.ClusterEditorTabPanel;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextArrayComboBoxEditor implements ActionListener, ChangeableField
{

    private final List<JComboBox> comboBoxes = new ArrayList<JComboBox>();

    private final List<JButton> removeButtons = new ArrayList<JButton>();

    private final JPanel arrayPanel = new JPanel();

    private final JPanel addPanel = new JPanel(new BorderLayout());

    private final JButton addButton = Utils.createImageButton(
            "images/list-add-small.png", "Add a new item", null);

    private final JLabel label = new JLabel("", JLabel.TRAILING);

    private JPanel parentPanel;
    
    private String[] possibleValues;
    
    private String[] initialValues;
    
    private final JPanel tabPanel;

    /**
     * @param form - parent panel
     * @param text - label text
     * @param values - initial list of strings to be added as selected values for the comboboxes
     * @param possibleValues - possible values for the comboboxes
     */
    public TextArrayComboBoxEditor(final JPanel tabPanel, final JPanel form, final String text,
            String[] values, final String[] possibleValues) 
    {
        this.parentPanel = form;
        this.possibleValues = possibleValues;
        
        this.tabPanel = tabPanel;
        if(values != null)
        	this.initialValues = values;
        else
        	initialValues = new String[0];

        arrayPanel.setLayout(new BoxLayout(arrayPanel, BoxLayout.PAGE_AXIS));
        
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addField(null, arrayPanel, possibleValues);
                arrayPanel.getRootPane().repaint();
                
                informParent();
            }

        });

        addPanel.add(addButton, BorderLayout.WEST);
        arrayPanel.add(addPanel);
        
        if (values != null) {
            for (String value : values) {
                addField(value, arrayPanel, possibleValues);
            }
        }

        JPanel container = new JPanel(new BorderLayout());
        
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(label);
        labelPanel.add(Box.createVerticalGlue());
        container.add(labelPanel, BorderLayout.WEST);
        label.setText(text);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label.setLabelFor(arrayPanel);
        label.setPreferredSize(new Dimension(Utils.defaultLabelWidth,
                label.getPreferredSize().height));
        
        container.add(arrayPanel, BorderLayout.CENTER);
        form.add(container);
    }

    /**
     * @param value - value to be selected in the combobox
     * @param panel - parent panel
     * @param possibleValues - list of possible values for the combobox
     */
    private void addField(String value, final JPanel panel,
            String[] possibleValues) {
        final JPanel arrayItemPanel = new JPanel(new BorderLayout());
        final JComboBox comboBox = new JComboBox(possibleValues);
        for (String possibleValue : possibleValues) {
            if (possibleValue.equalsIgnoreCase(value)) {
                comboBox.setSelectedItem(possibleValue);
            }
        }

        comboBoxes.add(comboBox);
        arrayItemPanel.add(comboBox, BorderLayout.CENTER);
        comboBox.addActionListener(this);
        
        final Component rigidArea = Box.createRigidArea(new Dimension(0, Utils.gapHeight));
        panel.add(rigidArea);

        final JButton removeButton = Utils.createImageButton(
                "images/list-remove-small.png", "Remove item", null);
        removeButtons.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                comboBoxes.remove(comboBox);
                arrayItemPanel.remove(removeButton);
                panel.remove(arrayItemPanel);
                panel.remove(rigidArea);
                panel.getRootPane().repaint();
                
                informParent();
            }
        });
        arrayItemPanel.add(removeButton, BorderLayout.WEST);
        panel.add(arrayItemPanel);
    }

    /**
     * @return - an array of strings containing the selected values in all the comboboxes
     */
    public String[] getTextArray() {
        if (comboBoxes.size() > 0) {
            String[] result = new String[comboBoxes.size()];
            int i = 0;
            for (JComboBox comboBox : comboBoxes) {
                result[i] = comboBox.getSelectedItem().toString();
                i++;
            }
            return result;
        } else {
            return null;
        }
    }
    
    private void clearAllFields()
    {
    	arrayPanel.removeAll();
    	arrayPanel.add(addPanel);
    }
    
    /**
     * adds a field for each of the new values 
     * @param values - new list of values
     */
    public void setFileArray(String[] values)
    {
    	clearAllFields();
    	
    	if (values != null) 
    	{
            for (String value : values) 
            {
                addField(value, arrayPanel, possibleValues);
            }
        } 	
    	parentPanel.getRootPane().repaint();
    }
    
    @Override
    public void refreshInitialValue()
    {
    	initialValues = getTextArray();
    	if(initialValues == null)
    		initialValues = new String[0];
    }

	@Override
	public void actionPerformed(ActionEvent arg0) {
		informParent();
	}
	
	/**
	 * @return - true if the selected values in the comboboxes are different 
	 * than the initially selected values. Also returns true if the number of
	 * comboboxes has changed.
	 */
	public boolean hasChanged()
	{
		if(comboBoxes.size() != initialValues.length)
			return true;
		
		int i = 0;
		for (JComboBox comboBox : comboBoxes) 
		{
            if(!comboBox.getSelectedItem().toString().equalsIgnoreCase(initialValues[i]))
            	return true;
            i++;
        }
		
		return false;
	}
	
	/**
	 * Informs the parent to check for changes
	 */
	private void informParent()
	{
		//the editor's configuration has changed, the parent panel must check for changes
        if(tabPanel instanceof ClusterEditorTabPanel)
			((ClusterEditorTabPanel) tabPanel).checkForChanges();
	}
}

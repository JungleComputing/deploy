package ibis.deploy.gui.applications;

import ibis.deploy.Application;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.editor.ChangeableField;
import ibis.deploy.gui.editor.FileArrayEditor;
import ibis.deploy.gui.editor.FileEditor;
import ibis.deploy.gui.editor.NumberEditor;
import ibis.deploy.gui.editor.Spacer;
import ibis.deploy.gui.editor.TextArrayEditor;
import ibis.deploy.gui.editor.TextEditor;
import ibis.deploy.gui.editor.TextMapArrayEditor;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class ApplicationEditorTabPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1085273687721913236L;

    private ArrayList<ChangeableField> fields = new ArrayList<ChangeableField>();

    private JButton discardButton = null;
    private JButton applyButton = null;

    public ApplicationEditorTabPanel(final Application source,
            final ApplicationEditorPanel applicationEditorPanel, final GUI gui) {
        setLayout(new BorderLayout());

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

        formPanel.add(new Spacer("Runtime"));
        final TextEditor mainEditor = new TextEditor(this, formPanel,
                "Main Class: ", source.getMainClass());
        fields.add(mainEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        final TextArrayEditor argumentsEditor = new TextArrayEditor(this,
                formPanel, "Arguments: ", source.getArguments());
        fields.add(argumentsEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        final FileArrayEditor libsEditor = new FileArrayEditor(this, formPanel,
                "Libraries: ", source.getLibs());
        fields.add(libsEditor);
        
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        
        final NumberEditor memoryEditor = new NumberEditor(this, formPanel,
                "Memory (MB): ", source.getMemorySize(), true);
        fields.add(memoryEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        formPanel.add(new Spacer("JVM"));
        final TextArrayEditor jvmOptionsEditor = new TextArrayEditor(this,
                formPanel, "JVM Options: ", source.getJVMOptions());
        fields.add(jvmOptionsEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        final TextMapArrayEditor systemPropertiesEditor = new TextMapArrayEditor(
                this, formPanel, "JVM System Properties: ", source
                        .getSystemProperties());
        fields.add(systemPropertiesEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        formPanel.add(new Spacer("I/O"));
        final FileArrayEditor inputFilesEditor = new FileArrayEditor(this,
                formPanel, "Input Files: ", source.getInputFiles());
        fields.add(inputFilesEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));
        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        final FileArrayEditor outputFilesEditor = new FileArrayEditor(this,
                formPanel, "Output Files: ", source.getOutputFiles());
        fields.add(outputFilesEditor);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        final FileEditor log4jFile = new FileEditor(this, formPanel,
                "Log4j File: ", source.getLog4jFile());
        fields.add(log4jFile);

        formPanel.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

        container.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(container,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(buttonPanel, BorderLayout.SOUTH);

        applyButton = new JButton("Apply");
        buttonPanel.add(applyButton);
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    source.setName(nameEditor.getText());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            e.getMessage(), "Unable to apply changes",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                    return;
                }
                source.setMainClass(mainEditor.getText());
                source.setArguments(argumentsEditor.getTextArray());
                source.setSystemProperties(systemPropertiesEditor.getTextMap());
                source.setJVMOptions(jvmOptionsEditor.getTextArray());
                source.setLibs(libsEditor.getFileArray());
                source.setInputFiles(inputFilesEditor.getFileArray());
                source.setOutputFiles(outputFilesEditor.getFileArray());
                source.setLog4jFile(log4jFile.getFile());
                
                if(memoryEditor.getValue() > 0)
                    source.setMemorySize(memoryEditor.getValue());
                
                applicationEditorPanel.fireApplicationEdited(source);

                applyButton.setEnabled(false);
                discardButton.setEnabled(false);
            }
        });

        // button that allows to reset the fields to the initial source values
        discardButton = new JButton("Discard changes");
        buttonPanel.add(discardButton);
        discardButton.setEnabled(false);
        discardButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                nameEditor.setText(source.getName());

                mainEditor.setText(source.getMainClass());

                argumentsEditor.setTextArray(source.getArguments());

                systemPropertiesEditor.setTextMap(source.getSystemProperties());

                jvmOptionsEditor.setTextArray(source.getJVMOptions());

                libsEditor.setFileArray(source.getLibs());

                inputFilesEditor.setFileArray(source.getInputFiles());

                outputFilesEditor.setFileArray(source.getOutputFiles());

                if (source.getLog4jFile() != null) {
                    log4jFile.setFile(source.getLog4jFile().toString());
                } else
                    log4jFile.setFile("");
                
                memoryEditor.setValue(source.getMemorySize());

                applyButton.setEnabled(false);
                discardButton.setEnabled(false);
            }
        });
    }

    /**
     * Checks if in any of the fields the value is different from the one in the
     * source. According to that, the apply and discard buttons are
     * enabled / disabled
     */
    public void checkForChanges() {
        boolean hasChanged = false;
        for (ChangeableField field : fields) {
            hasChanged = hasChanged || field.hasChanged();
            if (hasChanged)
                break;
        }

        applyButton.setEnabled(hasChanged);
        discardButton.setEnabled(hasChanged);
    }
}

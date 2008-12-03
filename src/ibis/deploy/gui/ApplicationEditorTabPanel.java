package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

public class ApplicationEditorTabPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1085273687721913236L;

    public ApplicationEditorTabPanel(final Application source,
            final ApplicationEditorPanel applicationEditorPanel, final GUI gui) {
        this(source, applicationEditorPanel, gui, false);
    }

    public ApplicationEditorTabPanel(final Application source,
            final ApplicationEditorPanel applicationEditorPanel, final GUI gui,
            final boolean noNameEditor) {
        setLayout(new BorderLayout());
        Application defaults = source.getApplicationSet().getDefaults();

        JPanel container = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.PAGE_AXIS));
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextEditor nameEditor = (noNameEditor) ? null : new TextEditor(
                formPanel, "Name: ", source.getName(), "");
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextEditor mainEditor = new TextEditor(formPanel, "Main Class: ",
                source.getMainClass(), defaults.getMainClass());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextArrayEditor argumentsEditor = new TextArrayEditor(formPanel,
                "Arguments: ", source.getArguments(), defaults.getArguments());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final TextArrayEditor jvmOptionsEditor = new TextArrayEditor(formPanel,
                "JVM Options: ", source.getJVMOptions(), defaults
                        .getJVMOptions());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final FileArrayEditor libsEditor = new FileArrayEditor(formPanel,
                "Libraries: ", source.getLibs(), defaults.getLibs());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final FileArrayEditor inputFilesEditor = new FileArrayEditor(formPanel,
                "Input Files: ", source.getInputFiles(), defaults
                        .getInputFiles());
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        final FileArrayEditor outputFilesEditor = new FileArrayEditor(
                formPanel, "Output Files: ", source.getOutputFiles(), defaults
                        .getOutputFiles());
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

            public void actionPerformed(ActionEvent e) {
                if (!noNameEditor) {
                    source.setName(nameEditor.getText());
                }
                source.setMainClass(mainEditor.getText());
                source.setArguments(argumentsEditor.getTextArray());
                source.setJVMOptions(jvmOptionsEditor.getTextArray());
                source.setLibs(libsEditor.getFileArray());
                source.setInputFiles(inputFilesEditor.getFileArray());
                source.setOutputFiles(outputFilesEditor.getFileArray());
                applicationEditorPanel.fireApplicationEdited(source);
            }

        });
    }
}

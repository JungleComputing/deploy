package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.Job;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class OutputPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2448246780577566271L;

    public OutputPanel(Job job) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        if (job == null) {
            add(new JLabel("Error: no output available"));
            return;
        }
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Output Files for '"
                + job.getDescription().getName() + "'"));

        final String stdout = job.getDescription().getPoolName() + "."
                + job.getDescription().getName() + ".out";
        JLabel stdoutLabel = new JLabel("<html><a href=.>" + stdout
                + "</a></html>", JLabel.TRAILING);

        stdoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stdoutLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new File(stdout).toURI());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(OutputPanel.this, e
                            .getMessage(), "Failed to open '" + stdout + "'",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                }
            }

        });
        JPanel stdoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        stdoutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel("Standard Output: ");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        stdoutPanel.add(label);
        stdoutPanel.add(stdoutLabel);
        container.add(stdoutPanel, Component.LEFT_ALIGNMENT);

        final String stderr = job.getDescription().getPoolName() + "."
                + job.getDescription().getName() + ".err";
        JLabel stderrLabel = new JLabel("<html><a href=.>" + stderr
                + "</a></html>", JLabel.TRAILING);
        stderrLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stderrLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new File(stderr).toURI());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(OutputPanel.this, e
                            .getMessage(), "Failed to open '" + stderr + "'",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                }
            }

        });
        JPanel stderrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        stderrPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stderrPanel.add(new JLabel("Standard Error: "));
        stderrPanel.add(stderrLabel);
        container.add(stderrPanel);

        container.add(Box.createVerticalStrut(20));
        container.add(new JLabel("Other Files: "));
        if (job.getApplication().getOutputFiles() == null) {
            container.add(new JLabel("No other output files"));
        } else {
            for (final File outputFile : job.getApplication().getOutputFiles()) {
                JLabel outputFileLabel = new JLabel("<html><a href=.>"
                        + outputFile.getName() + "</a></html>");
                outputFileLabel.setCursor(Cursor
                        .getPredefinedCursor(Cursor.HAND_CURSOR));
                container.add(outputFileLabel);
                outputFileLabel.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                        try {
                            java.awt.Desktop.getDesktop().browse(
                                    new File(outputFile.getName()).toURI());
                        } catch (IOException e) {

                            JOptionPane.showMessageDialog(OutputPanel.this, e
                                    .getMessage(), "Failed to open '"
                                    + outputFile.getName() + "'",
                                    JOptionPane.PLAIN_MESSAGE);
                            e.printStackTrace(System.err);
                        }
                    }

                });

            }
        }

        if (job.getException() != null) {
            container.add(new JLabel("Exception of job: "));

            Writer stackTraceString = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceString);
            job.getException().printStackTrace(printWriter);

            JTextArea stackTrace = new JTextArea(stackTraceString.toString(),
                    24, 80);
            stackTrace.setLineWrap(true);
            stackTrace.setAutoscrolls(false);
            stackTrace.setEditable(false);

            container.add(stackTrace);
        }

        add(container);
    }
}
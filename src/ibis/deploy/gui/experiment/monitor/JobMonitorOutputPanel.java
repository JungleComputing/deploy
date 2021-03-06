package ibis.deploy.gui.experiment.monitor;

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

public class JobMonitorOutputPanel extends JPanel {

    private final class ShowFileListener extends MouseAdapter {
        private final File file;

        private ShowFileListener(File file) {
            this.file = file;
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
            if (file.exists()) {
                try {
                    java.awt.Desktop.getDesktop().browse(file.toURI());
                } catch (IOException e) {

                    JOptionPane.showMessageDialog(JobMonitorOutputPanel.this, e
                            .getMessage(), "Failed to open '" + file.getName()
                            + "'", JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                }
            } else {
                JOptionPane.showMessageDialog(JobMonitorOutputPanel.this,
                        "File not Found", "file \"" + file
                                + "\" does not exist",
                        JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2448246780577566271L;

    public JobMonitorOutputPanel(Job job) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        if (job == null) {
            add(new JLabel("Error: no output available"));
            return;
        }
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Output Files for '"
                + job.getDescription().getName() + "'"));

       
        final File stdout = new File(job.getDescription().getPoolName() + "."
                + job.getDescription().getName() + ".out.txt");
        
        JLabel stdoutLabel = new JLabel("<html><a href=.>" + stdout
                + "</a></html>", JLabel.TRAILING);
        
        if (!stdout.exists()) {
            stdoutLabel.setText("unavailable");
        }

        stdoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stdoutLabel.addMouseListener(new ShowFileListener(stdout));
        JPanel stdoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        stdoutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel("Standard Output: ");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        stdoutPanel.add(label);
        stdoutPanel.add(stdoutLabel);
        container.add(stdoutPanel, Component.LEFT_ALIGNMENT);

        final File stderr = new File(job.getDescription().getPoolName() + "."
                + job.getDescription().getName() + ".err.txt");
        JLabel stderrLabel = new JLabel("<html><a href=.>" + stderr
                + "</a></html>", JLabel.TRAILING);
        
        if (!stderr.exists()) {
            stderrLabel.setText("unavailable");
        }

        stderrLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stderrLabel.addMouseListener(new ShowFileListener(stderr));
        
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
                File file = new File(outputFile.getName());

                JLabel outputFileLabel = new JLabel("<html><a href=.>"
                        + outputFile.getName() + "</a></html>");
                outputFileLabel.setCursor(Cursor
                        .getPredefinedCursor(Cursor.HAND_CURSOR));
                container.add(outputFileLabel);
                outputFileLabel.addMouseListener(new ShowFileListener(file));

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

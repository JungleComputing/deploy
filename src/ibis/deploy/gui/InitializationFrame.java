package ibis.deploy.gui;

import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class InitializationFrame extends JFrame implements StateListener,
        Runnable {

    private static final long serialVersionUID = 1L;

    private final JProgressBar progressBar;

    InitializationFrame() {
        this.setTitle("Initializing");

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setIconImage(Utils.createImageIcon("images/favicon.ico", null)
                .getImage());
        //center on screen
        this.setLocationRelativeTo(null);

        progressBar = new JProgressBar();
        progressBar.setString("" + State.CREATED);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(this);
    }

    // Initialize Ibis Deploy lib, update content of panel when done
    public void run() {
        try {
            // create status dialog
            JPanel panel = new JPanel();
            panel.setBackground(Color.WHITE);
            panel.setOpaque(true);
            panel.setLayout(new BorderLayout());

            JLabel logo = Utils.createImageLabel("images/ibis-logo.png",
                    "Ibis Logo");
            panel.add(logo, BorderLayout.NORTH);

            JTextArea text = new JTextArea(4, 25);
            text.setEditable(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setText("\nInitializing Ibis-Deploy.\n"
                    + "Please wait for the server to be deployed.\n");
            panel.add(text, BorderLayout.CENTER);

            panel.add(progressBar, BorderLayout.SOUTH);
            setContentPane(panel);

            //setPreferredSize(new Dimension(300, 200));

            pack();
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            JOptionPane.showMessageDialog(getRootPane(), writer.toString(),
                    "Initialize failed", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public void remove() {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dispose();
            }
        });
    }

    @Override
    public void stateUpdated(State state, Exception exception) {
        progressBar.setString("" + state);
    }
}

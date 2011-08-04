package ibis.deploy.vizFramework.globeViz.viz;

/**
 * @author Ana Vinatoru
 *
 */
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;

public class Main {

    public static void main(String[] args) {

        // build Java swing interface

        //TODO this will very likely not work. I'll remove it at some point
        GlobeVisualization globe = new GlobeVisualization(null);

        JFrame frame = new JFrame("World Wind");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPanel = new JPanel();
        BorderLayout layout = new BorderLayout();
        contentPanel.setLayout(layout);

        contentPanel.add(globe, BorderLayout.CENTER);
        
        JPanel phoneyPanel = new JPanel();
        phoneyPanel.setSize(800, 100);
        phoneyPanel.setBackground(Color.cyan);
        
        contentPanel.add(phoneyPanel, BorderLayout.NORTH);
        
        frame.add(contentPanel);
        frame.setSize(800, 600);
        frame.setVisible(true);

    }

}

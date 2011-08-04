package ibis.deploy.vizFramework;

import ibis.deploy.gui.DetachableTab;
import ibis.deploy.gui.GUI;
import ibis.deploy.monitoring.collection.exceptions.SingletonObjectNotInstantiatedException;
import ibis.deploy.monitoring.collection.impl.CollectorImpl;
import ibis.deploy.vizFramework.bundles.BundlesVisualization;
import ibis.deploy.vizFramework.globeViz.GlobePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.color.ColorUtil;

import prefuse.util.ColorLib;

/**
 * @author Ana Vinatoru
 * 
 */

public class VisualizationPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String REAL_DATA                       = "Real data";
    private static final String REAL_DATA_RECORDING             = "Real data - recording";
    private static final String READING_FROM_FILE_STARTING      = "Reading from file - loaded - ";
    private static final String READING_FROM_FILE_ONGOING       = "Reading from file - ";
    private static final String READING_FROM_FILE_DONE          = "Reading from file - done - ";

    private String currentStatus = REAL_DATA;
    private static VisualizationPanel panel;

    private final JLabel statusLabel;
    private final JButton playButton;
    private final JButton revertToRealDataButton;
    private final JButton recordButton;
    private final JButton loadButton;
    private final JFileChooser chooser;
    
    private String fileName;

    public VisualizationPanel(final GUI gui) {
        setLayout(new BorderLayout(5, 5));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        BundlesVisualization bundlePanel = new BundlesVisualization(gui);

        DetachableTab deployVizTab = new DetachableTab("Connection View",
                "images/gridvision.png", bundlePanel, tabbedPane);

        GlobePanel globePanel = new GlobePanel(gui);

        DetachableTab globeTab = new DetachableTab("Global View",
                "images/gridvision.png", globePanel, tabbedPane);

        ArrayList<IVisualization> visualizations = new ArrayList<IVisualization>();
        visualizations.add(globePanel.getGlobe());
        visualizations.add(bundlePanel);

        final MetricManager mgr = MetricManager.getMetricManager(
                gui.getCollector(), visualizations);

        final JPanel topPanel = new JPanel();
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        topPanel.setLayout(flowLayout);
        // BoxLayout layout = new BoxLayout(topPanel,
        // BoxLayout.LINE_AXIS);
        // topPanel.setLayout(layout);

        topPanel.add(Box.createRigidArea(new Dimension(133, 20)));

        JLabel statusL = new JLabel("Current status:  ");
        Font newLabelFont = new Font(statusL.getFont().getName(), Font.BOLD,
                statusL.getFont().getSize());
        statusL.setFont(newLabelFont);
        topPanel.add(statusL);

        statusLabel = new JLabel(currentStatus + "  ");
        int fixedHeight = statusLabel.getPreferredSize().height;

        Dimension fixedSize = new Dimension(200, fixedHeight);
        statusLabel.setMinimumSize(fixedSize);
        statusLabel.setMaximumSize(fixedSize);
        statusLabel.setPreferredSize(fixedSize);
        statusLabel.setFont(newLabelFont);
        topPanel.add(statusLabel);

        topPanel.add(Box.createRigidArea(new Dimension(20, 10)));

        topPanel.add(Box.createRigidArea(new Dimension(20, 20)));

        loadButton = new JButton("Load");
        chooser = new JFileChooser(); // we will reuse this
        loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                chooser.setDialogTitle("Load from file ...");
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                int returnVal = chooser.showOpenDialog(topPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    playButton.setText("Play");
                    playButton.setEnabled(true);
                    revertToRealDataButton.setEnabled(true);
                    recordButton.setEnabled(false);
                    
                    fileName = chooser.getSelectedFile().getName();
                    statusLabel.setText(READING_FROM_FILE_STARTING + fileName);
                    recordButton.setEnabled(false);
                    try {
                        CollectorImpl.getCollector().prepareForImport(chooser.getSelectedFile());
                    } catch (SingletonObjectNotInstantiatedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        topPanel.add(loadButton);

        playButton = new JButton("Play");
        playButton.setEnabled(false);
        playButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JButton source = (JButton) arg0.getSource();
                    revertToRealDataButton.setEnabled(true);

                    if (source.getText().equals("Play")) {
                        source.setText("Pause");
                        CollectorImpl.getCollector().toggleImport(false);
                        mgr.togglePause(false);
                        statusLabel.setText(READING_FROM_FILE_ONGOING + fileName);
                    } else {
                        source.setText("Play");
                        CollectorImpl.getCollector().toggleImport(true);
                        mgr.togglePause(true);
                    }

                } catch (SingletonObjectNotInstantiatedException e) {
                    e.printStackTrace();
                }
            }
        });
        topPanel.add(playButton);

        revertToRealDataButton = new JButton("Real data");
        revertToRealDataButton.setEnabled(false);
        revertToRealDataButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    CollectorImpl.getCollector().revertToRealData();
                } catch (SingletonObjectNotInstantiatedException e) {
                    e.printStackTrace();
                }
                revertToRealDataButton.setEnabled(false);
                recordButton.setEnabled(true);
                statusLabel.setText(REAL_DATA);
            }
        });
        topPanel.add(revertToRealDataButton);

        recordButton = new JButton("Record");
        recordButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JButton source = (JButton) event.getSource();
                if (source.getText().equals("Record")) {
                    chooser.setDialogTitle("Record to file ...");
                    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                    int returnVal = chooser.showOpenDialog(topPanel);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            CollectorImpl.getCollector().startWritingToFile(chooser.getSelectedFile());
                        } catch (SingletonObjectNotInstantiatedException e) {
                            e.printStackTrace();
                        }
                        source.setText("Stop recording");
                        loadButton.setEnabled(false);
                        playButton.setEnabled(false);
                        statusLabel.setText(REAL_DATA_RECORDING);
                    }
                } else {
                    source.setText("Record");
                    loadButton.setEnabled(true);
                    playButton.setEnabled(true);
                    statusLabel.setText(REAL_DATA);
                    try {
                        CollectorImpl.getCollector().stopWritingToFile();
                    } catch (SingletonObjectNotInstantiatedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        topPanel.add(recordButton);

        topPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public static VisualizationPanel getVisualizationPanel(GUI gui) {
        if (panel == null && gui != null) {
            panel = new VisualizationPanel(gui);
        }
        return panel;
    }

    public void resetPlayButton() {
        playButton.setText("Play");
        statusLabel.setText(READING_FROM_FILE_DONE + fileName);
    }
}

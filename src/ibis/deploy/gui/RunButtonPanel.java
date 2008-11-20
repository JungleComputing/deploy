package ibis.deploy.gui;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;

public class RunButtonPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 5177904946395126314L;

    public RunButtonPanel(final GUI gui, final MyTableModel model) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JButton createButton = GUIUtils.createImageButton("images/go-down.png",
                "create this job, but don't run it", "create");
        JButton createAndRunButton = GUIUtils.createImageButton(
                "images/go-next.png", "create and run this job", "run");
        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    JobDescription result = gui.getCurrentJobDescription()
                            .resolve(gui.getApplicationSet(), gui.getGrid());
                    result.checkSettings();
                    model.addRow(result);
                    model.fireTableChanged(new TableModelEvent(model));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            e.getMessage(), "Job creation failed",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                }
            }

        });
        createAndRunButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    JobDescription result = gui.getCurrentJobDescription()
                            .resolve(gui.getApplicationSet(), gui.getGrid());
                    result.checkSettings();
                    result.setSharedHub(gui.getSharedHubs());
                    final int row = model.getRowCount();
                    Job job = gui.getDeploy().submitJob(result,
                            gui.getApplicationSet(), gui.getGrid(),
                            new MetricListener() {

                                public void processMetricEvent(MetricEvent event) {
                                    model.setValueAt(event.getValue()
                                            .toString(), row, 2);
                                }

                            }, new MetricListener() {

                                public void processMetricEvent(MetricEvent event) {
                                    model.setValueAt(event.getValue()
                                            .toString(), row, 3);
                                }

                            });
                    model.addRow(job);
                    model.fireTableChanged(new TableModelEvent(model));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            e.getMessage(), "Job creation failed",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                }
            }

        });
        add(createButton);
        add(createAndRunButton);
    }

}
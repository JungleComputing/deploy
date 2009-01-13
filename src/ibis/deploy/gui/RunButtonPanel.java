package ibis.deploy.gui;

import ibis.deploy.AlreadyExistsException;
import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.StateListener;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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

    private static int id = 0;

    public RunButtonPanel(final GUI gui, final JobTableModel model) {

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JButton createButton = GUIUtils.createImageButton(
                "/images/list-add.png", "create this job, but don't start it",
                "Create");
        JButton createAndRunButton = GUIUtils.createImageButton(
                "/images/go-next.png", "create and start this job",
                "Create & Start");
        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    JobDescription result = getJobDescription(gui);
                    gui.fireSubmitJob(result);
                    // result = result.resolve(gui.getApplicationSet(), gui
                    // .getGrid());
                    //                    result.checkSettings();
                    result.setSharedHub(gui.getSharedHubs());
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
                    JobDescription result = getJobDescription(gui);
                    gui.fireSubmitJob(result);
                    result = result.resolve(gui.getApplicationSet(), gui
                            .getGrid());
                    result.checkSettings();
                    result.setSharedHub(gui.getSharedHubs());
                    final int row = model.getRowCount();
                    Job job = gui.getDeploy().submitJob(result,
                            gui.getApplicationSet(), gui.getGrid(),
                            new StateListener() {

                                public void stateUpdated(State state, Exception e) {
                                    model.setValueAt(state.toString(), row, 3);
                                }

                            }, new StateListener() {

                                public void stateUpdated(State state, Exception e) {
                                    model.setValueAt(state.toString(), row, 4);
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

    static private JobDescription getJobDescription(GUI gui) throws Exception {
        JobDescription result = null;
        while (result == null) {
            try {
                result = gui.getExperiment().createNewJob("job-" + id);
            } catch (AlreadyExistsException e) {
                id++;
            }
        }
        return result;
    }

}

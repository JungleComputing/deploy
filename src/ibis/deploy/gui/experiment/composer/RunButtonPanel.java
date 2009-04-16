package ibis.deploy.gui.experiment.composer;

import ibis.deploy.AlreadyExistsException;
import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.jobs.JobRowObject;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.misc.Utils;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;

public class RunButtonPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 5177904946395126314L;

    private static int id = 0;

    public RunButtonPanel(final GUI gui, final JobTableModel model) {

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JButton createButton = Utils.createImageButton("/images/list-add.png",
                "create this job, but don't start it", "Create");
        JButton createAndRunButton = Utils.createImageButton(
                "/images/go-next.png", "create and start this job",
                "Create & Start");
        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    JobDescription result = getJobDescription(gui);
                    gui.fireSubmitJob(result);
                    // result = result.resolve(gui.getApplicationSet(), gui
                    // .getGrid());
                    // result.checkSettings();
                    result.setSharedHub(gui.getSharedHubs());
                    model.addRow(new JobRowObject(result, null));
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
                    // result = result.resolve(gui.getApplicationSet(), gui
                    // .getGrid());
                    // result.checkSettings();
                    result.setSharedHub(gui.getSharedHubs());
                    final int row = model.getRowCount();
                    final JobRowObject jobRow = new JobRowObject(result, null);
                    Job job = gui.getDeploy().submitJob(result,
                            gui.getApplicationSet(), gui.getGrid(),
                            new StateListener() {

                                public void stateUpdated(State state,
                                        Exception e) {
                                    jobRow.setJobState(state.toString());
                                    model.setValueAt(state.toString(), row, 3);
                                }

                            }, new StateListener() {

                                public void stateUpdated(State state,
                                        Exception e) {
                                    jobRow.setHubState(state.toString());
                                    model.setValueAt(state.toString(), row, 4);
                                }

                            });
                    jobRow.setJob(job);
                    model.addRow(jobRow);
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

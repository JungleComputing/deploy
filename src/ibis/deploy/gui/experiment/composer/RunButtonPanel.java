package ibis.deploy.gui.experiment.composer;

import ibis.deploy.AlreadyExistsException;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.misc.Utils;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
                    //cause elements to fill job description with user selections
                    gui.fireSubmitJob(result);
                    model.addJob(result, false);
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
                    //cause elements to fill job description with user selections
                    gui.fireSubmitJob(result);
                    model.addJob(result, true);
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
                result = gui.getExperiment().createNewJob(String.format("job-%02d",id));
            } catch (AlreadyExistsException e) {
                id++;
            }
        }
        return result;
    }

}

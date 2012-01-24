package ibis.deploy.gui.experiment.jobs;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.deploy.gui.GUI;

public class JobRow {

    private static final Logger logger = LoggerFactory.getLogger(JobRow.class);

    public static final int CONTROL_COLUMN = 0;
    public static final int POOL_COLUMN = 1;
    public static final int NAME_COLUMN = 2;
    public static final int JOB_STATUS_COLUMN = 3;
    public static final int HUB_STATUS_COLUMN = 4;
    public static final int RESOURCE_COLUMN = 5;
    public static final int MIDDLEWARE_COLUMN = 6;
    public static final int APPLICATION_COLUMN = 7;
    public static final int PROCESS_COUNT_COLUMN = 8;
    public static final int RESOURCE_COUNT_COLUMN = 9;
    public static final int RUNTIME_COLUMN = 10;

    public static final int OUTPUT_COLUMN = 11;

    public static final int NUMBER_OF_COLUMNS = 12;

    private JobDescription jobDescription;

    private Job job;

    private State hubState;

    private State jobState;

    private final JobTableModel model;
    private final GUI gui;

    public JobRow(JobDescription jobDescription, JobTableModel model, GUI gui)
            throws Exception {
        this.jobDescription = jobDescription.resolve(gui.getApplicationSet(), gui.getJungle());
        this.model = model;
        this.gui = gui;

        job = null;
        jobState = State.UNKNOWN;
        hubState = State.UNKNOWN;
    }

    public Job getJob() {
        return job;
    }
    
    public void remove() {
        gui.getExperiment().removeJob(jobDescription);
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public Object getValue(int col) {
        switch (col) {
        case CONTROL_COLUMN:
            if (job == null || job.isFinished()) {
                // job not running -> display start button
                return Boolean.TRUE;
            } else {
                // job running -> display stop button
                return Boolean.FALSE;
            }
        case POOL_COLUMN:
            return jobDescription.getPoolName();
        case NAME_COLUMN:
            return jobDescription.getName();
        case JOB_STATUS_COLUMN:
            return jobState;
        case HUB_STATUS_COLUMN:
            return hubState;
        case RESOURCE_COLUMN:
            return jobDescription.getResource().getName();
        case MIDDLEWARE_COLUMN:
            return jobDescription.getResource().getJobURI().getScheme();
        case APPLICATION_COLUMN:
            return jobDescription.getApplication().getName();
        case PROCESS_COUNT_COLUMN:
            return jobDescription.getProcessCount();
        case RESOURCE_COUNT_COLUMN:
            return jobDescription.getResourceCount();
        case RUNTIME_COLUMN:
            return jobDescription.getRuntime();
        case OUTPUT_COLUMN:
            if (job == null) {
                // no job, so no output
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }
        default:
            logger.error("Unknown column:" + col);
            return null;
        }
    }

    public static Class<?> getColumnClass(int column) {
        switch (column) {
        case CONTROL_COLUMN:
        case OUTPUT_COLUMN:
            return Boolean.class;

        case APPLICATION_COLUMN:
        case POOL_COLUMN:
        case NAME_COLUMN:
        case RESOURCE_COLUMN:
        case MIDDLEWARE_COLUMN:
            return String.class;

        case JOB_STATUS_COLUMN:
        case HUB_STATUS_COLUMN:
            return State.class;

        case PROCESS_COUNT_COLUMN:
        case RESOURCE_COUNT_COLUMN:
        case RUNTIME_COLUMN:
            return Integer.class;

        default:
            return Object.class;
        }
    }

    private void setJobState(State state) {
        this.jobState = state;

        logger.info("JobRow: Job" + this.getJob() + " state now " + state);
        
        model.fireTableDataChanged();
    }

    private void setHubState(State state) {
        this.hubState = state;
        model.fireTableDataChanged();
    }

    void stop() {
        if (job == null || job.isFinished()) {
            // job already stopped
            return;
        }

        //kill job in a separate thread
        new Thread() {
            public void run() { 
                job.kill();
            }
        }.start();
    }

    void start() {
        if (job != null && !job.isFinished()) {
            System.err.println("Job already started");
            // job already started
            return;
        }

        try {
            logger.info("submitting job" + jobDescription.getName());
            job = gui.getDeploy().submitJob(jobDescription,
                    gui.getApplicationSet(), gui.getJungle(),
                    new StateListener() {

                        public void stateUpdated(final State state, Exception e) {
                            javax.swing.SwingUtilities
                                    .invokeLater(new Runnable() {
                                        public void run() {
                                            setJobState(state);
                                        }
                                    });
                        }

                    }, new StateListener() {
                        public void stateUpdated(final State state, Exception e) {
                            javax.swing.SwingUtilities
                                    .invokeLater(new Runnable() {
                                        public void run() {
                                            setHubState(state);
                                        }
                                    });
                        }

                    });

            jobDescription = job.getDescription();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gui.getFrame(), e.getMessage(),
                    "Job submission failed: " + e, JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }
    }

}

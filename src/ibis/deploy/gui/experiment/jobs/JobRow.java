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
    public static final int CLUSTER_COLUMN = 5;
    public static final int MIDDLEWARE_COLUMN = 6;
    public static final int APPLICATION_COLUMN = 7;
    public static final int PROCESS_COUNT_COLUMN = 8;
    public static final int RESOURCE_COUNT_COLUMN = 9;

    public static final int OUTPUT_COLUMN = 10;

    public static final int NUMBER_OF_COLUMNS = 11;

    private JobDescription jobDescription;

    private Job job;

    private State hubState;

    private State jobState;

    private final JobTableModel model;
    private final GUI gui;

    public JobRow(JobDescription jobDescription, JobTableModel model, GUI gui)
            throws Exception {
        this.jobDescription = jobDescription.resolve(gui.getApplicationSet(),
                gui.getGrid());
        this.model = model;
        this.gui = gui;

        job = null;
        jobState = State.UNKNOWN;
        hubState = State.UNKNOWN;
    }

    public Job getJob() {
        return job;
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
        case CLUSTER_COLUMN:
            return jobDescription.getClusterName();
        case MIDDLEWARE_COLUMN:
            return jobDescription.getClusterOverrides().getJobAdaptor();
        case APPLICATION_COLUMN:
            return jobDescription.getApplicationName();
        case PROCESS_COUNT_COLUMN:
            return jobDescription.getProcessCount();
        case RESOURCE_COUNT_COLUMN:
            return jobDescription.getResourceCount();
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
        case CLUSTER_COLUMN:
        case MIDDLEWARE_COLUMN:
            return String.class;

        case JOB_STATUS_COLUMN:
        case HUB_STATUS_COLUMN:
            return State.class;

        case PROCESS_COUNT_COLUMN:
        case RESOURCE_COUNT_COLUMN:
            return Integer.class;

        default:
            return Object.class;
        }
    }

    private void setJobState(State state) {
        this.jobState = state;

        model.fireTableCellUpdated(this, JOB_STATUS_COLUMN, CONTROL_COLUMN);
    }

    private void setHubState(State state) {
        this.hubState = state;
        model.fireTableCellUpdated(this, HUB_STATUS_COLUMN);
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
            System.err.println("submit job");
            job = gui.getDeploy().submitJob(jobDescription,
                    gui.getApplicationSet(), gui.getGrid(),
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

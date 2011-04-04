package ibis.deploy.gui.experiment.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.StateListener;

public class JobMonitorRow {

    private static final Logger logger = LoggerFactory.getLogger(JobMonitorRow.class);

    public static final int POOL_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int JOB_STATUS_COLUMN = 2;
    public static final int HUB_STATUS_COLUMN = 3;
    public static final int CLUSTER_COLUMN = 4;
    public static final int MIDDLEWARE_COLUMN = 5;
    public static final int APPLICATION_COLUMN = 6;
    public static final int PROCESS_COUNT_COLUMN = 7;
    public static final int RESOURCE_COUNT_COLUMN = 8;
    public static final int OUTPUT_COLUMN = 9;

    public static final int NUMBER_OF_COLUMNS = 10;

    private Job job;

    private JobDescription jobDescription;

    private State hubState;

    private State jobState;

    private final JobMonitorTableModel model;

    public JobMonitorRow(Job job, JobMonitorTableModel model) throws Exception {
        this.job = job;
        this.model = model;
 
        jobDescription = job.getDescription();

        jobState = State.UNKNOWN;
        hubState = State.UNKNOWN;

        job.addStateListener(new StateListener() {
            public void stateUpdated(final State state, Exception e) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setJobState(state);
                    }
                });
            }
        });
        
        job.addHubStateListener(new StateListener() {
            public void stateUpdated(final State state, Exception e) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setHubState(state);
                    }
                });
            }
        });
        

    }

    public Job getJob() {
        return job;
    }

    public Object getValue(int col) {
        switch (col) {
        case POOL_COLUMN:
            return jobDescription.getPoolName();
        case NAME_COLUMN:
            return jobDescription.getName();
        case JOB_STATUS_COLUMN:
            return jobState;
        case HUB_STATUS_COLUMN:
            return hubState;
        case CLUSTER_COLUMN:
            return jobDescription.getCluster().getName();
        case MIDDLEWARE_COLUMN:
            return jobDescription.getCluster().getJobAdaptor();
        case APPLICATION_COLUMN:
            return jobDescription.getApplication().getName();
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
        case OUTPUT_COLUMN:
            return Boolean.class;

        case APPLICATION_COLUMN:
        case POOL_COLUMN:
        case NAME_COLUMN:
        case CLUSTER_COLUMN:
        case MIDDLEWARE_COLUMN:
            return String.class;

        case HUB_STATUS_COLUMN:
        case JOB_STATUS_COLUMN:
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

        logger.info("JobRow: Job" + this.getJob() + " state now " + state);

        model.fireTableDataChanged();
    }
    
    private void setHubState(State state) {
        this.hubState = state;
        model.fireTableDataChanged();
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }
}

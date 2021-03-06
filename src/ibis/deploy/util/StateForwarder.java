package ibis.deploy.util;

import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for forwarding listener calls. Separate class to prevent some deadlocks
 * 
 * @author Niels Drost
 * 
 */
public class StateForwarder implements MetricListener, Runnable {

    private static final Logger logger = LoggerFactory
            .getLogger(StateForwarder.class);

    private final String name;

    // listeners
    private final List<StateListener> listeners;

    private State currentState = State.CREATED;

    private Exception exception = null;

    public StateForwarder(String name) {
        this.name = name;

        listeners = new ArrayList<StateListener>();

        ThreadPool.createNew(this, "State forwarder");
    }

    public synchronized void addListener(StateListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);

        // tell listener the current state
        try {
            listener.stateUpdated(currentState, exception);
        } catch (Throwable t) {
            logger.warn("State update handler threw exception", t);
        }

    }

    public synchronized void setState(State state) {
        if (this.currentState == state
                || (currentState == State.DEPLOYED && state == State.INITIALIZING)) {
            // The latter condition can occur if ibis-deploy detects that the
            // job is running before JavaGAT delivers the upcall. --Ceriel
            return;
        }

        if (state.ordinal() < currentState.ordinal()) {
            logger.warn("Tried to set state backward from " + currentState
                    + " to " + state);
            return;
        }

        logger.info(name + " state now " + state);

        this.currentState = state;

        notifyAll();
    }

    public synchronized void setErrorState(Exception exception) {
        setState(State.ERROR);

        if (this.exception == null) {
            this.exception = exception;
        }

        notifyAll();
    }

    /**
     * @see org.gridlab.gat.monitoring.MetricListener#processMetricEvent(org.gridlab.gat.monitoring.MetricEvent)
     */
    public void processMetricEvent(MetricEvent event) {
        logger.debug(name + " GAT status now " + event.getValue());

        if (!(event.getValue() instanceof org.gridlab.gat.resources.Job.JobState)) {
            logger.warn(event.getValue() + " not of type JobState");
            return;
        }

        switch ((org.gridlab.gat.resources.Job.JobState) event.getValue()) {
        case INITIAL:
            setState(State.SUBMITTED);
            break;
        case PRE_STAGING:
            setState(State.COPYING);
            break;
        case SCHEDULED:
            setState(State.SCHEDULED);
            break;
        case RUNNING:
            setState(State.INITIALIZING);
            break;
        case POST_STAGING:
            setState(State.DOWNLOADING);
            break;
        case STOPPED:
            setState(State.DONE);
            break;
        case SUBMISSION_ERROR:
            setState(State.ERROR);
            break;
        default:
            logger.warn("Unknown state: " + event.getValue());
        }
    }

    public synchronized State getState() {
        return currentState;
    }

    public synchronized Exception getException() {
        return exception;
    }

    public synchronized boolean isFinished() {
        return currentState.equals(State.ERROR)
                || currentState.equals(State.DONE);
    }

    public synchronized boolean isRunning() {
        return currentState.equals(State.DEPLOYED);
    }

    public synchronized void waitUntilFinished() throws Exception {
        while (currentState.ordinal() < State.DONE.ordinal()) {
            if (exception != null) {
                throw exception;
            }

            try {
                wait();
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    public synchronized void waitUntilDeployed() throws Exception {
        while (currentState.ordinal() < State.DEPLOYED.ordinal()) {
            if (exception != null) {
                throw exception;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
        if (exception != null) {
            throw exception;
        }
        if (currentState.ordinal() > State.DEPLOYED.ordinal()) {
            throw new Exception(name + " state passed deployed state (now " + currentState + ") while waiting for it to start");
        }
    }

    public void run() {
        State reported = null;
        Exception reportedException = null;

        while (true) {
            StateListener[] listeners;

            synchronized (this) {
                if (reported != null
                        && (reported.equals(State.ERROR) || reported
                                .equals(State.DONE))) {
                    logger.debug("state forwarder stopping. job has stopped");
                    return;
                }

                while (currentState.equals(reported)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // IGNORE
                    }
                }

                // state has changed, report!
                reported = currentState;
                reportedException = exception;
                listeners = this.listeners.toArray(new StateListener[0]);
            }

            for (StateListener listener : listeners) {
                try {
                    listener.stateUpdated(reported, reportedException);
                } catch (Throwable t) {
                    logger.warn("State update handler threw exception", t);
                }
            }
        }

    }

}

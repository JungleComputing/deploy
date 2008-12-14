package ibis.deploy;

import ibis.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for forwarding listener calls. Separate class to prevent some deadlocks
 * 
 * @author Niels Drost
 * 
 */
class StateForwarder implements MetricListener, Runnable {

    private static final Logger logger = LoggerFactory
            .getLogger(StateForwarder.class);

    private final String name;

    // listeners
    private final List<StateListener> listeners;

    private State currentState = State.INITIAL;

    StateForwarder(String name) {
        this.name = name;

        listeners = new ArrayList<StateListener>();

        ThreadPool.createNew(this, "State forwarder");
    }

    synchronized void addListener(StateListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);

        // tell listener the current state
        try {
            listener.stateUpdated(currentState);
        } catch (Throwable t) {
            logger.warn("State update handler threw exception", t);
        }

    }

    synchronized void setState(State state) {
        if (this.currentState == state) {
            return;
        }

        if (state.ordinal() < currentState.ordinal()) {
            logger.warn("Tried to set state backward from " + currentState
                    + " to " + state);
            return;
        }

        this.currentState = state;
        notifyAll();
    }

    /**
     * @see org.gridlab.gat.monitoring.MetricListener#processMetricEvent(org.gridlab.gat.monitoring.MetricEvent)
     */
    public void processMetricEvent(MetricEvent event) {
        logger.debug(name + " GAT status now " + event.getValue());

        if (!(event.getValue() instanceof JobState)) {
            logger.warn(event.getValue() + " not of type JobState");
            return;
        }

        switch ((JobState) event.getValue()) {
        case INITIAL:
            setState(State.SUBMITTED);
            break;
            
        case PRE_STAGING:
            setState(State.PRE_STAGING);
            break;
            
        case SCHEDULED:
            setState(State.SCHEDULED);
            break;
        case RUNNING:
            setState(State.RUNNING);
            break;
        case POST_STAGING:
            setState(State.POST_STAGING);
            break;
        case STOPPED:
            setState(State.STOPPED);
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
    
    public synchronized boolean done() {
        return currentState.equals(State.ERROR)
        || currentState.equals(State.STOPPED);
    }


    public void run() {
        State reported = null;

        while (true) {
            StateListener[] listeners;

            synchronized (this) {
                if (reported != null && (reported.equals(State.ERROR)
                        || reported.equals(State.STOPPED))) {
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
                listeners = this.listeners.toArray(new StateListener[0]);
            }

            for (StateListener listener : listeners) {
                try {
                    listener.stateUpdated(reported);
                } catch (Throwable t) {
                    logger.warn("State update handler threw exception", t);
                }
            }
        }

    }

}

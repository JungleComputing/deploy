package ibis.deploy;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for forwarding listener calls. Separate class to prevent some deadlocks 
 * @author Niels Drost
 *
 */
class Listeners implements MetricListener {
    
    private static final Logger logger = LoggerFactory
    .getLogger(Listeners.class);
    
    private final String name;
    
    // listeners
    private final List<MetricListener> listeners;

    Listeners(String name) {
        this.name = name;
        
        listeners = new ArrayList<MetricListener>();
    }
    
    synchronized void addListener(MetricListener listener) {
        listeners.add(listener);
    }
    
    /**
     * @see org.gridlab.gat.monitoring.MetricListener#processMetricEvent(org.gridlab.gat.monitoring.MetricEvent)
     */
    public synchronized void processMetricEvent(MetricEvent event) {
        logger.info(name + " status now " + event.getValue());
        for (MetricListener listener : listeners) {
            listener.processMetricEvent(event);
        }
    }
}

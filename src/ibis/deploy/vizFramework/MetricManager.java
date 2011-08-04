package ibis.deploy.vizFramework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.vizFramework.bundles.BundlesVisualization;
import ibis.deploy.vizFramework.bundles.data.BundlesDataConvertor;
import ibis.deploy.vizFramework.globeViz.data.GlobeDataConvertor;
import ibis.deploy.vizFramework.globeViz.data.IDataConvertor;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;

/**
 * @author Ana Vinatoru
 *
 */

public class MetricManager {
    private Collector collector;
    private ArrayList<IDataConvertor> dataConvertors;
    private Timer refreshTimer;
    private static MetricManager manager = null;

    private MetricManager(final Collector collector,
            ArrayList<IVisualization> visualizations) {

        this.collector = collector;

        // initialize the array of data convertors
        dataConvertors = new ArrayList<IDataConvertor>();

        // create the globe data convertor and add it to the array
        for (IVisualization vis : visualizations) {
            if (vis instanceof GlobeVisualization) {
                dataConvertors.add(new GlobeDataConvertor(
                        (GlobeVisualization) vis, collector.getRoot()));
            } else if (vis instanceof BundlesVisualization) {
                dataConvertors.add(new BundlesDataConvertor(
                        (BundlesVisualization) vis));
            }
        }

        // the javax,swing.Timer is synchronized with the gui rendering thread -
        // if we use a separate thread for the update the MarkerLayer starts
        // throwing exceptions
        refreshTimer = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                update();
            }
        });
        refreshTimer.start();
    }

    public static MetricManager getMetricManager(final Collector collector,
            ArrayList<IVisualization> visualizations) {
        if (manager == null) {
            manager = new MetricManager(collector, visualizations);
        }
        return manager;
    }

    public static MetricManager getMetricManager() {
        return manager;
    }

    public void update() {
        Location root = collector.getRoot();
        for (IDataConvertor convertor : dataConvertors) {
            convertor.updateData(root, collector.change(), false);
        }
    }

    public void togglePause(boolean pause){
        for (IDataConvertor convertor : dataConvertors) {
            convertor.togglePause(pause);
        }
    }
    
}

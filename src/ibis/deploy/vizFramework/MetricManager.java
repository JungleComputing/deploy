package ibis.deploy.vizFramework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.vizFramework.globeViz.data.GlobeVizDataConvertor;
import ibis.deploy.vizFramework.globeViz.data.IDataConvertor;
import ibis.deploy.vizFramework.globeViz.viz.GlobeVisualization;

public class MetricManager {
    private Collector collector;
    private ArrayList<IDataConvertor> dataConvertors;
    private Timer refreshTimer;

    public MetricManager(final Collector collector, GlobeVisualization globe) {

        this.collector = collector;

        // initialize the array of data convertors
        dataConvertors = new ArrayList<IDataConvertor>();

        // create the globe data convertor and add it to the array
        dataConvertors.add(new GlobeVizDataConvertor(globe, collector.getRoot()));

        
        refreshTimer = new Timer(1000, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                update();
            }
        });
        refreshTimer.start();
        
//        // Create update thread
//        DataRefreshTimer updater = new DataRefreshTimer(this);
//        new Thread(updater).start();
    }

    public void update() {
        Location root = collector.getRoot();
        for (IDataConvertor convertor : dataConvertors) {
            convertor.updateData(root, collector.change());
        }
    }

    private void printLocations(Location root, String spacer) {
        ArrayList<Location> dataChildren = root.getChildren();
        if (dataChildren == null || dataChildren.size() == 0) {
            ArrayList<Ibis> ibises = root.getAllIbises();
            for (Ibis ibis : ibises) {
                System.out.println(spacer + ibis.toString());
            }
        }
        for (Location loc : dataChildren) {
            System.out.println(spacer + loc.getName());
            printLocations(loc, spacer.concat("  "));
        }
    }

    public Collector getCollector() {
        return collector;
    }
}

//class DataRefreshTimer implements Runnable {
//    private MetricManager mgr;
//
//    private int UPDATE_INTERVAL = 1000;
//
//    public DataRefreshTimer(MetricManager mgr) {
//        this.mgr = mgr;
//        UPDATE_INTERVAL = mgr.getCollector().getRefreshRate();
//    }
//
//    public void run() {
//        while (true) {
//            mgr.update();
//            try {
//                Thread.sleep(UPDATE_INTERVAL);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
//    }
//
//}

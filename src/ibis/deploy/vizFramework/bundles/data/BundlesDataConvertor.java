package ibis.deploy.vizFramework.bundles.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.Location;
import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Link.LinkDirection;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;
import ibis.deploy.monitoring.collection.impl.IbisImpl;
import ibis.deploy.vizFramework.bundles.BundlesVisualization;
import ibis.deploy.vizFramework.globeViz.data.IDataConvertor;
import ibis.deploy.vizFramework.globeViz.viz.utils.UIConstants;
import ibis.deploy.vizFramework.globeViz.viz.utils.Utils;

public class BundlesDataConvertor implements IDataConvertor {

    private final Location root;
    private final BundlesVisualization bundlesViz;

    private HashMap<String, Set<String>> ibisesPerSite = new HashMap<String, Set<String>>();
    private HashMap<String, HashMap<String, Double>> connectionsPerIbis = new HashMap<String, HashMap<String, Double>>();

    public BundlesDataConvertor(BundlesVisualization vPanel, Location rootRef) {
        root = rootRef;
        bundlesViz = vPanel;
    }

    public synchronized void updateData(Location root,
            boolean structureChanged, boolean forced) {
        if (structureChanged || ibisesPerSite.size() == 0) {
            ibisesPerSite.clear();
            connectionsPerIbis.clear();
        }
        updateLocations(root, UIConstants.LEVELS, structureChanged
                || ibisesPerSite.size() == 0);

        Utils.updateMinMaxWeights(connectionsPerIbis);

        // update the UI
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
                bundlesViz.updateVisualization(ibisesPerSite,
                        connectionsPerIbis);
        // }
        // });
    }

    // creates an annotation for each existing cluster. These will later be
    // grouped into pie charts.
    private synchronized void updateLocations(Location root, int level,
            boolean structureChanged) {
        ArrayList<Location> dataChildren = root.getChildren();
        if (dataChildren == null || dataChildren.size() == 0) {
            ArrayList<Ibis> dataIbises = root.getIbises();
            for (Ibis dataIbis : dataIbises) {
                IbisImpl ibis = (IbisImpl) dataIbis;
                String ibisName = ibis.getName();
                
                String locationName = extractLocationName(ibisName);

                // String locationName = ibisName
                // .substring(ibisName.indexOf("@") + 1);

                ibisName = extractIbisName(ibisName);

                if (structureChanged) {
                    ibisesPerSite.get(locationName).add(ibisName);
                }
                Link[] links = dataIbis.getLinks();
                String startLocation = null, stopLocation = null;
                double value1 = 0, value2 = 0;

                for (Link link : links) {
                    // only create arcs between locations
                    if ((link.getSource() instanceof IbisImpl)
                            && (link.getDestination() instanceof IbisImpl)) {
                        startLocation = ((IbisImpl) link.getSource()).getName();
                        startLocation = extractIbisName(startLocation);
                        
                        stopLocation = ((IbisImpl) link.getDestination())
                                .getName();
                        stopLocation = extractIbisName(stopLocation);

                        for (Metric metric : link
                                .getMetrics(LinkDirection.SRC_TO_DST)) {
                            try {
                                value1 = (Float) metric.getValue(
                                        MetricModifier.NORM,
                                        MetricOutput.PERCENT);

                            } catch (OutputUnavailableException e) {
                                System.out.println(e.getMessage());
                            }
                        }

                        for (Metric metric : link
                                .getMetrics(LinkDirection.DST_TO_SRC)) {
                            try {
                                value2 = (Float) metric.getValue(
                                        MetricModifier.NORM,
                                        MetricOutput.PERCENT);
                            } catch (OutputUnavailableException e) {
                                System.out.println(e.getMessage());
                            }
                        }

                        if (connectionsPerIbis.get(startLocation) == null) {
                            connectionsPerIbis.put(startLocation,
                                    new HashMap<String, Double>());
                        }
                        connectionsPerIbis.get(startLocation).put(stopLocation,
                                (value1 + value2) / 2); // since we're
                                                        // displaying
                                                        // percentages ...
                        // System.out.println(connectionsPerIbis.get(startLocation).get(stopLocation));
                    }

                }
            }
            return;
        }
        for (Location loc : dataChildren) {
            if (level == 0 && structureChanged) {
                String locationName = extractLocationName(loc.getName());
                ibisesPerSite.put(locationName, new HashSet<String>());
                // ibisesPerSite.put(loc.getName(), new
                // HashSet<String>());//TODO
            }

            updateLocations(loc, level - 1, structureChanged);
        }
    }

    private String extractLocationName(String name) {
        return "cluster" + name.substring(name.lastIndexOf("@"));
    }
    
    private String extractIbisName(String ibisName){
        String locationName = extractLocationName(ibisName);
        return ibisName.substring(0, ibisName.indexOf("-")) + "@"
                + locationName;
    }
}

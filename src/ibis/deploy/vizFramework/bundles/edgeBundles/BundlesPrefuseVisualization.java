package ibis.deploy.vizFramework.bundles.edgeBundles;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleManager;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualTable;
import prefuse.visual.tuple.TableNodeItem;

/**
 * @author Ana Vinatoru
 *
 */

public class BundlesPrefuseVisualization extends Visualization {

    @Override
    public synchronized VisualGraph addGraph(String group, Graph graph,
            Predicate filter, Schema nodeSchema, Schema edgeSchema) {
        checkGroupExists(group); // check before adding sub-tables
        String ngroup = PrefuseLib.getGroupName(group, Graph.NODES);
        String egroup = PrefuseLib.getGroupName(group, Graph.EDGES);

        VisualTable nt, et;
        nt = addTable(ngroup, graph.getNodeTable(), filter, nodeSchema);
        et = addTable(egroup, graph.getEdgeTable(), filter, edgeSchema);

        VisualGraph vg = new VisualGraph(nt, et, graph.isDirected(), graph
                .getNodeKeyField(), graph.getEdgeSourceField(), graph
                .getEdgeTargetField());
        vg.setVisualization(this);
        vg.setGroup(group);

        addDataGroup(group, vg, graph);

        TupleManager ntm = new TupleManager(nt, vg, TableNodeItem.class);

        // customized edge items
        TupleManager etm = new TupleManager(et, vg, BSplineEdgeItem.class);
        nt.setTupleManager(ntm);
        et.setTupleManager(etm);
        vg.setTupleManagers(ntm, etm);

        return vg;
    }
    
    @Override
    public synchronized void repaint() {
        long startTime = System.currentTimeMillis();
        super.repaint();
        long stopTime = System.currentTimeMillis();
        //System.out.println("Time to repaint: " + (stopTime - startTime));
    }
}

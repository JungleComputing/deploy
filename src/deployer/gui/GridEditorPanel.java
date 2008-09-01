package deployer.gui;

import deployer.Cluster;
import deployer.Deployer;
import deployer.Grid;
import deployer.PropertySet;
import deployer.PropertySetGroup;

public class GridEditorPanel extends PropertyEditorPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GridEditorPanel(Deployer deployer, String rootName) {
        super(deployer, rootName);
    }

    protected PropertySetGroup load(Deployer deployer, String path)
            throws Exception {
        return deployer.addGrid(new Grid(path));
    }

    protected PropertySetGroup addGroup(Deployer deployer, int i)
            throws Exception {
        return deployer.addGrid(new Grid("grid " + i, (Cluster) null));
    }

    protected PropertySet addGroupEntry(Deployer deployer,
            PropertySetGroup propertyGroupObject, int i) throws Exception {
        return new Cluster("cluster " + i, (Grid) propertyGroupObject);
    }

    protected void removeGroup(Deployer deployer, PropertySetGroup group) {
        deployer.removeGrid((Grid) group);
    }

    protected void removeGroupEntry(Deployer deployer, PropertySet entry) {
        ((Cluster) entry).getGrid().removeCluster((Cluster) entry);
    }

}

package deployer.ibis.gui;

import deployer.Cluster;
import deployer.Deployer;
import deployer.Grid;
import deployer.PropertySet;
import deployer.PropertySetGroup;
import deployer.gui.PropertyEditorPanel;
import deployer.ibis.IbisBasedGrid;
import deployer.ibis.IbisCluster;

public class IbisGridEditorPanel extends PropertyEditorPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public IbisGridEditorPanel(Deployer deployer, String rootName) {
        super(deployer, rootName);
    }

    protected PropertySetGroup load(Deployer deployer, String path)
            throws Exception {
        return deployer.addGrid(new IbisBasedGrid(path));
    }

    protected PropertySetGroup addGroup(Deployer deployer, int i)
            throws Exception {
        return deployer.addGrid(new IbisBasedGrid("grid " + i, (Cluster) null));
    }

    protected PropertySet addGroupEntry(Deployer deployer,
            PropertySetGroup propertyGroupObject, int i) throws Exception {
        return new IbisCluster("cluster " + i,
                (IbisBasedGrid) propertyGroupObject);
    }

    protected void removeGroup(Deployer deployer, PropertySetGroup group) {
        deployer.removeGrid((Grid) group);
    }

    protected void removeGroupEntry(Deployer deployer, PropertySet entry) {
        ((Cluster) entry).getGrid().removeCluster((Cluster) entry);
    }

}

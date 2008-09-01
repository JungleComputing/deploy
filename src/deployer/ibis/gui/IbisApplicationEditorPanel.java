package deployer.ibis.gui;

import deployer.Application;
import deployer.ApplicationGroup;
import deployer.Deployer;
import deployer.PropertySet;
import deployer.PropertySetGroup;
import deployer.gui.PropertyEditorPanel;
import deployer.ibis.IbisApplication;
import deployer.ibis.IbisBasedApplicationGroup;

public class IbisApplicationEditorPanel extends PropertyEditorPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public IbisApplicationEditorPanel(Deployer deployer, String rootName) {
        super(deployer, rootName);
    }

    protected PropertySetGroup load(Deployer deployer, String path)
            throws Exception {
        return deployer
                .addApplicationGroup(new IbisBasedApplicationGroup(path));
    }

    protected PropertySetGroup addGroup(Deployer deployer, int i)
            throws Exception {
        return deployer.addApplicationGroup(new IbisBasedApplicationGroup(
                "application group " + i, (Application) null));
    }

    protected PropertySet addGroupEntry(Deployer deployer,
            PropertySetGroup propertyGroupObject, int i) throws Exception {
        return new IbisApplication("application " + i,
                (IbisBasedApplicationGroup) propertyGroupObject);
    }

    protected void removeGroup(Deployer deployer, PropertySetGroup group) {
        deployer.removeApplicationGroup((ApplicationGroup) group);
    }

    protected void removeGroupEntry(Deployer deployer, PropertySet entry) {
        ((Application) entry).getGroup().removeApplication((Application) entry);
    }
}

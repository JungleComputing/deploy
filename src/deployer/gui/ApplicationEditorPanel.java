package deployer.gui;

import deployer.Application;
import deployer.ApplicationGroup;
import deployer.Deployer;
import deployer.JavaApplication;
import deployer.JavaBasedApplicationGroup;
import deployer.PropertySet;
import deployer.PropertySetGroup;

public class ApplicationEditorPanel extends PropertyEditorPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ApplicationEditorPanel(Deployer deployer, String rootName) {
        super(deployer, rootName);
    }

    protected PropertySetGroup load(Deployer deployer, String path)
            throws Exception {
        return deployer
                .addApplicationGroup(new JavaBasedApplicationGroup(path));
    }

    protected PropertySetGroup addGroup(Deployer deployer, int i)
            throws Exception {
        return deployer.addApplicationGroup(new JavaBasedApplicationGroup(
                "application group " + i, (Application) null));
    }

    protected PropertySet addGroupEntry(Deployer deployer,
            PropertySetGroup propertyGroupObject, int i) throws Exception {
        return new JavaApplication("application " + i,
                (JavaBasedApplicationGroup) propertyGroupObject);
    }

    protected String[] getCategories() {
        return new String[] { "basic", "java", "ibis" };
    }

    protected void removeGroup(Deployer deployer, PropertySetGroup group) {
        deployer.removeApplicationGroup((ApplicationGroup) group);

    }

    protected void removeGroupEntry(Deployer deployer, PropertySet entry) {
        ((Application) entry).getGroup().removeApplication((Application) entry);
    }
}

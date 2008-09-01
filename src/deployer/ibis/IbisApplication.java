package deployer.ibis;

import ibis.util.TypedProperties;

import java.util.SortedMap;
import java.util.TreeMap;

import deployer.ApplicationGroup;
import deployer.JavaApplication;
import deployer.PropertyCategory;

/**
 * This object contains all the information needed to describe an ibis
 * application. This information is about <b>which</b> application should be
 * run and not about where this application should be run.
 * 
 * @author rkemp
 */
public class IbisApplication extends JavaApplication {

    public IbisApplication(String name, ApplicationGroup group,
            TypedProperties properties) throws Exception {
        super(name, group, properties);

        SortedMap<String, String> ibisApplicationProperties = new TreeMap<String, String>();
        for (String key : IbisBasedApplicationGroup.KEYS) {
            ibisApplicationProperties.put(key, properties.getProperty(name
                    + "." + key, null));
        }
        getCategories().add(
                new PropertyCategory("ibis", ibisApplicationProperties));
    }

    public IbisApplication(String name, ApplicationGroup group) throws Exception {
        super(name, group);
        SortedMap<String, String> ibisApplicationProperties = new TreeMap<String, String>();
        for (String key : IbisBasedApplicationGroup.KEYS) {
            ibisApplicationProperties.put(key, null);
        }
        getCategories().add(
                new PropertyCategory("ibis", ibisApplicationProperties));
    }

    /**
     * Returns the files that should be pre staged in order to start an ibis
     * server.
     * 
     * @return the files that should be pre staged in order to start an ibis
     *         server.
     */
    public String getIbisPreStage() {
        for (PropertyCategory category : getCategories()) {
            if (category.getData().get("ibis.prestage") != null) {
                return category.getData().get("ibis.prestage");
            }
        }
        return null;
    }

}

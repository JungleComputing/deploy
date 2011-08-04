package ibis.deploy.vizFramework.globeViz.data;

import ibis.deploy.monitoring.collection.Location;

/**
 * @author Ana Vinatoru
 *
 */

public interface IDataConvertor {

    public void updateData(Location root, boolean structureChanged, boolean forced);
    public void togglePause(boolean pause);
}

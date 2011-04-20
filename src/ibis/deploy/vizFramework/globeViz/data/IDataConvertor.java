package ibis.deploy.vizFramework.globeViz.data;

import ibis.deploy.monitoring.collection.Location;

public interface IDataConvertor {

    public void updateData(Location root, boolean structureChanged);
}

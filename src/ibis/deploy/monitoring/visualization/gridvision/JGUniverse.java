package ibis.deploy.monitoring.visualization.gridvision;

import ibis.deploy.monitoring.collection.Location;

import java.util.ArrayList;

import javax.media.opengl.glu.gl2.GLUgl2;

public class JGUniverse extends JGVisualAbstract implements JGVisual {
    public JGUniverse(JungleGoggles goggles, JGVisual parent, GLUgl2 glu,
            Location root) {
        super(goggles, parent);

        locationSeparation[0] = 13;
        locationSeparation[1] = 13;
        locationSeparation[2] = 13;

        locationColShape = CollectionShape.CUBE;
        state = State.UNFOLDED;

        ArrayList<Location> dataChildren = root.getChildren();

        for (Location datachild : dataChildren) {
            locations.add(new JGLocation(goggles, this, glu, datachild,
                    FloatMatrixMath.div(locationSeparation, (datachild
                            .getRank()
                            + 1 * datachild.getRank() + 1))));
        }
    }
}
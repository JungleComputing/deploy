package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Location;


public class JGUniverse extends JGVisualAbstract implements JGVisual {	
	public JGUniverse(JungleGoggles goggles, JGVisual parent, GLUgl2 glu, Location root) {
		super(goggles, parent);
		
		locationSeparation[0] = 16;
		locationSeparation[1] = 16;
		locationSeparation[2] = 16;
		
		locationColShape = CollectionShape.CITYSCAPE;
		state = State.UNFOLDED;
		
		//goggles.registerVisual(root, this);
		
		ArrayList<Location> dataChildren = root.getChildren();
		
		for (Location datachild : dataChildren) {
			locations.add(new JGLocation(goggles, this, glu, datachild, locationSeparation));
		}
	}	
}
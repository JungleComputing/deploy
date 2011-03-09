package ibis.deploy.monitoring.visualization.gridvision;

import java.util.ArrayList;

import javax.media.opengl.glu.gl2.GLUgl2;

import ibis.deploy.monitoring.collection.Location;


public class JGUniverse extends JGVisualAbstract implements JGVisual {	
	public JGUniverse(JungleGoggles jv, GLUgl2 glu, Location root) {
		super();
		separation = 16.0f;
		cShape = CollectionShape.CITYSCAPE;
		
		jv.registerVisual(root, this);
		
		ArrayList<Location> dataChildren = root.getChildren();
		
		for (Location datachild : dataChildren) {
			locations.add(new JGLocation(jv,glu,datachild));
		}
		
		constructDimensions();
	}	
}
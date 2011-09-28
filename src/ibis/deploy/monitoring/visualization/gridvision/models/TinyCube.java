package ibis.deploy.monitoring.visualization.gridvision.models;

import ibis.deploy.monitoring.visualization.gridvision.common.*;
import ibis.deploy.monitoring.visualization.gridvision.models.base.Rectangle;

public class TinyCube extends DualModel {
	static float SIZE = 0.025f;
	
	public TinyCube (Color4 color) {
		transparent = new Rectangle(SIZE, SIZE, SIZE, new Point4(), color, true);
	}
}

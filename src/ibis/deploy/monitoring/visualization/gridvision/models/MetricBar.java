package ibis.deploy.monitoring.visualization.gridvision.models;

import ibis.deploy.monitoring.visualization.gridvision.common.*;
import ibis.deploy.monitoring.visualization.gridvision.models.base.Rectangle;

public class MetricBar extends DualModel {
	static float HEIGHT = 1f;
	static float WIDTH = 0.25f;
	
	public MetricBar (float height, Color4 bottomColor, Color4 topColor) {
		Point4 pos = new Point4();
		pos.set(2, ((-0.5f*HEIGHT)+(height*0.5f)));
		
		solid 		= new Rectangle(height, WIDTH, WIDTH, pos, bottomColor, true);
		
		pos.set(2, ((0.5f*HEIGHT)-((HEIGHT-height)*0.5f)));
		
		transparent = new Rectangle(HEIGHT-height, WIDTH, WIDTH, pos, topColor, false);
	}
}

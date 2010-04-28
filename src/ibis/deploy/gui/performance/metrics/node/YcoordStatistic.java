package ibis.deploy.gui.performance.metrics.node;

import ibis.deploy.gui.performance.metrics.MetricsObjectInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class YcoordStatistic extends NodeMetricsObject implements MetricsObjectInterface {
	public static final String NAME = "COORDS_Y";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final Float[] COLOR = {0.0f, 0.0f, 0.0f};
	
	public YcoordStatistic() {
		super();
		this.name = NAME;
		this.color = COLOR;
		attributesCountNeeded = DESCRIPTIONS_COUNT_NEEDED;
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "vivaldi");	
	}
	
	public void update(Object[] results) {
		Coordinates coord = (Coordinates) results[0];
		
		double[] unUsableCoords = coord.getCoordinates();
		value = (float) unUsableCoords[1];		
	}
}
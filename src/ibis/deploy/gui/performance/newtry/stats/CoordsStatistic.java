package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class CoordsStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "COORDS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	public static final int VALUES_COUNT = 3;
	
	public CoordsStatistic() {
		super();
		
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "vivaldi");
		values = new Float[VALUES_COUNT];
	}
	
	public void update(Object[] results) {
		Coordinates coord = (Coordinates) results[0];
		
		double[] unUsableCoords = coord.getCoordinates();
		values[0] = (float) unUsableCoords[0];
		values[1] = (float) unUsableCoords[1];
		values[2] = (float) unUsableCoords[2];
	}
}

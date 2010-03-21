package ibis.deploy.gui.performance.newtry.stats;

import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class ZcoordStatistic extends StatisticsObject implements StatisticsObjectInterface {
	public static final String NAME = "COORDS";
	public static final int DESCRIPTIONS_COUNT_NEEDED = 1;
	
	public ZcoordStatistic() {
		super();
		
		necessaryAttributes = new AttributeDescription[DESCRIPTIONS_COUNT_NEEDED];
		necessaryAttributes[0] = new AttributeDescription("ibis", "vivaldi");	}
	
	public void update(Object[] results) {
		Coordinates coord = (Coordinates) results[0];
		
		double[] unUsableCoords = coord.getCoordinates();
		value = (float) unUsableCoords[2];		
	}
}
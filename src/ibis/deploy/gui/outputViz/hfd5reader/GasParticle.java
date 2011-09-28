package ibis.deploy.gui.outputViz.hfd5reader;

import java.util.HashMap;

public class GasParticle {
	public HashMap<Integer, Double> x;
	public HashMap<Integer, Double> y;
	public HashMap<Integer, Double> z;
		
	public GasParticle() {
		x = new HashMap<Integer, Double>();
		y = new HashMap<Integer, Double>();
		z = new HashMap<Integer, Double>();
	}
}

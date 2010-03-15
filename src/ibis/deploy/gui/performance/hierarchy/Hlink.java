package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.stats.SingleStat;
import ibis.ipl.IbisIdentifier;

import javax.media.opengl.GL;

public class Hlink extends Hobject implements HobjectInterface {
	protected SingleStat[] theStats;
	
	public Hlink(PerfVis perfvis, int glName, IbisIdentifier from, IbisIdentifier to) {
		super(perfvis, glName);
	}

	public void drawThis(GL gl, int glMode) {
		// TODO Auto-generated method stub
		
	}

	public void update() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

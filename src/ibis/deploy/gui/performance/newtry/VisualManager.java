package ibis.deploy.gui.performance.newtry;

import java.util.List;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.newtry.dataobjects.Pool;

public class VisualManager {
	List<Vpool> pools;

	public VisualManager(PerfVis perfVis) {
		// TODO Auto-generated constructor stub
	}

	public void reinitialize(List<Pool> pools) {
		// TODO Auto-generated method stub
		
	}
	
	public void drawAll() {
		int i = 0;
		
		for (Pool pool : pools) {			
	
			pools.get(entry.getKey()).setSize(0.25f, 1.0f);
			pools.get(entry.getKey()).drawThis(gl, mode);
			i++;
		}
	}

}

package ibis.deploy.monitoring.visualization.gridvision.swing.listeners;

import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RefreshrateSliderChangeListener implements ChangeListener {
	private GogglePanel gp;
	
	public RefreshrateSliderChangeListener(GogglePanel gp) {
		this.gp = gp;		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        int newRate = source.getValue();        
	        
	        gp.setRefreshrate(newRate);
	        
	    }
		
	}

}

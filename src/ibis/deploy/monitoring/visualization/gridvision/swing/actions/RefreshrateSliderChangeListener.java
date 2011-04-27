package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RefreshrateSliderChangeListener implements ChangeListener {
	private GogglePanel gp;
	private JungleGoggles goggles;
	
	public RefreshrateSliderChangeListener(GogglePanel gp) {
		this.gp = gp;
		goggles = gp.getGoggles();		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        int newRate = source.getValue();
	        
	        goggles.setRefreshrate(newRate);
	        gp.setRefreshrateText(newRate);
	        
	    }
		
	}

}

package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IbisSpacingSliderChangeListener implements ChangeListener {
	private GogglePanel gp;
	private JungleGoggles goggles;
	
	public IbisSpacingSliderChangeListener(GogglePanel gp) {
		this.gp = gp;
		goggles = gp.getGoggles();		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	    	int sliderSetting = source.getValue();	    	
	        float value = sliderSetting / 10f;    
	        
	        goggles.setIbisSpacing(value);								
			gp.setIbisSpacerText(value);			
	    }		
	}
}

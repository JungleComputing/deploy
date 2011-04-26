package ibis.deploy.monitoring.visualization.gridvision.swing.actions;

import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MetricSpacingSliderChangeListener implements ChangeListener {
	private GogglePanel gp;
	private JungleGoggles goggles;
	
	public MetricSpacingSliderChangeListener(GogglePanel gp) {
		this.gp = gp;
		goggles = gp.getGoggles();		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {	    	
	    	int sliderSetting = (int)source.getValue();
	    	float value = ((float)sliderSetting)/20f;
	    	
	    	goggles.setMetricSpacing(value);								
	    	gp.setMetricSpacerText(value);
	    }
	}
}

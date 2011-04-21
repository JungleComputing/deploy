package ibis.deploy.monitoring.visualization.gridvision.swing;

import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.MetricDescriptionNotAvailableException;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ThresholdSliderChangeListener implements ChangeListener {
	private GogglePanel gp;
	private JungleGoggles goggles;
	
	public ThresholdSliderChangeListener(GogglePanel gp) {
		this.gp = gp;
		goggles = gp.getGoggles();		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        int newMax = (int)source.getValue();
	        MetricDescription myDescription;
			try {
				myDescription = goggles.getMetricDescription("Bytes_Sent_Per_Sec");
				myDescription.setMaxForPercentages(newMax);
								
				gp.setNetworkThresholdText(newMax);
			} catch (MetricDescriptionNotAvailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	    }
		
	}

}

package ibis.deploy.monitoring.visualization.gridvision.swing.listeners;

import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.MetricDescriptionNotAvailableException;

import java.awt.event.ItemEvent;


public class MetricListener implements GoggleListener {	
	JungleGoggles goggles;
	MetricDescription[] myDescs;
	
	public MetricListener(JungleGoggles goggles, String label) {		
		this.goggles = goggles;
				
		myDescs = new MetricDescription[6];
		try {
			myDescs[0] = goggles.getMetricDescription("CPU");
			myDescs[1] = goggles.getMetricDescription("Load");
			myDescs[2] = goggles.getMetricDescription("MEM_SYS");
			myDescs[3] = goggles.getMetricDescription("MEM_HEAP");
			myDescs[4] = goggles.getMetricDescription("MEM_NONHEAP");
			myDescs[5] = goggles.getMetricDescription("Bytes_Sent_Per_Sec");
		} catch (MetricDescriptionNotAvailableException e) {
			e.printStackTrace();
		}
		
		try {
			if (label.compareTo("CPU Usage") == 0) {
				myDescs = new MetricDescription[1];
				myDescs[0] = goggles.getMetricDescription("CPU");			
			} else if (label.compareTo("System Load Average")== 0) {
				myDescs = new MetricDescription[1];
				myDescs[0] = goggles.getMetricDescription("Load");
			} else if (label.compareTo("System Memory Usage")== 0) {
				myDescs = new MetricDescription[1];
				myDescs[0] = goggles.getMetricDescription("MEM_SYS");
			} else if (label.compareTo("Java Heap Memory Usage")== 0) {
				myDescs = new MetricDescription[1];
				myDescs[0] = goggles.getMetricDescription("MEM_HEAP");
			} else if (label.compareTo("Java Nonheap Memory Usage")== 0) {
				myDescs = new MetricDescription[1];
				myDescs[0] = goggles.getMetricDescription("MEM_NONHEAP");
			}
		} catch (MetricDescriptionNotAvailableException e) {
			e.printStackTrace();
		}
	}

	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.DESELECTED || arg0.getStateChange() == ItemEvent.SELECTED) {
			goggles.toggleMetrics(myDescs);
		}
	}
	
	public GoggleListener clone(String label) {
		return new MetricListener(goggles, label);		
	}

	
}

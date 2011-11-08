package ibis.deploy.monitoring.visualization.gridvision.swing.listeners;

import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.MetricDescriptionNotAvailableException;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ThresholdSliderChangeListener implements ChangeListener {
    private final GogglePanel gp;
    private final JungleGoggles goggles;

    public ThresholdSliderChangeListener(GogglePanel gp, JungleGoggles goggles) {
        this.gp = gp;
        this.goggles = goggles;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            int sliderSetting = source.getValue();
            int newMax = 0;

            if (sliderSetting == 0) {
                newMax = 1;
            } else if (sliderSetting == 1) {
                newMax = 10;
            } else if (sliderSetting == 2) {
                newMax = 25;
            } else if (sliderSetting == 3) {
                newMax = 50;
            } else if (sliderSetting == 4) {
                newMax = 100;
            } else if (sliderSetting == 5) {
                newMax = 250;
            } else if (sliderSetting == 6) {
                newMax = 500;
            } else if (sliderSetting == 7) {
                newMax = 1000;
            } else if (sliderSetting == 8) {
                newMax = 2500;
            } else if (sliderSetting == 9) {
                newMax = 5000;
            }

            MetricDescription myDescription;
            try {
                myDescription = goggles.getMetricDescription("Bytes_Sent_Per_Sec");
                myDescription.setMaxForPercentages(newMax * 1024);

                myDescription = goggles.getMetricDescription("MPI_Bytes_Sent_Per_Sec");
                myDescription.setMaxForPercentages(newMax * 1024);

                gp.setNetworkThreshold(newMax);
            } catch (MetricDescriptionNotAvailableException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }

    }

}

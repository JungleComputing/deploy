package ibis.deploy.monitoring.visualization.gridvision.swing.listeners;

import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MetricSpacingSliderChangeListener implements ChangeListener {
    private final GogglePanel gp;

    public MetricSpacingSliderChangeListener(GogglePanel gp) {
        this.gp = gp;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            int sliderSetting = source.getValue();
            float value = sliderSetting / 10f;

            gp.setMetricSpacer(value);
        }
    }
}

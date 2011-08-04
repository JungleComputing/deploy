package ibis.deploy.vizFramework.globeViz.viz.markers;

import javax.swing.SwingUtilities;

import ibis.deploy.vizFramework.globeViz.data.GlobeDataConvertor;

/**
 * @author Ana Vinatoru
 *
 */

public class MarkerMovementThread implements Runnable {
    private static final int APS = 8;

    private long startTime = 0;
    private int waitTime;
    private GlobeDataConvertor convertor;

    public MarkerMovementThread(GlobeDataConvertor convertor) {
        waitTime = 1000 / APS;
        this.convertor = convertor;
        startTime = System.currentTimeMillis();
    }

    public void startTiming(long timeToComplete) {
        this.startTime = System.currentTimeMillis();

    }

    public void run() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;
            startTime = currentTime;

            float fraction = (float) elapsed / 1000f;

            // TODO - do refresh here
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    convertor.moveMarkers();
                }
            });

            try {
                if (waitTime - elapsed > 0) {
                    Thread.sleep(waitTime - elapsed);
                } else {
                    Thread.sleep(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

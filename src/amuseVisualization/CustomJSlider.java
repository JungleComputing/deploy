package amuseVisualization;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class CustomJSlider extends JSlider {
	private static final long serialVersionUID = -3067450096465148814L;

	public CustomJSlider () {
		MouseListener[] listeners = getMouseListeners();
	    for (MouseListener l : listeners)
	        removeMouseListener(l); // remove UI-installed TrackListener
	    final BasicSliderUI ui = (BasicSliderUI) getUI();
	    BasicSliderUI.TrackListener tl = ui.new TrackListener() {
	        // this is where we jump to absolute value of click
	        @Override public void mouseClicked(MouseEvent e) {
	            Point p = e.getPoint();
	            int value = ui.valueForXPosition(p.x);
	
	            setValue(value);
	        }
	        // disable check that will invoke scrollDueToClickInTrack
	        @Override public boolean shouldScroll(int dir) {
	            return false;
	        }
	    };
	    addMouseListener(tl);
	}
}
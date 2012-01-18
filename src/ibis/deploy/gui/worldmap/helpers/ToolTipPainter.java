package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.WorldMapPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.painter.Painter;

public class ToolTipPainter<T extends JXMapViewer> implements
        Painter<JXMapViewer> {

    private Point location; // location of the label
    private WorldMapPanel worldMapPanel = null;
    private final int rowHeight = 12;
    private final int shadowPx = 1;

    private String label = new String();

    public ToolTipPainter(WorldMapPanel parentPanel) {
        super();
        worldMapPanel = parentPanel;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        int width, height;
        double newX, newY;

        String selectedResourceName = null;
        if (worldMapPanel.getSelectedResource() != null) {
            selectedResourceName = worldMapPanel.getSelectedResource().getName();
        }

        Rectangle bounds = map.getBounds();
        if (location != null) {
            Font oldFont = g.getFont();
            g.setFont(oldFont.deriveFont(Font.BOLD));

            height = rowHeight;
            newX = location.getX();
            newY = location.getY();

            width = (int) g.getFontMetrics().getStringBounds(label, g)
                    .getWidth();

            // compute new position of the tooltip relative to the bounds
            if (newX < bounds.getX()) {
                newX = bounds.getX();
            }

            if (newX + width > bounds.getX() + bounds.getWidth()) {
                newX = bounds.getX() + bounds.getWidth() - width;
            }

            if (newY < bounds.getY() + height) {
                newY = bounds.getY() + height;
            }

            if (newY > bounds.getY() + bounds.getHeight()) {
                newY = bounds.getY() + bounds.getHeight();
            }

            // update the location
            if (newX != location.getX() || newY != location.getY()) {
                location = new Point((int) newX, (int) newY);
            }

            // draw the shadow
            g.setColor(Color.black);

            g.drawString(label, (int) location.getX() - shadowPx,
                    (int) location.getY() + -shadowPx);
            g.drawString(label, (int) location.getX() + shadowPx,
                    (int) location.getY() + -shadowPx);
            g.drawString(label, (int) location.getX() - shadowPx,
                    (int) location.getY() + shadowPx);
            g.drawString(label, (int) location.getX() + shadowPx,
                    (int) location.getY() + shadowPx);

            // draw the text
            if (label.equals(selectedResourceName)) {
                g.setColor(new Color(255, 100, 100));
            } else {
                g.setColor(Color.white);
            }
            g.drawString(label, (int) location.getX(), (int) location.getY());

            g.setFont(oldFont);
        }
    }

    public void setLabel(String lbl) {
        label = lbl;
    }

}

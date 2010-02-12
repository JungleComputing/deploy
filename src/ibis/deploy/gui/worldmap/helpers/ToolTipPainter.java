package ibis.deploy.gui.worldmap.helpers;

import ibis.deploy.gui.worldmap.WorldMapPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.painter.Painter;

public class ToolTipPainter<T extends JXMapViewer> implements Painter<JXMapViewer>
{

	private Point location;
	private WorldMapPanel worldMapPanel = null;
	private final int rowHeight = 12;
	private final int shadowPx = 1;
	
	public ArrayList<String> labels = new ArrayList<String>();
	
	public ToolTipPainter(WorldMapPanel parentPanel)
	{
		super();
		worldMapPanel = parentPanel;
	}
	
	public Point getLocation()
	{
		return location;
	}
	
	public void setLocation(Point location)
	{
		this.location = location;
	}
	
	public void paint(Graphics2D g, JXMapViewer map, int w, int h)
	{
		int i;
		int width, height;
		int maxWidth = 0;
		double newX, newY;
		
		String selectedClusterName = null;
		if(worldMapPanel.getSelectedCluster() != null)
			selectedClusterName = worldMapPanel.getSelectedCluster().getName();
		
		Rectangle bounds = map.getBounds();
		if(location != null)
        {
        	Font oldFont = g.getFont();
    		g.setFont(oldFont.deriveFont(Font.BOLD));
    		
    		 
    		height = labels.size() * rowHeight;
    		newX = location.getX();
    		newY = location.getY();
    		
    		//get maximum label width
    		for(i = 0; i < labels.size(); i++)
        	{
        		width = (int) g.getFontMetrics().getStringBounds(labels.get(i), g).getWidth();
        		if(width > maxWidth)
        			maxWidth = width;
        	}
    		
    		//recompute new position of the  tooltip relative to the bounds
    		if(newX < bounds.getX())
    			newX = bounds.getX();
    		
    		if(newX + maxWidth > bounds.getX() + bounds.getWidth())
    			newX = bounds.getX() + bounds.getWidth() - maxWidth;
    		
    		if(newY < bounds.getY())
    			newY = bounds.getY();
    		
    		if(newY + height > bounds.getY() + bounds.getHeight())
    			newY = bounds.getY() + bounds.getHeight() - height;
    		
    		//update the location
    		if(newX != location.getX() || newY != location.getY())
    			location = new Point((int)newX, (int)newY);
    		
    		//draw the text
        	for(i = 0; i< labels.size(); i++)
        	{
        		//draw the shadow
        		g.setColor(Color.black);
        		
        		g.drawString(labels.get(i), (int)location.getX() - shadowPx , 
        				(int)location.getY() + i*rowHeight - shadowPx);
        		g.drawString(labels.get(i), (int)location.getX() + shadowPx ,
        				(int)location.getY() + i*rowHeight - shadowPx);
        		g.drawString(labels.get(i), (int)location.getX() - shadowPx ,
        				(int)location.getY() + i*rowHeight + shadowPx);
        		g.drawString(labels.get(i), (int)location.getX() + shadowPx ,
        				(int)location.getY() + i*rowHeight + shadowPx);
        		
        		//draw the text
        		if(labels.get(i).equals(selectedClusterName))
        			g.setColor(new Color(255, 100,100));
        		else
        			g.setColor(Color.white);
        		g.drawString(labels.get(i), (int)location.getX(), 
        				(int)location.getY() + i*rowHeight);
        	}
      
        	g.setFont(oldFont);
        }
		
		
	}
	
}

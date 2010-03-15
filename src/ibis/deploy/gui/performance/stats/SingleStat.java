package ibis.deploy.gui.performance.stats;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.deploy.gui.performance.visuals.Element;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class SingleStat {
	protected PerfVis perfvis;
	
	protected String name;
	protected IbisIdentifier ibis;
	protected ManagementServiceInterface manInterface;
	
	public float value;
	public int glName;
	
	private Element element;

	protected int zoomStat;
	protected int zoomLevel;
	
	SingleStat(PerfVis perfvis, IbisIdentifier ibis, Float[] color) {
		this.perfvis = perfvis;
		this.ibis = ibis;
		this.manInterface = perfvis.getManInterface();
		
		element = new Element(perfvis, color);
	}
	
	public void update() throws Exception {
		throw new MethodNotOverriddenException();
	}
	
	public void setName(int glName) {
		this.glName = glName;
	}
	
	public void setSize(float width, float height) {
		element.setSize(width, height);		
	}
	
	public void drawThis(GL gl, int mode, int glMode) {
		try {
			element.setForm(mode);
			element.setFilledArea(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		element.setGLName(glName);
		element.drawThis(gl, glMode);
	}
		
	public double[] getVivaldi() {
		Coordinates coord = null;
		AttributeDescription vivaldi = new AttributeDescription("ibis", "vivaldi");
		try {
			coord = (Coordinates)manInterface.getAttributes(ibis, vivaldi)[0];
			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return coord.getCoordinates();
	}
	
	public IbisIdentifier[] getConnections() {
		IbisIdentifier[] conn = null;
		AttributeDescription connections = new AttributeDescription("ibis", "connections");
		try {
			conn = (IbisIdentifier[]) manInterface.getAttributes(ibis, connections)[0];
			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return conn;
	}
	public void setZoom(int zoomLevel, int zoomStat) {
		this.zoomLevel = zoomLevel;
		this.zoomStat = zoomStat;
	}
}
package ibis.deploy.gui.performance.hierarchy.stats;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vsinglestat;
import ibis.deploy.gui.performance.exceptions.MethodNotOverriddenException;
import ibis.deploy.gui.performance.hierarchy.Hobject;
import ibis.deploy.gui.performance.hierarchy.HobjectInterface;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.support.vivaldi.Coordinates;

public class Hsinglestat extends Hobject implements HobjectInterface {
	
	protected IbisIdentifier ibis;
	protected ManagementServiceInterface manInterface;
	
	protected float value;
	
	public Hsinglestat(PerfVis perfvis, IbisIdentifier ibis, Float[] color) {
		super(perfvis);
		
		this.ibis = ibis;
		this.manInterface = perfvis.getManInterface();
		
		myVisual = new Vsinglestat(perfvis, color);
	}
	
	public void update() throws Exception {
		throw new MethodNotOverriddenException();
	}
	
	public void setSize(float width, float height) {
		myVisual.setSize(width, height);		
	}
	
	public void drawThis(GL gl, int glMode) {
		try {
			((Vsinglestat) myVisual).setSize(width, height);
			((Vsinglestat) myVisual).setLocation(location);
			
			((Vsinglestat) myVisual).setForm(perfvis.getCurrentElementForm());
			
			((Vsinglestat) myVisual).setValue(value);				
			
			((Vsinglestat) myVisual).drawThis(gl, glMode);	
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public float getValue() {
		return value;
	}
}
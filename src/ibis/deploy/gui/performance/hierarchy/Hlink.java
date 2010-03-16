package ibis.deploy.gui.performance.hierarchy;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vlink;
import ibis.deploy.gui.performance.Vrarchy.Vobject;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValueOutOfBoundsException;
import ibis.deploy.gui.performance.hierarchy.stats.HlinkUsage;
import ibis.deploy.gui.performance.hierarchy.stats.Hsinglestat;

import javax.media.opengl.GL;

/*
 * Hlinks only exist between Hnodes 
 */
public class Hlink extends Hobject implements HobjectInterface {
	protected Hsinglestat link;
		
	public Hlink(PerfVis perfvis, Hnode from, Hnode to) {
		super(perfvis);
		
		link = new HlinkUsage(perfvis, from.getIbis(), to.getIbis(), Vobject.NETWORK_LINK_COLOR);		
		
		myVisual = new Vlink(perfvis, Vobject.NETWORK_LINK_COLOR, from.getVisual(), to.getVisual());
	}

	public void drawThis(GL gl, int glMode) {		
		try {
			((Vlink) myVisual).setSize(width, height);
			((Vlink) myVisual).setLocation(location);
			((Vlink) myVisual).setForm(perfvis.getCurrentLinkForm());
			((Vlink) myVisual).setValue(link.getValue());				
			
			((Vlink) myVisual).drawThis(gl, glMode);
		} catch (ModeUnknownException e) {			
			e.printStackTrace();
		} catch (ValueOutOfBoundsException e) {
			e.printStackTrace();
		}
		
	}

	public void update() throws Exception {		
		//update the values		
		link.update();		
	}
}
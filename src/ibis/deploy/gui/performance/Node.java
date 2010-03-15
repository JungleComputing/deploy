package ibis.deploy.gui.performance;

import ibis.deploy.gui.performance.stats.*;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;

import javax.media.opengl.GL;

public class Node {	 
	private static final int GLNAMEMULT = 1;
	
	private PerfVis perfvis;
	
	private IbisIdentifier ibis;
	private CpuUsage cpu_stat;	
	private MemUsage mem_stat;			
		
	private Collection stats;
	private Float[] values;
	
	private int zoomLevel = 0;
	private int zoomStat = 0;
	private int glName;
			
	Node(PerfVis perfvis, IbisIdentifier ibis, ManagementServiceInterface manInterface) {	
		this.perfvis = perfvis;
		this.ibis = ibis;
		
		Float[] cpu_color = Collection.CPU_HIGH_COLOR;
		Float[] mem_color = Collection.MEM_HIGH_COLOR;
							
		cpu_stat  = new CpuUsage(perfvis, ibis, cpu_color);
		mem_stat  = new MemUsage(perfvis, ibis, mem_color);
				
		//the compounded stat collection
		String[] names = {"cpu", "mem"};
		Float colors[][] = {Collection.CPU_HIGH_COLOR, Collection.MEM_HIGH_COLOR};
		stats = new Collection(perfvis, names, colors);
		values = new Float[2];		
	}
	
	public void update() throws Exception {	
		if (zoomLevel == PerfVis.ZOOM_NODES) {
			
			//update the values
			cpu_stat.update(); 
			mem_stat.update();
		
			Float[] new_values = {cpu_stat.value, mem_stat.value};
			values = new_values;		
		
			if (perfvis.getSelection() >= glName && perfvis.getSelection() < glName + 1000*GLNAMEMULT) {			
				if (perfvis.getSelection() % 1000 == 0) perfvis.setValue(cpu_stat.value);
				if (perfvis.getSelection() % 1000 == 1) perfvis.setValue(mem_stat.value);			
			}
		}
	}
	
	public void drawThis(GL gl, float width, float height, int siteForm, int barForm, int mode) {
		//TODO Grid visualization if statement and vivaldi coords
		if (zoomLevel == PerfVis.ZOOM_NODES) {
			try {			
				stats.setSize(width, height); 
				stats.setSeparation(0.0f);
				stats.setForm(siteForm, barForm);
				stats.setGLName(glName);
				stats.setValues(values);
				stats.setLocation(0.0f, 0.0f, 0.0f);
				stats.drawThis(gl, mode);			
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setName(int glName) {
		this.glName = glName;		
		cpu_stat.setName(glName+ GLNAMEMULT);
		mem_stat.setName(glName+ GLNAMEMULT);		
	}
	
	public void setZoom(int zoomLevel, int zoomStat) {
		this.zoomLevel = zoomLevel;
		this.zoomStat = zoomStat;
		
		cpu_stat.setZoom(zoomLevel, zoomStat);
		mem_stat.setZoom(zoomLevel, zoomStat);
		
	}
}

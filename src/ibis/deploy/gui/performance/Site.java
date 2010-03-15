package ibis.deploy.gui.performance;

import ibis.deploy.gui.performance.stats.*;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;

import javax.media.opengl.GL;

public class Site {	 
	private static final int GLNAMEMULT = 1;
	
	private PerfVis perfvis;
	
	private IbisIdentifier[] ibises;
	private CpuUsage[] cpu_stats;	
	private MemUsage[] mem_stats;			
		
	//Site compounded visualization variables
	public float cpu_low = 9999f;
	public float cpu_avg = 0;
	public float cpu_high = -9999f;
	public float mem_low = 9999f;
	public float mem_avg = 0;
	public float mem_high = -9999f;		
	private Collection compoundStats;
	private Float[] compoundValues;
	
	//Individual stats visualization variables
	private Collection cpuCollection;
	private Float[] cpu_values;
	
	private Collection memCollection;
	private Float[] mem_values;	
	
	private int zoomLevel = 0;
	private int zoomStat = 0;
	private int glName;
			
	Site(PerfVis perfvis, IbisIdentifier[] ibises) {	
		this.perfvis = perfvis;
		
		cpu_stats = new CpuUsage[ibises.length];
		cpu_values = new Float[ibises.length];
		
		mem_stats = new MemUsage[ibises.length];
		mem_values = new Float[ibises.length];
		
		Float[] cpu_color = Collection.CPU_HIGH_COLOR;
		Float[] mem_color = Collection.MEM_HIGH_COLOR;
		
		for (int i=0; i<ibises.length; i++) {			
			cpu_stats[i]  = new CpuUsage(perfvis, ibises[i], cpu_color);
			mem_stats[i]  = new MemUsage(perfvis, ibises[i], mem_color);
		}
		
		//the compounded stat collection
		String[] names = {"cpu_high", "cpu_avg", "cpu_low", "mem_high", "mem_avg", "mem_low"};
		Float colors[][] = {Collection.CPU_HIGH_COLOR, Collection.CPU_AVG_COLOR, Collection.CPU_LOW_COLOR, 
							Collection.MEM_HIGH_COLOR, Collection.MEM_AVG_COLOR, Collection.MEM_LOW_COLOR};
		compoundStats = new Collection(perfvis, names, colors);
		compoundValues = new Float[6];
		
		
		//individual stat collections
		names = new String[ibises.length];
		colors = new Float[ibises.length][];					
		
		for (int i=0; i<ibises.length; i++) {			
				names[i] = "CPU Ibis " + ibises[i].name();
				colors[i] = cpu_color;	
		}
		cpuCollection = new Collection(perfvis, names, colors);
		
		for (int i=0; i<ibises.length; i++) {
			names[i] = "MEM Ibis " + ibises[i].name();
			colors[i] = mem_color;						
		}		
		memCollection = new Collection(perfvis, names, colors);
		
		this.ibises = ibises;
	}
	
	public void update() throws Exception {	
		if (zoomLevel == PerfVis.ZOOM_POOLS || zoomLevel == PerfVis.ZOOM_SITES) {
			cpu_low = 9999f;
			cpu_avg = 0;
			cpu_high = -9999f;
			mem_low = 9999f;
			mem_avg = 0;
			mem_high = -9999f;	
			
			for (int i=0; i<ibises.length; i++) {
				//update the values
				cpu_stats[i].update(); 
				mem_stats[i].update();
							
				//calculate lowest, highest and averages
				if (cpu_stats[i].value < cpu_low) 	cpu_low = cpu_stats[i].value;
				if (cpu_stats[i].value > cpu_high) 	cpu_high = cpu_stats[i].value;
				if (mem_stats[i].value < mem_low) 	mem_low = mem_stats[i].value;
				if (mem_stats[i].value > mem_high) 	mem_high = mem_stats[i].value;
				
				cpu_avg += cpu_stats[i].value;
				mem_avg += mem_stats[i].value;
			}
			cpu_avg = cpu_avg/ibises.length;
			mem_avg = mem_avg/ibises.length;
			
			Float[] new_values = {cpu_high, cpu_avg, cpu_low, mem_high, mem_avg, mem_low};
			compoundValues = new_values;
		} else if (zoomLevel == PerfVis.ZOOM_NODES) {
			if (zoomStat == PerfVis.STAT_CPU || zoomStat == PerfVis.STAT_ALL) {
				for (int i=0; i<ibises.length; i++) {
					cpu_stats[i].update();
					cpu_values[i] = cpu_stats[i].value;
				}
				 
			} else if (zoomStat == PerfVis.STAT_MEM || zoomStat == PerfVis.STAT_ALL) {
				for (int i=0; i<ibises.length; i++) {			
					mem_stats[i].update();
					mem_values[i] = mem_stats[i].value;
				}
			}
		}
		
		if (zoomLevel == PerfVis.ZOOM_SITES && perfvis.getSelection() >= glName && perfvis.getSelection() < glName + 1000*GLNAMEMULT) {
			//if (zoomLevel == PerfVis.ZOOM_SITES && zoomStat == PerfVis.STAT_CPU) {
				if (perfvis.getSelection() % 1000 == 0) perfvis.setValue(cpu_high);
				if (perfvis.getSelection() % 1000 == 1) perfvis.setValue(cpu_avg);
				if (perfvis.getSelection() % 1000 == 2) perfvis.setValue(cpu_low);
			//} else if (zoomLevel == PerfVis.ZOOM_SITES && zoomStat == PerfVis.STAT_MEM) {
				if (perfvis.getSelection() % 1000 == 3) perfvis.setValue(mem_high);
				if (perfvis.getSelection() % 1000 == 4) perfvis.setValue(mem_avg);
				if (perfvis.getSelection() % 1000 == 5) perfvis.setValue(mem_low);			
			//}
		}
	}
	
	public void drawThis(GL gl, float width, float height, int siteForm, int barForm, int mode) {
		//TODO Grid visualization if statement and vivaldi coords
		
		try {
			if (zoomLevel == PerfVis.ZOOM_SITES) {
				compoundStats.setSize(width, height); 
				compoundStats.setSeparation(0.1f);
				compoundStats.setForm(siteForm, barForm);
				compoundStats.setGLName(glName);
				compoundStats.setValues(compoundValues);
				compoundStats.setLocation(0.0f, 0.0f, 0.0f);
				compoundStats.drawThis(gl, mode);
			} else if (zoomLevel == PerfVis.ZOOM_NODES) {		
				if (zoomStat == PerfVis.STAT_CPU) {				
					cpuCollection.setSize(width, height);
					cpuCollection.setSeparation(0.1f);
					cpuCollection.setForm(siteForm, barForm);
					cpuCollection.setGLName(glName);
					cpuCollection.setValues(cpu_values);
					cpuCollection.setLocation(0.0f, 0.0f, 0.0f);
					cpuCollection.drawThis(gl, mode);
					//cpu_stats[i].setSize(width, height);
					//cpu_stats[i].drawThis(gl, barForm, mode);
				} else if (zoomStat == PerfVis.STAT_MEM) {	
					memCollection.setSize(width, height);
					memCollection.setSeparation(0.1f);
					memCollection.setForm(siteForm, barForm);
					memCollection.setGLName(glName);
					memCollection.setValues(mem_values);
					memCollection.setLocation(0.0f, 0.0f, 0.0f);
					memCollection.drawThis(gl, mode);
					//mem_stats[i].setSize(width, height);
					//mem_stats[i].drawThis(gl, barForm, mode);
				}			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setName(int glName) {
		this.glName = glName;
		for (int i=0; i<ibises.length; i++) {
			cpu_stats[i].setName(glName+ GLNAMEMULT*i);
			mem_stats[i].setName(glName+ GLNAMEMULT*i);
		}
	}
	
	public void setZoom(int zoomLevel, int zoomStat) {
		this.zoomLevel = zoomLevel;
		this.zoomStat = zoomStat;
		for (int i=0; i<ibises.length; i++) {
			cpu_stats[i].setZoom(zoomLevel, zoomStat);
			mem_stats[i].setZoom(zoomLevel, zoomStat);
		}
	}
}

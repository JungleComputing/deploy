package ibis.deploy.gui.performance;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class Pool {	
	private static final int GLNAMEMULT = 1000;
	
	private PerfVis perfvis;
	private IbisIdentifier[] ibises;
			
	private Site[] sites;
	private String poolName;
	
	private int zoomLevel = 0;
	private int zoomStat = 0;
	private int glName;
	
	//Pool visualization variables
	public float cpu_low = 9999f;
	public float cpu_avg = 0;
	public float cpu_high = -9999f;
	public float mem_low = 9999f;
	public float mem_avg = 0;
	public float mem_high = -9999f;	
	private Collection compoundStats;
	private Float[] compoundValues = {0.5f,0.5f,0.5f,0.5f,0.5f,0.5f};
	
	Pool(PerfVis perfvis, String name) {
		this.perfvis = perfvis;		
		
		this.poolName = name;
			
		ibises = new IbisIdentifier[0];
		
		String[] names = {"cpu_high", "cpu_avg", "cpu_low", "mem_high", "mem_avg", "mem_low"};
		Float colors[][] = {Collection.CPU_HIGH_COLOR, Collection.CPU_AVG_COLOR, Collection.CPU_LOW_COLOR, 
							Collection.MEM_HIGH_COLOR, Collection.MEM_AVG_COLOR, Collection.MEM_LOW_COLOR};
		compoundStats = new Collection(perfvis, names, colors);		
	}

	public void update(GUI gui) {
		//Check if the pool's size is updated, if so, reinitialize the displayable features.
		try {
			Map<String, Integer> newSizes = gui.getDeploy().poolSizes();	
			if (ibises.length != newSizes.get(poolName)) {
				ibises = perfvis.getRegInterface().getMembers(poolName);
								
				Map<String, Site> locationsMap = new HashMap<String, Site>();
				String[] locations = {};
				try {
					locations = perfvis.getRegInterface().getLocations(poolName);					
				} catch (IOException e) {					
					e.printStackTrace();
				}
				
				String[] tempLocation_one;
				String[] tempLocation_two;
				
				//make a map of all the sites
				for (int i=0; i<locations.length;i++) {
					tempLocation_one = locations[i].split("@");
					
					if (!locationsMap.containsKey(tempLocation_one[1])) {
						int locationSize = 0, l = 0;
						IbisIdentifier[] localIbises = new IbisIdentifier[0];
						
						//Determine which ibises belong to this site
						for (int j=0; j<ibises.length;j++) {
							tempLocation_two = ibises[j].location().toString().split("@");
							
							//First determine the amount of ibises at this site
							if (tempLocation_two[1].compareTo(tempLocation_one[1]) == 0) {
								locationSize++;
							}
						}
						
						//Then, create a Ibisidentifier array with that size
						localIbises = new IbisIdentifier[locationSize];							
						
						//And add all the site's ibises to that array
						for (int j=0; j<ibises.length;j++) {
							tempLocation_two = ibises[j].location().toString().split("@");
							if (tempLocation_two[1].compareTo(tempLocation_one[1]) == 0) {
								localIbises[l] = ibises[j];
								l++;
							}
						}
						
						locationsMap.put(tempLocation_one[1], new Site(perfvis, localIbises));												
					}
				}
				
				//TODO make the rest of the code use the map instad of an array 				
				sites = new Site[locationsMap.size()];
				int i = 0;
				for (Map.Entry<String, Site> entry : locationsMap.entrySet()) {
					sites[i] = entry.getValue();
					i++;
				}				
			}
			
			cpu_low = 9999f;
			cpu_avg = 0;
			cpu_high = -9999f;
			mem_low = 9999f;
			mem_avg = 0;
			mem_high = -9999f;
			
			for (int i=0; i< sites.length; i++) {
				//update the values
				sites[i].update();						
				
				//calculate lowest, highest and averages
				if (sites[i].cpu_low < cpu_low) cpu_low 	= sites[i].cpu_low;
				if (sites[i].cpu_high > cpu_high) cpu_high 	= sites[i].cpu_high;
				if (sites[i].mem_low < mem_low) mem_low 	= sites[i].mem_low;
				if (sites[i].mem_high > mem_high) mem_high 	= sites[i].mem_high;
				
				cpu_avg += sites[i].cpu_avg;
				mem_avg += sites[i].mem_avg;
			}
			
			cpu_avg = cpu_avg/sites.length;
			mem_avg = mem_avg/sites.length;
				
			Float[] new_values = {cpu_high, cpu_avg, cpu_low, mem_high, mem_avg, mem_low};
			compoundValues = new_values;
			
		} catch (Exception e) {	
			e.printStackTrace();
		}	
		
		if (zoomLevel == PerfVis.ZOOM_POOLS && perfvis.getSelection() >= glName && perfvis.getSelection() < glName + 1000*GLNAMEMULT) {
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
		//Save the old matrix mode and transformation matrix
		IntBuffer oldMode = IntBuffer.allocate(1);		
		gl.glGetIntegerv(GL.GL_MATRIX_MODE, oldMode);
		gl.glPushMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		if (zoomLevel == PerfVis.ZOOM_POOLS) {
			try {
				compoundStats.setSize(width, height);
				compoundStats.setSeparation(0.1f);
				compoundStats.setGLName(glName);
				compoundStats.setValues(compoundValues);
				compoundStats.setForm(siteForm, barForm);
				compoundStats.setLocation(0.0f, 0.0f, 0.0f);
				compoundStats.drawThis(gl, mode);
				
			} catch (Exception e) {				
				e.printStackTrace();
			}			
		} else {
			for (int i=0; i< sites.length; i++) {
				sites[i].drawThis(gl, width, height, siteForm, barForm, mode);
				
				//TODO distance between sites in a single pool				
				gl.glTranslatef(0.0f,0.0f,-1.5f);
			}				
		}
		
		//Restore the old matrix mode and transformation matrix		
		gl.glMatrixMode(oldMode.get());
		gl.glPopMatrix();	
	}
	
	public void setName(int glName) {
		this.glName = glName;
		for (int i=0; i<sites.length; i++) {
			sites[i].setName(glName + i*GLNAMEMULT);
		}
	}
	
	public void setZoom(int zoomLevel, int zoomStat) {
		this.zoomLevel = zoomLevel;
		this.zoomStat = zoomStat;
		for (int i=0; i< sites.length; i++) {
			sites[i].setZoom(zoomLevel, zoomStat);
		}
	}
}

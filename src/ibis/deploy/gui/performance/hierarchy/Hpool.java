package ibis.deploy.gui.performance.hierarchy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.ipl.IbisIdentifier;

public class Hpool extends Hobject implements HobjectInterface{	
	private Hsite[] theSites;
	
	private Float[] theValues;
		
	public Hpool(PerfVis perfvis, int glName, String poolName) {		
		super(perfvis, glName);
				
		try {
			init(poolName);
		} catch (IOException e) {			
			e.printStackTrace();
		}
				
		theNames = new String[Hnode.DISPLAYNAMES.length*3];
		theValues = new Float[Hnode.DISPLAYNAMES.length*3];
		Float[][] colors = new Float[Hnode.DISPLAYNAMES.length*3][];
		for (int i=0; i<theNames.length; i+=3) {
			theNames [i  ] = poolName + " " + Hnode.DISPLAYNAMES[i/3] + "_HIGH";
			colors[i  ] = Hnode.DISPLAYCOLORS[i/3];
			theNames [i+1] = poolName + " " + Hnode.DISPLAYNAMES[i/3] + "_AVG";
			Float[] newColors_avg = {Hnode.DISPLAYCOLORS[i/3][0]*0.66f, Hnode.DISPLAYCOLORS[i/3][1]*0.66f, Hnode.DISPLAYCOLORS[i/3][2]*0.66f};
			colors[i+1] = newColors_avg;
			theNames [i+2] = poolName + " " + Hnode.DISPLAYNAMES[i/3] + "_LOW";
			Float[] newColors_low = {Hnode.DISPLAYCOLORS[i/3][0]*0.33f, Hnode.DISPLAYCOLORS[i/3][1]*0.33f, Hnode.DISPLAYCOLORS[i/3][2]*0.33f};
			colors[i+2] = newColors_low;
		}
		theVobjects[0] = new Collection(perfvis, theNames, colors);
	}	
	
	private void init(String poolName) throws IOException {
		IbisIdentifier[] ibises = perfvis.getRegInterface().getMembers(poolName);
		
		HashMap<String, Hsite> locationsMap = new HashMap<String, Hsite>();
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
				int locationSize = 0;
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
				int nextname = (glName+Hnode.DISPLAYNAMES.length*3) + (Hnode.DISPLAYNAMES.length*3*i);
				locationsMap.put(tempLocation_one[1], new Hsite(perfvis, nextname, tempLocation_one[1], localIbises));												
			}
		}
		
		//An array is more useful in the rest of the code
		theSites = new Hsite[locationsMap.size()];
		int i = 0;
		for (Entry<String, Hsite> entry : locationsMap.entrySet()) {
			theSites[i] = entry.getValue();
			i++;
		}
	}

	public void drawThis(GL gl, int glMode) {
		if (perfvis.getCurrentZoom() == PerfVis.ZOOM_POOLS) {
			try {			
				theVobjects[0].setSize(width, height);
				theVobjects[0].setGLName(glName);
				theVobjects[0].setLocation(location);
				
				((Collection) theVobjects[0]).setForm(perfvis.getCurrentCollectionForm(), perfvis.getCurrentElementForm());
				((Collection) theVobjects[0]).setSeparation(0.1f);
							
				((Collection) theVobjects[0]).setValues(theValues);				
				
				((Collection) theVobjects[0]).drawThis(gl, glMode);			
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (perfvis.getCurrentZoom() == PerfVis.ZOOM_SITES || perfvis.getCurrentZoom() == PerfVis.ZOOM_NODES) {
			for (int i=0; i<theSites.length; i++) {
				
				//TODO meaningful location
				theSites[i].setSize(width, height);
				theSites[i].setLocation(location);
				theSites[i].drawThis(gl, glMode);
			}
		}
	}
	
	public void update() throws Exception {
		for (int i=0; i<theSites.length; i++) {
			theSites[i].update();
		}
		
		if (perfvis.getCurrentZoom() == PerfVis.ZOOM_POOLS) {
			for (int i=0; i<theNames.length; i+=3) {
				//set the values to ridiculous, so that < and > functions work
				float low = 9999f;
				float avg = 0;
				float high = -9999f;				
				
				for (int j=0; j<theSites.length; j++) {							
					//calculate lowest, highest and averages
					if (theSites[j].getStat(i+2) < low) low	= theSites[j].getStat(i+2);
					if (theSites[j].getStat(i) > high)  high= theSites[j].getStat(i);
					
					avg += theSites[j].getStat(i+1);
				}
				avg = avg/theSites.length;				
				
				theValues[i  ] = high;
				theValues[i+1] = avg;
				theValues[i+2] = low;
			}	
				
			//set the HUD value
			if (perfvis.getSelection() >= glName && perfvis.getSelection() < glName + Hnode.DISPLAYNAMES.length*3){
				perfvis.setHUDValues(theNames, theValues);				
			}
		}
	}
}

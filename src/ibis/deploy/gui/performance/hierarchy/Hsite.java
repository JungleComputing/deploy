package ibis.deploy.gui.performance.hierarchy;

import javax.media.opengl.GL;

import ibis.deploy.gui.performance.PerfVis;
import ibis.deploy.gui.performance.Vrarchy.Vsite;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.deploy.gui.performance.exceptions.ValuesMismatchException;
import ibis.deploy.gui.performance.visuals.Collection;
import ibis.deploy.gui.performance.visuals.MetaCollection;
import ibis.deploy.gui.performance.visuals.VisualizationElement;
import ibis.ipl.IbisIdentifier;

public class Hsite extends Hobject implements HobjectInterface{
		
	private Hnode[] theNodes;
	
	private Float[] theValues;	
		
	Hsite(PerfVis perfvis, String name, IbisIdentifier[] ibises) {		
		super(perfvis);
		
		theNodes  = new Hnode[ibises.length];		
		
		for (int i=0; i<ibises.length; i++) {
			theNodes[i] = new Hnode(perfvis, ibises[i]);			
		}
		
		theNames = new String[Hnode.DISPLAYNAMES.length*3];
		theValues = new Float[Hnode.DISPLAYNAMES.length*3];
		Float[][] colors = new Float[Hnode.DISPLAYNAMES.length*3][];
		
		for (int i=0; i<theNames.length; i+=3) {
			theNames [i  ] = name + " " + Hnode.DISPLAYNAMES[i/3] + "_HIGH";
			colors[i  ] = Hnode.DISPLAYCOLORS[i/3];			
			theNames [i+1] = name + " " + Hnode.DISPLAYNAMES[i/3] + "_AVG";
			Float[] newColors_avg = {Hnode.DISPLAYCOLORS[i/3][0]*0.66f, Hnode.DISPLAYCOLORS[i/3][1]*0.66f, Hnode.DISPLAYCOLORS[i/3][2]*0.66f};
			colors[i+1] = newColors_avg;
			theNames [i+2] = name + " " + Hnode.DISPLAYNAMES[i/3] + "_LOW";
			Float[] newColors_low = {Hnode.DISPLAYCOLORS[i/3][0]*0.33f, Hnode.DISPLAYCOLORS[i/3][1]*0.33f, Hnode.DISPLAYCOLORS[i/3][2]*0.33f};
			colors[i+2] = newColors_low;
		}
		myVisual = new Vsite(perfvis, colors);
				
		String[][] nodeNames = new String[theNodes.length][];
		for (int i=0; i<theNodes.length; i++) {
			nodeNames[i] = theNodes[i].theNames;
		}
		theVobjects[1] = new MetaCollection(perfvis, nodeNames, Hnode.DISPLAYCOLORS);
		/*
		
		
		for (int i=0; i < theNodes.length; i++) {			
			int nextname = (glName+DISPLAYSIZE) + (Hnode.DISPLAYSIZE*i);
			theNodes[i] = new Hnode(perfvis, nextname, ibises[i]);
		}
		
		//the compounded stat collection
		String[] names = {name + " cpu_high", name + " cpu_avg", name + " cpu_low", name + " mem_high", name + " mem_avg", name + " mem_low"};
		Float colors[][] = {Collection.CPU_HIGH_COLOR, Collection.CPU_AVG_COLOR, Collection.CPU_LOW_COLOR, 
							Collection.MEM_HIGH_COLOR, Collection.MEM_AVG_COLOR, Collection.MEM_LOW_COLOR};
		theVobjects[0] = new Collection(perfvis, names, colors);	
		theValues = new Float[6];
		theNames = names;
		
		*/
	}

	public void drawThis(GL gl, int glMode) {
		if (perfvis.getCurrentZoom() == PerfVis.ZOOM_SITES) {
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
		} else if (perfvis.getCurrentZoom() == PerfVis.ZOOM_NODES) {
			Float[][] values = new Float[theNodes.length][];
			for (int i=0; i<theNodes.length; i++) {
				
				Float[] some = {theNodes[i].getStat(0), theNodes[i].getStat(1)};
				values[i] = some;
				
				try {
					theVobjects[1].setSize(width, height);
					theVobjects[1].setGLName(glName);
					theVobjects[1].setLocation(location);
					
					((MetaCollection) theVobjects[1]).setForm(perfvis.getCurrentCollectionForm(), perfvis.getCurrentElementForm());
					((MetaCollection) theVobjects[1]).setSeparation(width+0.2f);					
					((MetaCollection) theVobjects[1]).setValues(values);
					
					((MetaCollection) theVobjects[1]).drawThis(gl, glMode);
				} catch (ValuesMismatchException e) {
					e.printStackTrace();
				} catch (ModeUnknownException e) {
					e.printStackTrace();
				}
				
				
				/*
				//TODO meaningful location
				theNodes[i].setSize(width, height);
				theNodes[i].setLocation(location);
				theNodes[i].drawThis(gl, glMode);
				*/				
			}
		}
	}

	public void update() throws Exception {
		for (int i=0; i<theNodes.length; i++) {			
			theNodes[i].update(); 
		}
		
		if (perfvis.getCurrentZoom() == PerfVis.ZOOM_POOLS || perfvis.getCurrentZoom() == PerfVis.ZOOM_SITES) {
			for (int i=0; i<theNames.length; i+=3) {
				//set the values to ridiculous, so that < and > functions work
				float low = 9999f;
				float avg = 0;
				float high = -9999f;				
				
				for (int j=0; j<theNodes.length; j++) {							
					//calculate lowest, highest and averages
					if (theNodes[j].getStat(i/3) < low) 	low	= theNodes[j].getStat(i/3);
					if (theNodes[j].getStat(i/3) > high) 	high= theNodes[j].getStat(i/3);
					
					avg += theNodes[j].getStat(i/3);
				}
				avg = avg/theNodes.length;				
				
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
	public Float getStat(int i) {		
		return theValues[i];
	}
	
	public Float[] getCPU() {
		Float[] values = {theValues[0], theValues[1], theValues[2]};
		return values;
	}
	
	public Float[] getMEM() {
		Float[] values = {theValues[3], theValues[4], theValues[5]};
		return values;
	}
	
	public Hnode[] getNodes() {
		return theNodes;
	}
}

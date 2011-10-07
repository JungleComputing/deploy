package ibis.deploy.gui.outputViz.amuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.HObject;
import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.math.Vec4;
import ibis.deploy.gui.outputViz.common.scenegraph.OctreeNode;
import ibis.deploy.gui.outputViz.exceptions.FileOpeningException;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.models.base.Sphere;
import ibis.deploy.gui.outputViz.shaders.Program;

public class Hdf5Snapshotter {	
	public static OctreeNode cubeRoot;
	public static StarSGNode sgRoot = new StarSGNode();
	
	String evoNamePostfix = ".evo";	
	String gravNamePostfix = ".grav";
	String gasNamePostfix = ".gas";
		
    public Hdf5Snapshotter() {
    }
	
    public void open(String namePrefix, GLWindow glw, Program ppl, Program gas, int currentFrame) {    	
    	@SuppressWarnings("unused")
		Hdf5StarReader starReader = null;		
    	@SuppressWarnings("unused")
		Hdf5GasCloudReader gasReader = null;    	
    	
    	sgRoot = new StarSGNode();
    		
    	HashMap<Long, Star> particles = new HashMap<Long, Star>();
    	long[] particleKeys = null;
    	
    	Material starMaterial = new Material();
    	HashMap<Integer, Model> starModels = new HashMap<Integer, Model>();
    	
    	Vec4 gasColor = new Vec4(.6f,.3f,.3f,0f);
    	Vec4 transparent = new Vec4(0,0,0,0);
    	Material gasMaterial = new Material(gasColor,transparent,transparent);	
    	List<Model> cloudModels = null;
    	    		
		String evoName, gravName, gasName;
		
		HashMap<String, Dataset> particleResult = new HashMap<String, Dataset>();
		List<HObject> particleMemberList;
				
		starModels.put(0, new Sphere(ppl, starMaterial, 2, 0.0001f, new Vec3()));
		
		if (GLWindow.GAS_ON) {
			cloudModels = new ArrayList<Model>();
			float gasSize = GLWindow.GAS_EDGES;
			for (int i=0; i < GLWindow.MAX_CLOUD_DEPTH; i++ ) {				 
				cloudModels.add(new Sphere(gas, gasMaterial, 1, gasSize*3f, new Vec3()));
				gasSize = gasSize/2f;
			}
		}
		
		try {
			if (GLWindow.GAS_ON) {				
				gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
								
				cubeRoot = new OctreeNode(GLWindow.MAX_ELEMENTS_PER_OCTREE_NODE_IN_SNAPSHOT, 0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
				gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
				
				glw.setCubeRoot(cubeRoot);				
			}	
						
			particleMemberList = Hdf5StarReader.getRoot(namePrefix + intToString(currentFrame) + evoNamePostfix).getMemberList();
			Hdf5StarReader.traverse("evo", particleResult, particleMemberList);
			
			particleMemberList = Hdf5StarReader.getRoot(namePrefix + intToString(currentFrame) + gravNamePostfix).getMemberList();
			Hdf5StarReader.traverse("grav", particleResult, particleMemberList);
						
			Dataset keysSet = particleResult.get("evo/particles/0000000001/keys");
			particleMemberList = null;
			
			//Pre-make all the particles in the hashmap
			particleKeys = (long[]) keysSet.read();
			for (int i = 0; i< particleKeys.length; i++) {
		    	particles.put(particleKeys[i], new Star());
		    }
			
			//Close the datasets
			for (Dataset d : particleResult.values()) {
				d.clear();
				d.close(0);
		    }		
			
			Hdf5StarReader.closeFiles();	
			
			//Get the initial data for the particles (frame 0)
			evoName = namePrefix + intToString(currentFrame) + evoNamePostfix;
			gravName = namePrefix + intToString(currentFrame) + gravNamePostfix;
			starReader = new Hdf5StarReader(starModels, particles, evoName, gravName);
			
			//Construct the Scene graph		
			
			for (int i = 0; i < particleKeys.length; i++) {
				StarSGNode node = new StarSGNode();		
				Star p = particles.get(particleKeys[i]);
				Vec4 color = p.color;
				node.setModel(p.model, new Material(color,color,color));
				node.setTranslation(p.location);				
				sgRoot.addChild(node);
			}
						
			glw.setRoot(sgRoot);
			
		} catch (FileOpeningException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
		
	private String intToString(int input) {		
		String result = ""; 
		if (input < 10) {
			result += "00000"+input;
		} else if (input >= 10 && input < 100) {
			result += "0000" +input;
		} else if (input >= 100 && input < 1000) {
			result += "000"   +input;
		} else if (input >= 1000 && input < 10000) {
			result += "00"    +input;
		}  else if (input >= 10000 && input < 100000) {
			result += "0"    +input;
		} else {
			result += input;
		}
		
		return result;
	}
}
